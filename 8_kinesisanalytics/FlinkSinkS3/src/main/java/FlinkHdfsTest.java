import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.streaming.api.TimeCharacteristic;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.connectors.fs.bucketing.BucketingSink;
import org.apache.flink.streaming.connectors.fs.bucketing.DateTimeBucketer;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer09;

import java.util.Properties;



public class FlinkHdfsTest {

    public static void main(String[] args) throws Exception {
        // set up the streaming execution environment
        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        env.enableCheckpointing(5000);
        env.setStreamTimeCharacteristic(TimeCharacteristic.EventTime);
        Properties properties = new Properties();
//目标环境的IP地址和端口号
        properties.setProperty("bootstrap.servers", "172.31.11.79:9092");//kafka
//kafka版本0.8需要；
//        properties.setProperty("zookeeper.connect", "192.168.0.1:2181");//zookeepe
        properties.setProperty("group.id", "test-consumer-group"); //group.id
//第一种方式：
//这里很重要，填写hdfs-site.xml和core-site.xml的路径，可以把目标环境上的hadoop的这两个配置拉到本地来，这个是我放在了项目的resources目录下。
        //       properties.setProperty("fs.hdfs.hadoopconf", "E:\\Ali-Code\\cn-smart\\cn-components\\cn-flink\\src\\main\\resources");
//第二种方式：
        properties.setProperty("fs.default-scheme","hdfs://ip-172-31-9-254.eu-central-1.compute.internal:8020");

//根据不同的版本new不同的消费对象；
        FlinkKafkaConsumer09<String> flinkKafkaConsumer09 = new FlinkKafkaConsumer09<>("my-test1", new SimpleStringSchema(),properties);
//        flinkKafkaConsumer010.assignTimestampsAndWatermarks(new CustomWatermarkEmitter());
        DataStream<String> keyedStream = env.addSource(flinkKafkaConsumer09);
        keyedStream.print();
        // execute program

        System.out.println("*********** hdfs ***********************");
        BucketingSink<String> bucketingSink = new BucketingSink<>("hdfs://ip-172-31-9-254.eu-central-1.compute.internal:8020/test/kafka"); //hdfs上的路径

        bucketingSink.setBatchSize(1024 * 1024 )
                .setBatchRolloverInterval(2000)
                .setBucketer(new DateTimeBucketer<String>("yyyy-MM-dd"));


        keyedStream.addSink(bucketingSink);

        env.execute("test");

        //streamfilesink s3


    }
}
