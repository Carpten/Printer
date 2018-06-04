package com.ysq.printer;

import android.app.IntentService;
import android.content.Intent;
import android.graphics.Bitmap;

import com.google.zxing.BarcodeFormat;
import com.ums.upos.sdk.printer.FontConfig;
import com.ums.upos.sdk.printer.OnPrintResultListener;
import com.ums.upos.sdk.printer.PrinterManager;
import com.ums.upos.sdk.system.BaseSystemManager;
import com.ums.upos.sdk.system.OnServiceStatusListener;

import java.util.concurrent.CountDownLatch;

public class ChinaumsPrinterService extends IntentService {

    /**
     * 意图类型传值键，0：启动打印机，1：写入打印机，2：断开打印机，3：打印文字
     * ，4：打印条码，5：打印二维码，6：打印延迟
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
    private PrinterManager mPrinter;


    public ChinaumsPrinterService() {
        super(ChinaumsPrinterService.class.getSimpleName());
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
                printText(intent);
            } else if (intExtra == 4) {
                printBarCode(intent);
            } else if (intExtra == 5) {
                printQrcode(intent);
            } else if (intExtra == 6) {
                delay(intent);
            }
        } catch (Exception ignored) {
        }
    }

    /**
     * 初始化打印机
     */
    private void init() throws Exception {
        mCountDownLatch = new CountDownLatch(1);
        BaseSystemManager.getInstance().deviceServiceLogin(getApplicationContext()
                , null, "99999998", new OnServiceStatusListener() {
                    @Override
                    public void onStatus(int i) {
                        if (0 == i || 2 == i || 100 == i) {
                            try {
                                mPrinter = new PrinterManager();
                                mPrinter.initPrinter();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                        mCountDownLatch.countDown();
                    }
                });
        mCountDownLatch.await();
    }


    /**
     * 写入打印机，某些打印机在startPrint以后需要flushPrint操作
     */
    private void flushPrint() throws Exception {
        mCountDownLatch = new CountDownLatch(1);
        mPrinter.startPrint(new OnPrintResultListener() {
            @Override
            public void onPrintResult(int i) {
                mCountDownLatch.countDown();
            }
        });
        mCountDownLatch.await();
    }

    /**
     * 释放打印机
     */
    private void close() throws Exception {
        BaseSystemManager.getInstance().deviceServiceLogout();
    }


    /**
     * 打印文字
     */
    private void printText(Intent intent) throws Exception {
        mPrinter.setPrnText(intent.getStringExtra(EXTRA_TEXT), new FontConfig());
    }

    /**
     * 打印条形码
     */
    private void printBarCode(Intent intent) throws Exception {
        Bitmap bitmap = BarUtils.encodeAsBitmap(intent.getStringExtra(EXTRA_TEXT)
                , BarcodeFormat.CODE_128, 380, 80);
        mPrinter.setBitmap(bitmap);
    }

    /**
     * 打印二维码
     */
    private void printQrcode(Intent intent) throws Exception {
        Bitmap bitmap = BarUtils.encodeAsBitmapOffset(intent.getStringExtra(EXTRA_TEXT)
                , BarcodeFormat.QR_CODE, 300, 300, 44);
        mPrinter.setBitmap(bitmap);
    }

    /**
     * 延迟
     */
    private void delay(Intent intent) throws InterruptedException {
        Thread.sleep(intent.getIntExtra(EXTRA_DELAY, 0));
    }
}
