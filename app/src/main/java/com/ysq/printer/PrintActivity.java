package com.ysq.printer;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

public class PrintActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_print);
    }

    public void print(View view) {
        //        new PrintManage(new LacaraPrinter(MainActivity.this)).printDetail();
//        new PrintManage(new ChinaumsPrinter(MainActivity.this)).printDetail();
//        new PrintManage(new SunmiscPrinter(MainActivity.this)).printDetail();
        new PrintManage(new BluetoothPrinter(
                PrintActivity.this, "DC:0D:30:21:24:14")).printDetail();
    }
}
