package net.sunrise.review.strem;

import lombok.extern.slf4j.Slf4j;
import org.apache.spark.SparkConf;
import org.apache.spark.streaming.Durations;
import org.apache.spark.streaming.api.java.JavaInputDStream;
import org.apache.spark.streaming.api.java.JavaStreamingContext;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author kuiqwang
 * @emai wqkenqingto@163.com
 * @time 2021/6/10
 * @desc Socket输入流处理
 */
@Slf4j
public class SocketOperateStream {
    public static void main(String[] args) throws InterruptedException {
        String host = args[0];
        String port = args[1];
        SparkConf conf = new SparkConf().setAppName("socketStream").setMaster("local[4]");
//        conf.set("spark.serializer", "org.apache.spark.serializer.KryoSerializer");
        JavaStreamingContext jscc = new JavaStreamingContext(conf, Durations.seconds(5));
        JavaInputDStream<String> socketStream = jscc.socketTextStream(host, Integer.valueOf(port));
        socketStream.mapPartitions(s -> {
            List<String> list = new ArrayList<>();
            while (s.hasNext()) {
                list.add(s.next());
            }
            log.info("test");
            return list.iterator();
        }).print();
        jscc.start();
        jscc.awaitTermination();
    }
}
