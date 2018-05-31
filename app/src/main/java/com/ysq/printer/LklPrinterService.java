package com.ysq.printer;

import android.app.IntentService;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import com.lkl.cloudpos.aidl.AidlDeviceService;
import com.lkl.cloudpos.aidl.printer.AidlPrinter;
import com.lkl.cloudpos.aidl.printer.AidlPrinterListener;
import com.lkl.cloudpos.aidl.printer.PrintItemObj;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

public class LklPrinterService extends IntentService {

    /**
     * 打印文档KEY
     */
    public static final String EXTRA_TEXT = "com.ysq.printer.action.EXTRA_TEXT";
    /**
     * 意图类型，0：启动打印机，1：打印内容，2：开始打印，3：断开打印机
     */
    public static final String INTENT_TYPE = "INTENT_TYPE";
    //LKL AIDL ACTION值
    private static final String ACTION_LKL_SERVICE = "lkl_cloudpos_mid_service";


    private CountDownLatch mCountDownLatch = new CountDownLatch(1);
    private List<PrintItemObj> mPrintItemObjs = new ArrayList<>();
    private AidlPrinter mPrinter;

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

    public LklPrinterService() {
        super(LklPrinterService.class.getSimpleName());
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        int intExtra = intent.getIntExtra(INTENT_TYPE, 0);
        if (intExtra == 0) {
            bindService();
        } else if (intExtra == 1 && mPrinter != null) {
            mPrintItemObjs.add(new PrintItemObj(intent.getStringExtra(EXTRA_TEXT), 20
                    , true, PrintItemObj.ALIGN.CENTER));
        } else if (intExtra == 2) {
            try {
                mPrinter.printText(mPrintItemObjs, new AidlPrinterListener.Stub() {
                    @Override
                    public void onError(int i) {

                    }

                    @Override
                    public void onPrintFinish() {
                        Log.i("test", Thread.currentThread().getName());
                    }
                });
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        } else if (intExtra == 3) {
            unbindService(mServiceConnection);
        }
    }

    //绑定服务
    private void bindService() {
        Intent intent = new Intent();
        intent.setAction(ACTION_LKL_SERVICE);
        bindService(getExplicitIntent(getApplicationContext(), intent)
                , mServiceConnection, Context.BIND_AUTO_CREATE);
        try {
            mCountDownLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
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
