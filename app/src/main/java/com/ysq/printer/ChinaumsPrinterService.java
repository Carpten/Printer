package com.ysq.printer;

import android.app.IntentService;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;

import com.ums.upos.sdk.exception.SdkException;
import com.ums.upos.sdk.printer.FontConfig;
import com.ums.upos.sdk.printer.OnPrintResultListener;
import com.ums.upos.sdk.printer.PrinterManager;
import com.ums.upos.sdk.system.BaseSystemManager;
import com.ums.upos.sdk.system.OnServiceStatusListener;

import java.util.List;
import java.util.concurrent.CountDownLatch;

public class ChinaumsPrinterService extends IntentService {

    /**
     * 打印文档KEY
     */
    public static final String EXTRA_TEXT = "com.ysq.printer.action.EXTRA_TEXT";
    /**
     * 意图类型，0：启动打印机，1：打印内容，2：开始打印，3：断开打印机
     */
    public static final String INTENT_TYPE = "INTENT_TYPE";


    private CountDownLatch mCountDownLatch = new CountDownLatch(1);

    private PrinterManager mPrinter;


    public ChinaumsPrinterService() {
        super(ChinaumsPrinterService.class.getSimpleName());
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        int intExtra = intent.getIntExtra(INTENT_TYPE, 0);
        if (intExtra == 0) {
            bindService();
        } else if (intExtra == 1 && mPrinter != null) {
            try {
                mPrinter.setPrnText(intent.getStringExtra(EXTRA_TEXT), new FontConfig());
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (intExtra == 2) {
            try {
                mPrinter.startPrint(new OnPrintResultListener() {
                    @Override
                    public void onPrintResult(int i) {
                        try {
                            BaseSystemManager.getInstance().deviceServiceLogout();
                        } catch (SdkException e) {
                            e.printStackTrace();
                        }
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (intExtra == 3) {

        }
    }

    //绑定服务
    private void bindService() {
        try {
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
                                mCountDownLatch.countDown();
                            }
                        }
                    });
            mCountDownLatch.await();
        } catch (Exception e) {
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
