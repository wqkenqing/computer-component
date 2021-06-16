package net.sunrise.test;

import net.sunrise.util.HbaseOperate;

import java.io.IOException;

/**
 * @author kuiqwang
 * @emai wqkenqingto@163.com
 * @time 2021/6/15
 * @desc
 */
public class HbaseTest {
    public static void main(String[] args) throws IOException {
        HbaseOperate operate = new HbaseOperate();
        String columns[] = {"info"};

//        operate.createTable("test-table", columns);
        operate.addRow("test-table", "test", "info", "origin", "test");
    }
}
