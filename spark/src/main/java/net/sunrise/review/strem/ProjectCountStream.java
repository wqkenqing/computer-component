package net.sunrise.review.strem;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.Optional;
import org.apache.spark.api.java.function.Function2;
import org.apache.spark.streaming.Durations;
import org.apache.spark.streaming.api.java.JavaInputDStream;
import org.apache.spark.streaming.api.java.JavaStreamingContext;
import org.apache.spark.streaming.kafka010.ConsumerStrategies;
import org.apache.spark.streaming.kafka010.KafkaUtils;
import org.apache.spark.streaming.kafka010.LocationStrategies;
import scala.Tuple2;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

/**
 * @author kuiqwang
 * @emai wqkenqingto@163.com
 * @time 2021/6/10
 * @desc updateStateByKey实现 InputStream Kafka
 */
@Slf4j
public class ProjectCountStream {
    public static void main(String[] args) throws InterruptedException {
        SparkConf conf = new SparkConf().setAppName("projectCountStream").setMaster("local[1]");
        conf.set("spark.serializer", "org.apache.spark.serializer.KryoSerializer");
        JavaStreamingContext jssc = new JavaStreamingContext(conf, Durations.seconds(10));
        jssc.sparkContext().setLogLevel("DEBUG");
        jssc.checkpoint("/Users/kuiqwang/Desktop/spark_tmp/checkpoint");
        HashMap<String, Object> kafkaParams = new HashMap<String, Object>();
        kafkaParams.put("bootstrap.servers", "kafka01:9092,kafka02:9092,kafka03:9092");
        kafkaParams.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        kafkaParams.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        kafkaParams.put("auto.offset.reset", "earliest");
        kafkaParams.put("group.id", "flume-new5");
        HashSet<String> topicsSet = new HashSet<>();
        topicsSet.add("jllsd-mock-camera-data-all-in-one");

        JavaInputDStream<ConsumerRecord<String, String>> projectCountDStream = KafkaUtils.createDirectStream(jssc, LocationStrategies.PreferConsistent(), ConsumerStrategies.<String, String>Subscribe(topicsSet, kafkaParams));
        Function2<List<Integer>, Optional<Integer>, Optional<Integer>> funn2 = (List<Integer> list, Optional<Integer> val) -> {
            Integer updateValue = 0;
            if (val.isPresent()) {
                updateValue = val.get();
            }
            for (Integer v : list) {
                updateValue += v;
            }
            return Optional.of(updateValue);
        };
        projectCountDStream.mapPartitionsToPair(message -> {
            List<Tuple2<String, Integer>> tlist = new ArrayList();
            while (message.hasNext()) {
                ConsumerRecord<String, String> records = message.next();
                tlist.add(new Tuple2(records.key(), 1));
            }
            return tlist.iterator();
        }).updateStateByKey(funn2).print();

        jssc.start();
        jssc.awaitTermination();

    }
}
