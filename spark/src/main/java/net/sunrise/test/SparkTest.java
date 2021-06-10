package net.sunrise.test;

import avro.shaded.com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.Function2;
import scala.Tuple2;


/**
 * @author kuiqwang
 * @emai wqkenqingto@163.com
 * @time 2021/5/31
 * @desc
 */
@Slf4j
public class SparkTest {


    public static void main(String[] args) {
        SparkConf conf = new SparkConf().setAppName("test-Spark").setMaster("local[2]");
        JavaSparkContext jsc = new JavaSparkContext(conf);
        String path = "/Users/kuiqwang/Desktop/out/animal.txt";
        JavaRDD<String> tempRDD = jsc.textFile(path);

        JavaPairRDD<String, String> tpairRDD = tempRDD.mapToPair(row -> {
            String rows[] = row.split("\t");
            return new Tuple2<String, String>(rows[0], rows[1]);
        });

        JavaPairRDD<String, String> javaPairRDD = jsc.parallelizePairs(Lists.<Tuple2<String, String>>newArrayList(
                new Tuple2<String, String>("cat", "3"),
                new Tuple2<String, String>("dog", "33"),
                new Tuple2<String, String>("cat", "16"),
                new Tuple2<String, String>("tiger", "66")));

//        /** reduce Function2函数的实现  */
//        Function2<String, String, Integer> reduceFunction = (String one, String two) -> {
//            return Integer.valueOf(one) + Integer.valueOf(two);
//        };
//        tpairRDD.reduceByKey(reduceFunction).collect().forEach(s->{
//            System.out.println(s);
//        });

        /** aggreateByKey  Function2函数 的实现 */
        Function2<String, String, String> aseq = (String one, String two) -> {
            log.info("as one " + one + "," + "  as two :" + two);
            return String.valueOf(Math.addExact(Integer.valueOf(one), Integer.valueOf(two)));
        };

        Function2<String, String, String> acombiner = (String one, String two) -> {
            log.info("进入了combiner");
            log.info("combiner one " + one + "," + "  combiner two :" + two);
            return String.valueOf(Math.addExact(Integer.valueOf(one), Integer.valueOf(two)));
        };
        Function2<Integer, String, Integer> aseq1 = (Integer one, String two) -> {
            log.info("as one " + one + "," + "  as two :" + two);
            return Math.addExact(Integer.valueOf(one), Integer.valueOf(two));
        };

        Function2<Integer, Integer, Integer> acombiner1 = (Integer one, Integer two) -> {
            log.info("进入了combiner");
            log.info("combiner one " + one + "," + "  combiner two :" + two);
            return one + two;
        };




        tpairRDD.aggregateByKey(0, aseq1, acombiner1).collect().forEach(s -> {
            System.out.println(s);
        });




    }
}
