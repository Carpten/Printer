package com.ysq.printer;

import android.app.IntentService;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.TextUtils;

import com.google.zxing.BarcodeFormat;
import com.ums.upos.sdk.printer.FontConfig;
import com.ums.upos.sdk.printer.FontSizeEnum;
import com.ums.upos.sdk.printer.OnPrintResultListener;
import com.ums.upos.sdk.printer.PrinterManager;
import com.ums.upos.sdk.system.BaseSystemManager;
import com.ums.upos.sdk.system.OnServiceStatusListener;

import java.util.concurrent.CountDownLatch;

public class ChinaumsPrinterService extends IntentService {

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
    private PrinterManager mPrinter;

    //空白图片，用来打印，展示行间距
    private Bitmap mSpaceBitmap;


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
            if (mCountDownLatch != null) {
                mCountDownLatch.countDown();
            }
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
    private void printText(String text, boolean isCenter, boolean isLarge) throws Exception {
        if (isCenter) {
            mPrinter.setBitmap(getCenterTextBitmap(text, isLarge));
        } else {
            FontConfig fontConfig = new FontConfig();
            fontConfig.setSize(isLarge ? FontSizeEnum.BIG : FontSizeEnum.MIDDLE);
            String[] split = text.split("\n");
            for (String s : split) {
                if (TextUtils.isEmpty(s)) {
                    mPrinter.setPrnText(" ", fontConfig);
                } else {
                    mPrinter.setPrnText(s, fontConfig);
                }
                mPrinter.setBitmap(getSpaceBitmap());
            }
        }
    }

    /**
     * 打印条形码
     */
    private void printBarCode(String text) throws Exception {
        Bitmap bitmap = BarUtils.encodeAsBitmap(text
                , BarcodeFormat.CODE_128, 380, 80);
        mPrinter.setBitmap(bitmap);
    }

    /**
     * 打印二维码
     */
    private void printQrcode(String text) throws Exception {
        Bitmap bitmap = BarUtils.encodeAsBitmapOffset(text
                , BarcodeFormat.QR_CODE, 300, 300, 44);
        mPrinter.setBitmap(bitmap);
    }

    /**
     * 延迟
     */
    private void delay(int millisecond) throws InterruptedException {
        Thread.sleep(millisecond);
    }

    /**
     * 银联商务不能直接居中打印文字，因此文字先生成图片再打印
     */
    private Bitmap getCenterTextBitmap(String text, boolean isLarge) {
        TextPaint textPaint = new TextPaint();
        textPaint.setAntiAlias(true);
        textPaint.setTextSize(isLarge ? 36 : 24);
        textPaint.setStrokeWidth(0.5f);
        textPaint.setColor(0xff000000);
        StaticLayout staticLayout = new StaticLayout(text, textPaint, 380
                , Layout.Alignment.ALIGN_CENTER
                , 1.0f, 0.0f, false);
        Bitmap bitmap = Bitmap.createBitmap(380
                , staticLayout.getHeight(), Bitmap.Config.RGB_565);
        Canvas canvas = new Canvas(bitmap);
        canvas.drawColor(0xffffffff);
        staticLayout.draw(canvas);
        return bitmap;
    }


    private Bitmap getSpaceBitmap() {
        if (mSpaceBitmap == null) {
            mSpaceBitmap = Bitmap.createBitmap(1, 2, Bitmap.Config.RGB_565);
            Canvas canvas = new Canvas(mSpaceBitmap);
            canvas.drawColor(0xffffffff);
        }
        return mSpaceBitmap;
    }

    /**
     * 打印机走纸，通过调用打印两行空白文字实现
     */
    private void feedPaper() {
    }
}
