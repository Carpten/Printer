package com.ysq.printer;

/**
 * <pre>
 * author : 杨水强
 * time   : 2018/05/31
 * desc   :
 * version: 1.0
 * </pre>
 */
public class PrintManage {

    private Printable mPrintable;

    public PrintManage(Printable printable) {
        mPrintable = printable;
    }

    public void printDetail() {
        mPrintable.init();
        mPrintable.printTextCenter("我");
        mPrintable.printTextCenter("是");
        mPrintable.printTextCenter("打");
        mPrintable.printTextCenter("印");
        mPrintable.printTextCenter("机");
        mPrintable.startPrint();
        mPrintable.close();
    }
}
