package com.ysq.printer;

import android.app.IntentService;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

import java.util.concurrent.CountDownLatch;

import woyou.aidlservice.jiuiv5.IWoyouService;

public class SunmiscPrinterService extends IntentService {

    /**
     * 打印文档KEY
     */
    public static final String EXTRA_TEXT = "com.ysq.printer.action.EXTRA_TEXT";
    /**
     * 意图类型，0：启动打印机，1：打印内容，2：开始打印，3：断开打印机
     */
    public static final String INTENT_TYPE = "INTENT_TYPE";

    //用来控制线程，将异步转成同步
    private CountDownLatch mCountDownLatch;
    //银商打印对象
    private IWoyouService mPrinter;


    private static final byte[] CLEAR_FORMAT = {0x1b, 0x40}; //复位打印机
    private static final byte[] COMMAND = {0x1B, 0x21, 0x08};
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
            int intExtra = intent.getIntExtra(INTENT_TYPE, 0);
            if (intExtra == 0) {
                init();
            } else if (intExtra == 1 && mPrinter != null) {
                mPrinter.sendRAWData(CLEAR_FORMAT, null);
                mPrinter.sendRAWData(COMMAND, null);
                mPrinter.setAlignment(0, null);
                //商米打印文字时需要加\r\n
                mPrinter.printText(intent.getStringExtra(EXTRA_TEXT) + "\r\n", null);
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
        Intent intent = new Intent();
        intent.setPackage("woyou.aidlservice.jiuiv5");
        intent.setAction("woyou.aidlservice.jiuiv5.IWoyouService");
        bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE);
        mCountDownLatch.await();
    }

    /**
     * 释放打印机
     */
    private void close() {
        unbindService(mServiceConnection);
    }
}
