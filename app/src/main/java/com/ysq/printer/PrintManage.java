package com.ysq.printer;

/**
 * <pre>
 * author : 杨水强
 * time   : 2018/05/31
 * desc   : 这个类是具体业务层，使用Printable实现具体打印内容
 * version: 1.0
 * </pre>
 */
public class PrintManage {

    private static final int COUNT = 1;
    private static final int DELAY = 2000;

    private Printable mPrintable;

    public PrintManage(Printable printable) {
        mPrintable = printable;
    }

    public void printDetail() {
        mPrintable.init();
        for (int i = 0; i < COUNT; i++) {
            mPrintable.printText("我是标题\r\n", true, true);
            mPrintable.printText("我是内容\r\n", false, false);
            mPrintable.printText("我是内容\r\n", false, false);
            mPrintable.printText("我是内容\r\n", false, false);
            mPrintable.printBarcode("23267419182847");
            mPrintable.printQrcode("2326741918284");
            mPrintable.flushPrint();
            mPrintable.delay(DELAY);
        }
        mPrintable.close();
    }
}
