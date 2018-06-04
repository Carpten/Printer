package com.ysq.printer;

/**
 * <pre>
 * author : 杨水强
 * time   : 2018/05/31
 * desc   : 打印机接口，各类打印机需要适配该接口
 * version: 1.0
 * </pre>
 */
public interface Printable {

    /**
     * 初始化打印机
     */
    void init();

    /**
     * 普通文本居左
     */
    void printText(String text, boolean center, boolean largeSize);

    /**
     * 打印条形码
     */
    void printBarcode(String text);

    /**
     * 打印二维码
     */
    void printQrcode(String text);


    /**
     * 写入打印
     */
    void flushPrint();


    /**
     * 延迟打印
     *
     * @param millisecond 毫秒数
     */
    void delay(int millisecond);

    /**
     * 关闭打印机
     */
    void close();
}
