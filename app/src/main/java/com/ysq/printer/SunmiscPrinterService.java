package com.ysq.printer;

import android.app.IntentService;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.os.IBinder;

import com.google.zxing.BarcodeFormat;

import java.util.concurrent.CountDownLatch;

import woyou.aidlservice.jiuiv5.IWoyouService;

public class SunmiscPrinterService extends IntentService {

    /**
     * 意图类型传值键，0：启动打印机，1：写入打印机，2：断开打印机，3：打印文字
     * ，4：打印条码，5：打印二维码，6：打印延迟，7：打印机走纸
     */
    public static final String EXTRA_TYPE = "EXTRA_TYPE";

    /**
     * 打印文字内容传值键
     */
    public static final String EXTRA_TEXT = "EXTRA_TEXT";

    /**
     * 打印文字是否居中传值键
     */
    public static final String EXTRA_CENTER = "EXTRA_CENTER";

    /**
     * 打印文字是否加大传值键
     */
    public static final String EXTRA_LARGE = "EXTRA_LARGE";

    /**
     * 打印延迟传值键
     */
    public static final String EXTRA_DELAY = "EXTRA_DELAY";

    //用来控制线程，将异步转成同步
    private CountDownLatch mCountDownLatch;
    //银商打印对象
    private IWoyouService mPrinter;


    private static final byte[] CLEAR_FORMAT = {0x1b, 0x40}; //复位打印机
    private static final byte[] COMMAND_UNSPECIFIED = {0x1B, 0x21, 0x08};//老代码，我也不知道有什么用
    private static final byte[] COMMAND_DOUBLE_HEIGHT = {0x1d, 0x21, 0x01}; //高加倍
    /**
     * 设别服务连接桥
     */
    private ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder serviceBinder) {
            mPrinter = IWoyouService.Stub.asInterface(serviceBinder);
            mCountDownLatch.countDown();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
        }
    };


    public SunmiscPrinterService() {
        super(SunmiscPrinterService.class.getSimpleName());
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        try {
            int intExtra = intent.getIntExtra(EXTRA_TYPE, 0);
            if (intExtra == 0) {
                init();
            } else if (intExtra == 1) {
                flushPrint();
            } else if (intExtra == 2) {
                close();
            } else if (intExtra == 3) {
                String text = intent.getStringExtra(EXTRA_TEXT);
                boolean isCenter = intent.getBooleanExtra(EXTRA_CENTER, false);
                boolean isLarge = intent.getBooleanExtra(EXTRA_LARGE, false);
                printText(text, isCenter, isLarge);
            } else if (intExtra == 4) {
                printBarCode(intent.getStringExtra(EXTRA_TEXT));
            } else if (intExtra == 5) {
                printQrcode(intent.getStringExtra(EXTRA_TEXT));
            } else if (intExtra == 6) {
                delay(intent.getIntExtra(EXTRA_DELAY, 0));
            } else if (intExtra == 7) {
                feedPaper();
            }
        } catch (Exception ignored) {
        }
    }

    /**
     * 初始化打印机
     */
    private void init() throws Exception {
        mCountDownLatch = new CountDownLatch(1);
        Intent intent = new Intent();
        intent.setPackage("woyou.aidlservice.jiuiv5");
        intent.setAction("woyou.aidlservice.jiuiv5.IWoyouService");
        bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE);
        mCountDownLatch.await();
    }


    /**
     * 写入打印机，某些打印机在startPrint以后需要flushPrint操作
     */
    private void flushPrint() {
    }

    /**
     * 释放打印机
     */
    private void close() {
        unbindService(mServiceConnection);
    }

    /**
     * 打印文字
     */
    private void printText(String text, boolean isCenter, boolean isLarge) throws Exception {
        mPrinter.sendRAWData(CLEAR_FORMAT, null);
        mPrinter.sendRAWData(COMMAND_UNSPECIFIED, null);
        if (isLarge) {
            mPrinter.sendRAWData(COMMAND_DOUBLE_HEIGHT, null);
        }
        mPrinter.setAlignment(isCenter ? 1 : 0, null);
        //商米打印文字时需要加\r\n
        mPrinter.printText(text + "\n", null);

    }

    /**
     * 打印条形码
     */
    private void printBarCode(String text) throws Exception {
        mPrinter.sendRAWData(CLEAR_FORMAT, null);
        mPrinter.setAlignment(1, null);
        Bitmap bitmap = BarUtils.encodeAsBitmap(text
                , BarcodeFormat.CODE_128, 380, 80);
        mPrinter.printBitmap(bitmap, null);
        mPrinter.lineWrap(1, null);
    }

    /**
     * 打印二维码
     */
    private void printQrcode(String text) throws Exception {
        mPrinter.sendRAWData(CLEAR_FORMAT, null);
        mPrinter.setAlignment(1, null);
        Bitmap bitmap = BarUtils.encodeAsBitmap(text
                , BarcodeFormat.QR_CODE, 300, 300);
        mPrinter.printBitmap(bitmap, null);
        mPrinter.lineWrap(1, null);
    }

    /**
     * 延迟
     */
    private void delay(int millisecond) throws InterruptedException {
        Thread.sleep(millisecond);
    }

    /**
     * 打印机走纸，通过调用打印两行空白文字实现
     */
    private void feedPaper() throws Exception {
        printText("\n\n", false, false);
    }
}
