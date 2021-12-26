#!/bin/bash

# Please replace your keypair name at line 30 manually.
EMR_CLUSTER_NAME="tdemo-streaming"
S3_LOGGING="s3://aws-tdemo-deployment-test/emr_logs/"

aws emr create-cluster \
    --name "${EMR_CLUSTER_NAME}" \
    --release-label emr-5.29.0 \
    --applications Name=Hadoop Name=Hive Name=Spark Name=Presto\
    --use-default-roles \
    --no-auto-terminate \
    --enable-debugging \
    --log-uri ${S3_LOGGING} \
    --instance-groups '[
    {
        "InstanceCount":1,
        "InstanceGroupType":"MASTER",
        "InstanceType":"m5.xlarge",
        "Name":"Master instance group - 1"
    },
    {
        "InstanceCount":'2',
        "InstanceGroupType":"CORE",
        "InstanceType":"m5.xlarge",
        "Name":"Core instance group - 2"
    }
    ]' \
    --ec2-attributes '{
        "KeyName":"tdemo"
    }' \
    --configurations '[
    {
        "Classification":"emrfs-site",
        "Properties":{
            "fs.s3.consistent.retryPeriodSeconds":"10",
            "fs.s3.consistent":"true",
            "fs.s3.consistent.retryCount":"5",
            "fs.s3.consistent.metadata.tableName":"EmrFSMetadata"
        },
        "Configurations":[]
    }, 
    {
        "Classification": "spark",
        "Properties": {
          "maximizeResourceAllocation": "true"
        }
    },
    {
        "Classification": "presto-connector-hive",
        "Properties": {
            "hive.metastore": "glue"
        }
    },
    {
        "Classification": "spark-hive-site",
        "Properties": {
        "hive.metastore.client.factory.class": "com.amazonaws.glue.catalog.metastore.AWSGlueDataCatalogHiveClientFactory"
        }
    },
    {
        "Classification": "hive-site",
        "Properties": {
          "hive.metastore.client.factory.class": "com.amazonaws.glue.catalog.metastore.AWSGlueDataCatalogHiveClientFactory",
          "hive.metastore.schema.verification": "false"
        }
    }
    ]'