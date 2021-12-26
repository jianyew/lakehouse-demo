import org.apache.flink.api.common.restartstrategy.RestartStrategies;
import org.apache.flink.api.common.serialization.SimpleStringEncoder;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.api.common.time.Time;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.core.fs.Path;
import org.apache.flink.runtime.state.filesystem.FsStateBackend;
import org.apache.flink.streaming.api.CheckpointingMode;
import org.apache.flink.streaming.api.TimeCharacteristic;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.CheckpointConfig;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.sink.filesystem.OutputFileConfig;
import org.apache.flink.streaming.api.functions.sink.filesystem.StreamingFileSink;
import org.apache.flink.streaming.api.functions.sink.filesystem.bucketassigners.DateTimeBucketAssigner;
import org.apache.flink.streaming.api.functions.sink.filesystem.rollingpolicies.DefaultRollingPolicy;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer09;


import java.util.Properties;
import java.util.concurrent.TimeUnit;


public class FlinkS3ConsumerTest {

    public static void main(String[] args) throws Exception {
        // set up the streaming execution environment
        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setStreamTimeCharacteristic(TimeCharacteristic.EventTime);

        ParameterTool parameterTool = ParameterTool.fromArgs(args);

        // Use S3 as the backend store to store checkpoints for recovery
/      	env.setStateBackend(new FsStateBackend("s3://aws-logs-056683719894-eu-central-1/flinkTest/checkpoint"));
 //       env.setStateBackend(new FsStateBackend("hdfs://ip-172-31-9-254.eu-central-1.compute.internal:8020/flink-checkpoints"));

        // Perform checkpoint every 1min
        env.enableCheckpointing(10*6000L);
        env.setStreamTimeCharacteristic(TimeCharacteristic.EventTime);
        env.getCheckpointConfig().setCheckpointingMode(CheckpointingMode.EXACTLY_ONCE);

        // 表示一旦Flink程序被cancel后，会保留checkpoint数据，以便根据实际需要恢复到指定的checkpoint
        env.getCheckpointConfig().enableExternalizedCheckpoints(CheckpointConfig.ExternalizedCheckpointCleanup.RETAIN_ON_CANCELLATION);
        // 确保检查点之间有至少500 ms的间隔【checkpoint最小间隔】
        env.getCheckpointConfig().setMinPauseBetweenCheckpoints(500);
// 检查点必须在一分钟内完成，或者被丢弃【checkpoint的超时时间】
        env.getCheckpointConfig().setCheckpointTimeout(60000);
        env.getCheckpointConfig().setMaxConcurrentCheckpoints(1);

        // checkpoint错误，是否任务会失败
        env.getCheckpointConfig().setFailOnCheckpointingErrors(true);
//        env.setRestartStrategy(RestartStrategies.failureRateRestart(20, org.apache.flink.api.common.time.Time.seconds(100), org.apache.flink.api.common.time.Time.seconds(10)))


        Properties properties = new Properties();
//目标环境的IP地址和端口号
        properties.setProperty("bootstrap.servers", "172.31.11.79:9092");//kafka
        properties.setProperty("zookeeper.connect", "172.31.11.79:2181");//kafka
//kafka版本0.8需要；
        properties.setProperty("group.id", "s3test1"); //group.id
        properties.setProperty("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        properties.setProperty("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
//第一种方式：
//这里很重要，填写hdfs-site.xml和core-site.xml的路径，可以把目标环境上的hadoop的这两个配置拉到本地来，这个是我放在了项目的resources目录下。
        //       properties.setProperty("fs.hdfs.hadoopconf", "E:\\Ali-Code\\cn-smart\\cn-components\\cn-flink\\src\\main\\resources");
//第二种方式：
        properties.setProperty("fs.default-scheme","hdfs://ip-172-31-9-254.eu-central-1.compute.internal:8020");

//        properties.put("enable.auto.commit",true);
//        properties.put("auto.commit.interval.ms", 1000);
//        properties.put("auto.offset.reset", "latest");
        properties.put("flink.partition-discovery.interval-millis", "30000");

//根据不同的版本new不同的消费对象；
        FlinkKafkaConsumer09<String> flinkKafkaConsumer09 = new FlinkKafkaConsumer09<>("s3test", new SimpleStringSchema(),properties);
        // 默认读取上次保存的offset信息, 如果是应用第一次启动，读取不到上次的offset信息，则会根据这个参数auto.offset.reset的值来进行消费数据
        flinkKafkaConsumer09.setStartFromGroupOffsets();
        // 表示在checkpoint的时候提交offset, 此时，kafka(properties设置的)中的自动提交机制就会被忽略
        flinkKafkaConsumer09.setCommitOffsetsOnCheckpoints(true);
        DataStream<String> keyedStream = env.addSource(flinkKafkaConsumer09)
                .name("kafka source").uid("kafkaSource-id");

//        keyedStream.print();
        // execute program

        //streamfilesink s3


//        String s3SinkPath="s3a://aws-logs-056683719894-eu-central-1/flinkTest/data/";
        String s3SinkPath="hdfs://ip-172-31-9-254.eu-central-1.compute.internal:8020/flinkTest/data/";
        final StreamingFileSink<String> streamSink = StreamingFileSink
                .forRowFormat(new Path(s3SinkPath), new SimpleStringEncoder<String>("UTF-8"))
                .withBucketAssigner(new DateTimeBucketAssigner<>("yyyy-MM-dd--HH"))
                .withOutputFileConfig(new OutputFileConfig("part", ".log"))
                .withBucketCheckInterval(TimeUnit.MINUTES.toMinutes(10))
                .withRollingPolicy(
                        DefaultRollingPolicy.builder()
                                .withRolloverInterval(TimeUnit.MINUTES.toMillis(3))
                                .withInactivityInterval(TimeUnit.MINUTES.toMillis(1))
                                .withMaxPartSize(128 * 1024 * 1024)
                                .build())
                .build();

        keyedStream.addSink(streamSink);
        env.execute("flinkTest");



    }
}
