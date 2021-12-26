# -*- coding: utf-8 -*-
#
# Licensed to the Apache Software Foundation (ASF) under one
# or more contributor license agreements.  See the NOTICE file
# distributed with this work for additional information
# regarding copyright ownership.  The ASF licenses this file
# to you under the Apache License, Version 2.0 (the
# "License"); you may not use this file except in compliance
# with the License.  You may obtain a copy of the License at
#
#   http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing,
# software distributed under the License is distributed on an
# "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
# KIND, either express or implied.  See the License for the
# specific language governing permissions and limitations
# under the License.

from airflow.hooks.postgres_hook import PostgresHook
from airflow.hooks.S3_hook import S3Hook
from airflow.models import BaseOperator
from urllib.parse import unquote
from airflow.utils.decorators import apply_defaults
import json

class S3ToRedshiftTransfer(BaseOperator):

    template_fields = ('s3_key', )

    template_ext = ()

    ui_color = '#ededed'

    @apply_defaults
    def __init__(
            self,
            schema,
            table,
            s3_bucket,
            s3_key,
            redshift_conn_id='redshift_default',
            aws_conn_id='aws_default',
            verify=None,
            copy_options=tuple(),
            autocommit=False,
            parameters=None,
            xcom_task_id=None,
            xcom_task_id_key='messages',
            *args, **kwargs):
        super(S3ToRedshiftTransfer, self).__init__(*args, **kwargs)
        self.schema = schema
        self.table = table
        self.s3_bucket = s3_bucket
        self.s3_key = s3_key
        self.redshift_conn_id = redshift_conn_id
        self.aws_conn_id = aws_conn_id
        self.verify = verify
        self.copy_options = copy_options
        self.autocommit = autocommit
        self.parameters = parameters
        self.xcom_task_id = xcom_task_id
        self.xcom_task_id_key = xcom_task_id_key

    def execute(self, context):
        self.hook = PostgresHook(postgres_conn_id=self.redshift_conn_id)
        copy_options = '\n\t\t\t'.join(self.copy_options)
        if self.xcom_task_id:
            s3_keys = self.extract_objects(context)
            #TODO: use manifest, such that COPY only once, but how to get "meta"?
            for s3_key in s3_keys:
                copy_query = """
                    COPY {schema}.{table}
                    FROM 's3://{s3_bucket}/{s3_key}/'
                    {copy_options};
                """.format(schema=self.schema,
                           table=self.table,
                           s3_bucket=self.s3_bucket,
                           s3_key=self.s3_key,
                           copy_options=copy_options)

                self.log.info('Executing COPY command...')
                self.hook.run(copy_query, self.autocommit)
                self.log.info("COPY command complete...")
        else:
            copy_query = """
                COPY {schema}.{table}
                FROM 's3://{s3_bucket}/{s3_key}/'
                {copy_options};
            """.format(schema=self.schema,
                       table=self.table,
                       s3_bucket=self.s3_bucket,
                       s3_key=self.s3_key,
                       copy_options=copy_options)

            self.log.info('Executing COPY command...')
            self.hook.run(copy_query, self.autocommit)
            self.log.info("COPY command complete...")

    def extract_objects(self, context):
        xcom_messages = context['ti'].xcom_pull(self.xcom_task_id, key=self.xcom_task_id_key)
        s3_keys = [r['s3']['object']['key'] for i in xcom_messages['Messages'] for r in json.loads(i['Body'])['Records']]
        s3_keys = [unquote(i) for i in s3_keys if 'hive' not in i]
        return s3_keys
