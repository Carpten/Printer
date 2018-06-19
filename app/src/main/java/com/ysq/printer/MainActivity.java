package com.ysq.printer;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    private PrintManage mPrintManage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        mPrintManage = new PrintManage(new BluetoothPrinter(
//                MainActivity.this, "DC:0D:30:21:24:14"));
        //        new PrintManage(new LacaraPrinter(MainActivity.this)).printDetail();
//        new PrintManage(new ChinaumsPrinter(MainActivity.this)).printDetail();
        mPrintManage = new PrintManage(new SunmiscPrinter(MainActivity.this));
        mPrintManage.init();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mPrintManage.close();
    }

    public void print(View view) {
        mPrintManage.printDetail();
    }
}