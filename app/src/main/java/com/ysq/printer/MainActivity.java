package com.ysq.printer;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        new PrintManage(new LklPrinter(MainActivity.this)).printDetail();
        new PrintManage(new ChinaumsPrinter(MainActivity.this)).printDetail();
        new PrintManage(new ChinaumsPrinter(MainActivity.this)).printDetail();
        new PrintManage(new ChinaumsPrinter(MainActivity.this)).printDetail();
        new PrintManage(new ChinaumsPrinter(MainActivity.this)).printDetail();
    }

    public void print(View view) {
        new PrintManage(new ChinaumsPrinter(MainActivity.this)).printDetail();
    }
}
