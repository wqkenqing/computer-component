package net.sunrise.test;

import avro.shaded.com.google.common.collect.Lists;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.Function2;
import org.apache.spark.api.java.function.VoidFunction;
import scala.Tuple2;

/**
 * @author kuiqwang
 * @emai wqkenqingto@163.com
 * @time 2021/5/31
 * @desc
 */
public class SparkTest2 {


    public static void main(String[] args) {
        SparkConf conf = new SparkConf().setMaster("local").setAppName("TestSpark");
        JavaSparkContext sc = new JavaSparkContext(conf);
        JavaPairRDD<String, Integer> javaPairRDD = sc.parallelizePairs(Lists.<Tuple2<String, Integer>>newArrayList(
                new Tuple2<String, Integer>("cat", 3),
                new Tuple2<String, Integer>("dog", 33),
                new Tuple2<String, Integer>("cat", 16),
                new Tuple2<String, Integer>("tiger", 66)),
                2);

        // 打印样例数据
        javaPairRDD.foreach(new VoidFunction<Tuple2<String, Integer>>() {
            public void call(Tuple2<String, Integer> stringIntegerTuple2) throws Exception {
                System.out.println("样例数据>>>>>>>" + stringIntegerTuple2);
            }
        });

        Function2<Integer, Integer, Integer> fun1 = (Integer one, Integer two) -> {
            System.out.println("seqOp>>>>>  参数One：" + one + "--参数Two:" + two);
            return Math.max(one, two);
        };
        Function2<Integer, Integer, Integer> fun2 = (Integer one, Integer two) -> {
            System.out.println("combOp>>>>>  参数One：" + one + "--参数Two:" + two);
            return one + two;
        };
        javaPairRDD.aggregateByKey(14, fun1, fun2).collect().forEach(s -> {
            System.out.println(s);
        });
//
//        JavaPairRDD<String,Integer> javaPairRDD1 = javaPairRDD.aggregateByKey(14, new Function2<Integer, Integer, Integer>() {
//            public Integer call(Integer v1, Integer v2) throws Exception {
//                System.out.println("seqOp>>>>>  参数One："+v1+"--参数Two:"+v2);
//                return Math.max(v1,v2);
//            }
//        }, new Function2<Integer, Integer, Integer>() {
//            public Integer call(Integer v1, Integer v2) throws Exception {
//                System.out.println("combOp>>>>>  参数One："+v1+"--参数Two:"+v2);
//                return v1+v2;
//            }
//        });
//
//        // 打印结果数据
//        javaPairRDD1.foreach(new VoidFunction<Tuple2<String, Integer>>() {
//            public void call(Tuple2<String, Integer> stringIntegerTuple2) throws Exception {
//                System.out.println("结果数据>>>>>>>" + stringIntegerTuple2);
//            }
//        });


    }
}
