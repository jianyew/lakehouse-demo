import sys
from awsglue.utils import getResolvedOptions
from pyspark.context import SparkContext
from awsglue.context import GlueContext
from awsglue.dynamicframe import DynamicFrame
from pyspark.sql.types import MapType, StringType
from pyspark.sql import SparkSession
from awsglue.transforms import *
from pyspark.sql.functions import lit
import json


sc = SparkContext.getOrCreate()
spark = SparkSession(sc.getOrCreate())
glueContext = GlueContext(sc.getOrCreate())
args = getResolvedOptions(sys.argv, ['event_date'])
s3_bucket = 'aws-demo-deployment-test'
input_dir = 's3://{}/raw_data'.format(s3_bucket)
event_date = args['event_date']
input_path = '/'.join([input_dir, event_date, ''])

# read in the file to infer schema
## gen_schema
schema_holder = glueContext.create_dynamic_frame_from_options(
    connection_type="s3",
    connection_options={"paths": ['s3://{}/raw_data/test/seed.json'.format(s3_bucket)]},
    format='json')
msg_schema_holder = schema_holder.select_fields(paths=['message']).map(lambda x: x['message'])
msg_schema_holder = msg_schema_holder.resolveChoice(specs=[
    ('completion.page_attr.ActivityCityID', 'cast:string'),
    ('completion.page_attr.ActivityCountryID', 'cast:string'),
    ('completion.page_attr.CategoryID', 'cast:string')])
msg_schema = msg_schema_holder.toDF().schema
extra = MapType(StringType(), StringType())
msg_schema['page'].dataType['extra'].dataType = extra
msg_schema['module'].dataType['extra'].dataType = extra
msg_schema['item'].dataType['extra'].dataType = extra

# schema_holder = glueContext.create_dynamic_frame_from_catalog(
#     database='demo',
#     table_name='message'
#     )
## input
# event_date = '2021/02/23'
raw_rdd = sc.textFile(input_path)
msg_rdd = raw_rdd.map(lambda x: json.loads(json.loads(x)['message']))  # .map(replace_empty_dict)
msg_DF = spark.createDataFrame(data = msg_rdd, schema = msg_schema)
msg_DF = msg_DF.withColumn('event_date', lit(event_date.replace('/', '-')))
msg_DyF = DynamicFrame.fromDF(msg_DF, glueContext, 'DF_to_DyF')

# write the output
## output
additionalOptions = {"enableUpdateCatalog": True}
additionalOptions["partitionKeys"] = ["event_date"]

sink = glueContext.write_dynamic_frame_from_catalog(frame=msg_DyF, database='demo',
                                                    table_name='message', transformation_ctx="write_sink",
                                                    additional_options=additionalOptions)