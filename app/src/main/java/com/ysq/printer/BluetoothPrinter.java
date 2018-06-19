package com.ysq.printer;

import android.content.Context;
import android.content.Intent;

/**
 * <pre>
 * author : 杨水强
 * time   : 2018/05/31
 * desc   :
 * version: 1.0
 * </pre>
 */
public class BluetoothPrinter implements Printable {

    private Context mContext;
    private String mAddress;

    public BluetoothPrinter(Context context, String address) {
        mContext = context;
        mAddress = address;
    }

    @Override
    public void init() {
        Intent intent = new Intent(mContext, BluetoothPrinterService.class);
        intent.putExtra(BluetoothPrinterService.EXTRA_TYPE, 0);
        intent.putExtra(BluetoothPrinterService.EXTRA_ADDRESS, mAddress);
        mContext.startService(intent);
    }

    @Override
    public void printText(String text, boolean isCenter, boolean isLarge) {
        Intent intent = new Intent(mContext, BluetoothPrinterService.class);
        intent.putExtra(BluetoothPrinterService.EXTRA_TYPE, 3);
        intent.putExtra(BluetoothPrinterService.EXTRA_TEXT, text);
        intent.putExtra(BluetoothPrinterService.EXTRA_CENTER, isCenter);
        intent.putExtra(BluetoothPrinterService.EXTRA_LARGE, isLarge);
        mContext.startService(intent);
    }


    @Override
    public void printBarcode(String text) {
        Intent intent = new Intent(mContext, BluetoothPrinterService.class);
        intent.putExtra(BluetoothPrinterService.EXTRA_TYPE, 4);
        intent.putExtra(BluetoothPrinterService.EXTRA_TEXT, text);
        mContext.startService(intent);
    }

    @Override
    public void printQrcode(String text) {
        Intent intent = new Intent(mContext, BluetoothPrinterService.class);
        intent.putExtra(BluetoothPrinterService.EXTRA_TYPE, 5);
        intent.putExtra(BluetoothPrinterService.EXTRA_TEXT, text);
        mContext.startService(intent);
    }

    @Override
    public void flushPrint() {
        Intent intent = new Intent(mContext, BluetoothPrinterService.class);
        intent.putExtra(BluetoothPrinterService.EXTRA_TYPE, 1);
        mContext.startService(intent);
    }

    @Override
    public void delay(int millisecond) {
        Intent intent = new Intent(mContext, BluetoothPrinterService.class);
        intent.putExtra(BluetoothPrinterService.EXTRA_TYPE, 6);
        intent.putExtra(BluetoothPrinterService.EXTRA_DELAY, millisecond);
        mContext.startService(intent);
    }

    @Override
    public void feedPaper() {
        Intent intent = new Intent(mContext, BluetoothPrinterService.class);
        intent.putExtra(BluetoothPrinterService.EXTRA_TYPE, 7);
        mContext.startService(intent);
    }

    @Override
    public void close() {
        Intent intent = new Intent(mContext, BluetoothPrinterService.class);
        intent.putExtra(BluetoothPrinterService.EXTRA_TYPE, 2);
        intent.putExtra(PrintIntentService.EXTRA_MODE, 1);
        mContext.startService(intent);
    }
}
