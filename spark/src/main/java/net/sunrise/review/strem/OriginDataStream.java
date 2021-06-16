package net.sunrise.review.strem;

import com.alibaba.fastjson.JSONObject;
import net.sunrise.util.CommonUtil;
import net.sunrise.util.HbaseOperate;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.function.Function2;
import org.apache.spark.streaming.Durations;
import org.apache.spark.streaming.api.java.JavaInputDStream;
import org.apache.spark.streaming.api.java.JavaStreamingContext;
import org.apache.spark.streaming.kafka010.ConsumerStrategies;
import org.apache.spark.streaming.kafka010.KafkaUtils;
import org.apache.spark.streaming.kafka010.LocationStrategies;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;

/**
 * @author kuiqwang
 * @emai wqkenqingto@163.com
 * @time 2021/6/15
 * @desc 源数据kafka to hbase 流
 */
public class OriginDataStream {
    public static void main(String[] args) throws InterruptedException {
        SparkConf conf = new SparkConf().setAppName("origin-stream");
        JavaStreamingContext jsc = new JavaStreamingContext(conf, Durations.seconds(10));
        conf.set("spark.serializer", "org.apache.spark.serializer.KryoSerializer");
        HashMap<String, Object> kafkaParams = new HashMap<String, Object>();
        kafkaParams.put("bootstrap.servers", "kafka01:9092,kafka02:9092,kafka03:9092");
        kafkaParams.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        kafkaParams.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        kafkaParams.put("auto.offset.reset", "latest");
        kafkaParams.put("group.id", "origin-stream");
        HashSet<String> topicsSet = new HashSet<>();
        topicsSet.add("jllsd-mock-camera-data-all-in-one");
        JavaInputDStream<ConsumerRecord<String, String>> oringinStream = KafkaUtils.createDirectStream(jsc, LocationStrategies.PreferConsistent(), ConsumerStrategies.<String, String>Subscribe(topicsSet, kafkaParams));
        Function2<String, String, String> fun2 = (String s1, String s2) -> {
            return "";
        };
        HbaseOperate operate = new HbaseOperate();
        oringinStream.mapPartitions(mesasges -> {
                    while (mesasges.hasNext()) {
                        ConsumerRecord<String, String> msg = mesasges.next();
                        String val = msg.value();
                        JSONObject jobj = JSONObject.parseObject(val);
                        String time = jobj.getString("time");
                        String camera = jobj.getString("camera");
                        long timestamp = CommonUtil.formatStringToDateLong(time);
                        String row = camera + "_" + timestamp;
                        operate.addRow("test-table", row, "info", "origin", val);
                    }
                    return mesasges;
                }
        ).print();
        jsc.start();
        jsc.awaitTermination();

    }
}
