package com.ysq.printer;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.i("test", "machine:" + getChannel());
    }

    public void print(View view) {
        new PrintManage(new LacaraPrinter(MainActivity.this)).printDetail();
//        new PrintManage(new ChinaumsPrinter(MainActivity.this)).printDetail();
//        new PrintManage(new SunmiscPrinter(MainActivity.this)).printDetail();
//        new PrintManage(new BluetoothPrinter(
//                PrintActivity.this, "DC:0D:30:21:24:14")).printDetail();
    }

    public String getChannel() {
        String channel = "";
        PackageManager pm = getPackageManager();
        try {
            ApplicationInfo ai = pm.getApplicationInfo(
                    getPackageName(),
                    PackageManager.GET_META_DATA);
            String value = ai.metaData.getString("MACHINE_TYPE");
            if (value != null) {
                channel = value;
            }
        } catch (Exception e) {
            // 忽略找不到包信息的异常
        }
        return channel;
    }
}
