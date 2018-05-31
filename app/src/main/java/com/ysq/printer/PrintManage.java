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
        mPrintable.printTextCenter("");
        mPrintable.printTextCenter("");
        mPrintable.printTextCenter("");
        mPrintable.printTextCenter("");
        mPrintable.printTextCenter("");
        mPrintable.printTextCenter("");
        mPrintable.startPrint();
        mPrintable.close();
    }
}
