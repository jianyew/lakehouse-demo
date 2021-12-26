from pyflink.datastream import StreamExecutionEnvironment, TimeCharacteristic, CheckpointingMode
from pyflink.table import StreamTableEnvironment, EnvironmentSettings
source_Kafka = “”"
CREATE TABLE kafka_source (
 id VARCHAR,
 alarm_id VARCHAR,
 trck_id VARCHAR
) WITH (
 ‘connector’ = ‘kafka’,
 ‘topic’ = ‘test’,
 ‘properties.bootstrap.servers’ = ‘*’,
 ‘properties.group.id’ = ‘flink_grouper’,
 ‘scan.startup.mode’ = ‘earliest-offset’,
 ‘format’ = ‘json’,
 ‘json.fail-on-missing-field’ = ‘false’,
 ‘json.ignore-parse-errors’ = ‘true’
)
“”"
source_W_detail_ddl = “”"
CREATE TABLE source_W_detail (
 id VARCHAR,
 alarm_id VARCHAR,
 trck_id VARCHAR
) WITH (
 ‘connector’ = ‘jdbc’,
 ‘url’ = ‘jdbc:mysql:’,
 ‘driver’ = ‘com.mysql.cj.jdbc.Driver’,
 ‘table-name’ = ‘detail’,
 ‘username’ = ‘root’,
 ‘password’ = ‘root’,
 ‘sink.buffer-flush.max-rows’ = ‘1000’,
 ‘sink.buffer-flush.interval’ = ‘2s’
“”"
env = StreamExecutionEnvironment.get_execution_environment()
env.set_stream_time_characteristic(TimeCharacteristic.ProcessingTime)
env.set_parallelism(1)
env_settings = EnvironmentSettings.new_instance().use_blink_planner().in_streaming_mode().build()
t_env = StreamTableEnvironment.create(env, environment_settings=env_settings)
t_env.execute_sql(source_Kafka)
t_env.execute_sql(source_W_detail_ddl)
table_result1=t_env.execute_sql(’’‘insert into source_W_detail select id,alarm_id,trck_id from kafka_source’’’)
table_result1.get_job_client().get_job_execution_result().result()
