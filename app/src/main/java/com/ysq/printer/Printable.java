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
    void printTextLeft(String text);

    /**
     * 普通文本居中
     */
    void printTextCenter(String text);

    /**
     * 高加倍文本居中
     */
    void printTextHeightDoubleCenter(String text);

    /**
     * 打印条形码
     */
    void printBarcode(String orderNumberStr);

    /**
     * 打印二维码
     */
    void printQrcode(String text);


    /**
     * 开始打印
     */
    void startPrint();

    /**
     * 关闭打印机
     */
    void close();
}
