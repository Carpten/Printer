package com.ysq.printer;

import android.app.IntentService;
import android.content.Intent;

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
            } else if (intExtra == 1 && mPrinter != null) {
                mPrinter.setPrnText(intent.getStringExtra(EXTRA_TEXT), new FontConfig());
            } else if (intExtra == 2) {
                mCountDownLatch = new CountDownLatch(1);
                mPrinter.startPrint(new OnPrintResultListener() {
                    @Override
                    public void onPrintResult(int i) {
                        mCountDownLatch.countDown();
                    }
                });
                mCountDownLatch.await();
            } else if (intExtra == 3) {
                close();
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
     * 释放打印机
     */
    private void close() throws Exception {
        BaseSystemManager.getInstance().deviceServiceLogout();
    }
}
