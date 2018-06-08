package com.ysq.printer;

import android.app.IntentService;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.os.IBinder;
import android.os.RemoteException;

import com.google.zxing.BarcodeFormat;
import com.lkl.cloudpos.aidl.AidlDeviceService;
import com.lkl.cloudpos.aidl.printer.AidlPrinter;
import com.lkl.cloudpos.aidl.printer.AidlPrinterListener;
import com.lkl.cloudpos.aidl.printer.PrintItemObj;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * <pre>
 * author : 杨水强
 * time   : 2018/05/31
 * desc   : 拉卡拉打印服务
 * version: 1.0
 * </pre>
 */
public class LklPrinterService extends IntentService {

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

    //拉卡拉打印对象
    private AidlPrinter mPrinter;

    private AidlPrinterListener mListener = new AidlPrinterListener.Stub() {
        @Override
        public void onError(int i) {
            mCountDownLatch.countDown();
        }

        @Override
        public void onPrintFinish() {
            mCountDownLatch.countDown();
        }
    };

    public LklPrinterService() {
        super(LklPrinterService.class.getSimpleName());
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
     * 绑定服务
     */
    private void init() throws InterruptedException {
        mCountDownLatch = new CountDownLatch(1);
        Intent intent = new Intent();
        intent.setAction("lkl_cloudpos_mid_service");
        bindService(getExplicitIntent(getApplicationContext(), intent)
                , mServiceConnection, Context.BIND_AUTO_CREATE);
        mCountDownLatch.await();
    }

    /**
     * 设别服务连接桥
     */
    private ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder serviceBinder) {
            if (serviceBinder != null) {//绑定成功
                AidlDeviceService serviceManager = AidlDeviceService.Stub.asInterface(serviceBinder);
                try {
                    mPrinter = AidlPrinter.Stub.asInterface(serviceManager.getPrinter());
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
            mCountDownLatch.countDown();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
        }
    };

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
        mCountDownLatch = new CountDownLatch(1);
        //拉卡拉打印条目
        List<PrintItemObj> printItemObjs = new ArrayList<>();
        printItemObjs.add(new PrintItemObj(text, isLarge ? 20 : 8, true
                , isCenter ? PrintItemObj.ALIGN.CENTER : PrintItemObj.ALIGN.LEFT));
        mPrinter.printText(printItemObjs, mListener);
        mCountDownLatch.await();
    }

    /**
     * 打印条形码
     */
    private void printBarCode(String text) throws Exception {
        mCountDownLatch = new CountDownLatch(1);
        mPrinter.printBarCode(-1, 100, 18, 73, text, mListener);
        mCountDownLatch.await();
    }

    /**
     * 打印二维码
     */
    private void printQrcode(String text) throws Exception {
        mCountDownLatch = new CountDownLatch(1);
        Bitmap bitmap = BarUtils.encodeAsBitmap(text
                , BarcodeFormat.QR_CODE, 300, 300);
        mPrinter.printBmp(46, 300, 300, bitmap, mListener);
        mCountDownLatch.await();
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

    /**
     * 将意图转为显示意图
     *
     * @param implicitIntent 需要转换的意图
     * @return 转换后意图
     */
    private Intent getExplicitIntent(Context context, Intent implicitIntent) {
        PackageManager pm = context.getPackageManager();
        List<ResolveInfo> resolveInfo = pm.queryIntentServices(implicitIntent, 0);
        if (resolveInfo == null || resolveInfo.size() != 1) {
            return null;
        }
        ResolveInfo serviceInfo = resolveInfo.get(0);
        String packageName = serviceInfo.serviceInfo.packageName;
        String className = serviceInfo.serviceInfo.name;
        ComponentName component = new ComponentName(packageName, className);
        Intent explicitIntent = new Intent(implicitIntent);
        explicitIntent.setComponent(component);
        return explicitIntent;
    }
}
