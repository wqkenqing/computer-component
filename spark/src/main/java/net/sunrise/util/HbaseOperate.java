package net.sunrise.util;


import lombok.extern.slf4j.Slf4j;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.HColumnDescriptor;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.filter.FilterList;
import org.apache.hadoop.hbase.util.Bytes;
import org.ho.yaml.Yaml;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalTime;
import java.util.*;

/**
 * @author wqkenqing
 * @emai wqkenqingto@163.com
 * @time 2021年 6月15日
 * @desc 更新hbase api 0.98和1.0之后有较大差别
 */

@Slf4j
public class HbaseOperate implements Serializable {
    //1. configruation 配置  2.所有表 3.创建表 4.删除表 5.修改表 6.添加信息 7.读取信息 8.删除信息
    private static Configuration conf = null;
    private static Admin hAdmin = null;
    public static Table table = null;
    private static Properties properties;

    static {
        properties = CommonUtil.initConfig("hbase.properties");
        conf = HBaseConfiguration.create();
        //1. 设置zookeeper集群 2.端口号 3.hdfs上的hbase地址 4.nameservices
        for (Object k : properties.keySet()) {
            String key = String.valueOf(k);
            conf.set(key, String.valueOf(properties.getProperty(key)));
        }
        //            hAdmin = new HBaseAdmin();
    }

    public static Table getTable(String tableName) {
        table = null;
        Connection connection = null;

        try {
            connection = ConnectionFactory.createConnection(getConf());
            table = connection.getTable(TableName.valueOf(tableName.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            log.error("htable获取失败");
        }
        return table;
    }


    public static Configuration getConf() {
        return conf;
    }

    /**
     * 罗列所有表
     */
    public void listAllTables() {
        Connection connection = null;
        try {
            connection = ConnectionFactory.createConnection(conf);
            Admin hAdmin = connection.getAdmin();
            log.info("以下是所有的表...");
            Arrays.asList(hAdmin.listTableNames()).forEach(t -> {
                System.out.println(t.toString());
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 创建表,并且要有表的描述
     **/
    public void createTable(String tableName, String[] columnFamilys) throws IOException {
        hAdmin = ConnectionFactory.createConnection(conf).getAdmin();
        if (hAdmin.tableExists(TableName.valueOf(tableName.getBytes(StandardCharsets.UTF_8)))) {
            log.info("表已存在,请不要重复创建..");
            return;
        }
        HTableDescriptor desc = new HTableDescriptor(TableName.valueOf(tableName.getBytes(StandardCharsets.UTF_8)));
        Arrays.asList(columnFamilys).forEach(c -> {
            desc.addFamily(new HColumnDescriptor(c.getBytes()));
        });
        try {

            if (hAdmin == null) {

                hAdmin = ConnectionFactory.createConnection(conf).getAdmin();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        hAdmin.createTable(desc);
        log.info(tableName + ":创建成功....");
    }

    /**
     * 删除表
     */
    public void deleteTable(String tableName) throws IOException {
        if (hAdmin == null) {
            hAdmin = ConnectionFactory.createConnection(conf).getAdmin();
        }
        hAdmin.disableTable(TableName.valueOf(tableName));
        hAdmin.deleteTable(TableName.valueOf(tableName));
        log.info(tableName + ":删除成功....");

    }

    public void addRow(String tableName, String row, String columnFamily, String column, String val) {
        try {
            Table table = ConnectionFactory.createConnection(conf).getTable(TableName.valueOf(tableName.getBytes(StandardCharsets.UTF_8)));
            Put put = new Put(Bytes.toBytes(row));
            put.addColumn(Bytes.toBytes(columnFamily), Bytes.toBytes(column), Bytes.toBytes(val));
            table.put(put);
            table.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 多版本version Get
     */
    public void getRowShow(String tableName, String row) {
        try {
            Table table = ConnectionFactory.createConnection().getTable(TableName.valueOf(tableName.getBytes(StandardCharsets.UTF_8)));
            Get get = new Get(Bytes.toBytes(row));

            get.setMaxVersions(3);
            Result res = table.get(get);
            res.listCells().forEach(r -> {

                System.out.println(Bytes.toString(r.getFamilyArray()));
                System.out.println(Bytes.toString(r.getQualifierArray()));
                System.out.println(Bytes.toString(r.getValueArray()));
                System.out.println(r.getTimestamp());

            });
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    public String getRow(String tableName, String row) {
        String result = "";

        try {
            Table table = ConnectionFactory.createConnection().getTable(TableName.valueOf(tableName.getBytes(StandardCharsets.UTF_8)));
            Get get = new Get(Bytes.toBytes(row));

            Result res = table.get(get);

            if (res == null) {
                return "";
            }
            result = Bytes.toString(res.listCells().get(0).getValueArray());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * scan
     */
    public void scanShow(String tableName) throws IOException {
        Table table = ConnectionFactory.createConnection().getTable(TableName.valueOf(tableName.getBytes(StandardCharsets.UTF_8)));
        Scan scan = new Scan();
        ResultScanner scanner = table.getScanner(scan);
        scan.setMaxVersions(3);


    }

    public void deleteRow(String tableName, String row) {
        try {
            Table table = ConnectionFactory.createConnection().getTable(TableName.valueOf(tableName.getBytes(StandardCharsets.UTF_8)));
            table.delete(new Delete(row.getBytes()));
            table.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * 通过rowkeyList获得,在大批量操作的时候能有效率的提升
     */
    public Result[] getPutList(String tableName, List<Get> rlist) throws IOException {

        Table table = ConnectionFactory.createConnection().getTable(TableName.valueOf(tableName.getBytes(StandardCharsets.UTF_8)));
        LocalTime start = LocalTime.now();
        Result[] rset = table.get(rlist);
        LocalTime stop = LocalTime.now();
        Duration duration = Duration.between(start, stop);
        log.info("耗时:" + duration.toMillis());
        return rset;
    }


    /**
     * 批量写入
     */
    public void insertList(String tname, List<Put> putList) throws IOException {
        Table table = ConnectionFactory.createConnection().getTable(TableName.valueOf(tname.getBytes(StandardCharsets.UTF_8)));
        try {
            table.put(putList);
            table.close();
        } catch (Exception e) {
            log.warn("批量写入失败");
            return;
        }
        log.info("批量写入成功...");
    }


    public List<Result> scanByFilter(String tableName, FilterList filterList) throws IOException {

        Table table = ConnectionFactory.createConnection().getTable(TableName.valueOf(tableName.getBytes(StandardCharsets.UTF_8)));
        Scan scan = new Scan();
        scan.setFilter(filterList);
        scan.setCaching(10000);
//        scan.setBatch(1000);
        ResultScanner scanner = table.getScanner(scan);
        List<Result> results = new ArrayList<>();
        Result res = null;
        int count = 1;
        while ((res = scanner.next()) != null) {
            results.add(res);
            count++;
            if (count == 100000) {
                return results;
            }
        }
//        scanner.iterator().forEachRemaining(result -> {
//            results.add(result);
//        });
//        return results;
        return results;
    }


    public static void main(String[] args) throws FileNotFoundException {
        HbaseOperate operate = new HbaseOperate();
        HbaseOperate.initConfig("", new Configuration());
    }

    /**
     * Hbase单条记录添加
     *
     * @param put
     * @param tableName
     * @return
     * @throws
     * @author wqkenqing
     * @date 2019-07-23
     **/
    public void addPut(Put put, String tableName) {
        Table table = null;
        try {
            table = ConnectionFactory.createConnection(conf).getTable(TableName.valueOf(tableName.getBytes(StandardCharsets.UTF_8)));
            table.put(put);
            table.close();
        } catch (Exception e) {
            log.error("[{}]表添加记录失败", tableName);
            e.printStackTrace();
            return;
        }
        log.info("[{}]表添加记录成功", tableName);
    }

    /**
     * 批量提交putlist
     *
     * @param plist
     * @param tableName
     * @return
     * @throws
     * @author wqkenqing
     * @date 2019-07-23
     **/
    public void addPutList(List<Put> plist, String tableName) {
        try {
            getTable(tableName);
            if (table == null) {
                table = getTable(tableName);
            }
            table.put(plist);
            table.close();
            log.info("[{}]成功上传[{}]条数据", tableName, plist.size());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public boolean tableIsExist(String tname) {

        boolean res = false;
        try {
            Admin hAdmin = ConnectionFactory.createConnection(conf).getAdmin();

            res = hAdmin.tableExists(TableName.valueOf(tname.getBytes(StandardCharsets.UTF_8)));
        } catch (IOException e) {
            e.printStackTrace();

        }
        return res;
    }

    public static Configuration initConfig(String path, Configuration conf) throws FileNotFoundException {
        path = HbaseOperate.class.getResource("/hbase.properties").getPath();
        System.out.println(path);
        File dumpFile = new File(path);
        Map prop = Yaml.loadType(dumpFile, HashMap.class);
        Map<String, Object> propn = prop;
        for (String key : propn.keySet()) {
            conf.set(key, (String) propn.get(key));
        }
        return conf;
    }


}
