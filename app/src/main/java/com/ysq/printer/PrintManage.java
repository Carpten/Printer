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
    private static final int DELAY = 10000;

    private Printable mPrintable;

    public PrintManage(Printable printable) {
        mPrintable = printable;
    }

    public void printDetail() {
        mPrintable.init();
        for (int i = 0; i < COUNT; i++) {
            mPrintable.printText("俊隆实业", true, true);
            mPrintable.printText(getSplicedText(
                    ""
                    , "*********  付款凭证   *********"
                    , ""
                    , "门 店 名：俊隆实业"
                    , "收 银 员：测试商户"
                    , "订单编号："
                    , "201806061200110744500240"
                    , "支付方式：支付宝"
                    , "支付状态：支付状态"
                    , "支付平台：收银台"
                    , "支付时间：2018-06-06 12:00:14"
                    , "订单金额：0.01"
                    , "优惠金额：0.00"
                    , "顾客实付："), false, false);
            mPrintable.printText("RMB:0.01", true, true);
            mPrintable.printText(getSplicedText(
                    ""
                    , "签　　名：____________"
                    , ""
                    , "备　　注："
                    , ""
                    , "**********   退款码   **********"
            ), false, false);
            mPrintable.printBarcode("201806041806071826001110");
            mPrintable.printText(getSplicedText(
                    ""
                    , "微信扫码关注，享受优惠权益"
                    , ""), true, false);
            mPrintable.printQrcode("http://weixin.qq.com/q/02CdanA5focHh1faGtNr1M");
            mPrintable.feedPaper();
            mPrintable.flushPrint();
            mPrintable.delay(DELAY);
        }
        mPrintable.close();
    }


    /**
     * 将字符串用\n拼接起来
     *
     * @param texts
     * @return
     */
    private String getSplicedText(String... texts) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < texts.length; i++) {
            sb.append(texts[i]);
            if (i < texts.length - 1) {
                sb.append("\n");
            }
        }
        return sb.toString();
    }
}
