package net.sunrise.review.operator;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.Function2;
import scala.Tuple2;

import java.util.Arrays;

/**
 * @author kuiqwang
 * @emai wqkenqingto@163.com
 * @time 2021/6/1
 * @desc spark 算子回顾
 */
public class SparkOperatorReview {
    static JavaSparkContext jsc;

    static {
        SparkConf conf = new SparkConf().setAppName("operator review");
        jsc = new JavaSparkContext(conf);
    }

    /**
     * transform 算子 map
     */
    public static void map(JavaRDD<String> rdd) {
        rdd.map(s -> {
            return s + " " + "test";
        }).collect().forEach(s -> {
            System.out.println(s);
        });
    }

    public static void flatMap(JavaRDD<String> rdd) {
        rdd.flatMap(s -> {
            return Arrays.stream(s.split("\n")).iterator();
        }).foreach(s -> {
            System.out.println(s);
        });
    }

    /**
     * @desc: union算子与reduceBykey的结合
     * @test: path=/Users/kuiqwang/Desktop/temp_key/spark/union1 path2=/Users/kuiqwang/Desktop/temp_key/spark/union2
     * @date: 2021/6/7
     **/
    public static void union(JavaRDD<String> rdd1, JavaRDD<String> rdd2) {
        JavaRDD<String> rdd3 = rdd1.union(rdd2);

        Function2<String, String, String> reduceFun = (String val1, String val2) -> {
            return val1 + "|" + val2;
        };
        rdd3.mapToPair(s -> {
            String[] ss = s.split("\\s");
            return new Tuple2<>(ss[0], ss[1]);
        }).countByKey().forEach((s, val) -> {
            System.out.print(s);
            System.out.print(" ");
            System.out.println(val);
        });

    }

    public static void join(JavaRDD<String> rdd1, JavaRDD<String> rdd2) {
        rdd1.mapToPair(s -> {
            String[] ss = s.split("\\s");
            return new Tuple2<>(ss[0], ss[1]);
        }).join(rdd2.mapToPair(s -> {
            String[] ss = s.split("\\s");
            return new Tuple2<>(ss[0], ss[1]);
        })).collect().forEach(s -> {
            System.out.print(s._1);
            System.out.print(" ");
            System.out.println(s._2);
        });

    }

    public static void main(String[] args) {
        String path = "/Users/kuiqwang/Desktop/temp_key/spark/union1";
        String path2 = "/Users/kuiqwang/Desktop/temp_key/spark/union2";
        JavaRDD<String> textRDD = jsc.textFile(path);
        JavaRDD<String> textRDD1 = jsc.textFile(path2);
//        map(textRDD);
//        flatMap(textRDD);
        union(textRDD, textRDD1);
//        join(textRDD, textRDD1);
    }
}
