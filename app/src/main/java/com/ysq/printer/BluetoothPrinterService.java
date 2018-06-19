package com.ysq.printer;

import android.app.IntentService;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;

import java.io.IOException;
import java.io.OutputStream;
import java.util.UUID;

public class BluetoothPrinterService extends PrintIntentService {

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

    /**
     * 蓝牙地址传值键
     */
    public static final String EXTRA_ADDRESS = "EXTRA_ADDRESS";

    //蓝牙打印所需的UUID
    private static final UUID BLUETOOTH_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    // 蓝牙SOCKET对象
    private BluetoothSocket mBluetoothSocket;

    // 蓝牙SOCKET输出流
    private OutputStream mOutputStream;

    private static final byte[] COMMAND_CLEAR_FORMAT = {0x1b, 0x40}; //复位打印机
    private static final byte[] COMMAND_CENTER = {0x1b, 0x61, 0x01};//居中指令
    private static final byte[] COMMAND_DOUBLE_HEIGHT = {0x1d, 0x21, 0x01}; //高加倍
    private static final byte[] COMMAND_WIDTH = {0x1D, 0x77, 0x02};//设置条码宽
    private static final byte[] COMMAND_HEIGHT = {0x1D, 0x68, 0x50};//设置条码高
    private static final byte[] COMAND_TOP_FROMAT = {0x1d, 0x48, 0x00}; //设置条码样式
    private static final byte[] COMMAND_ONE_CODE = {0x1D, 0x6B, 0x49, 0x0E, 0x7B, 0x43};//一维码指令


    public BluetoothPrinterService() {
        super(BluetoothPrinterService.class.getSimpleName());
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        try {
            int intExtra = intent.getIntExtra(EXTRA_TYPE, 0);
            if (intExtra == 0) {
                init(intent.getStringExtra(EXTRA_ADDRESS));
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
        }
    }

    /**
     * 初始化打印机
     */
    private void init(String address) throws Exception {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        BluetoothDevice remoteDevice = bluetoothAdapter.getRemoteDevice(address);
        mBluetoothSocket = remoteDevice.createRfcommSocketToServiceRecord(BLUETOOTH_UUID);
        mBluetoothSocket.connect();
        mOutputStream = mBluetoothSocket.getOutputStream();
    }

    /**
     * 写入打印机，某些打印机在startPrint以后需要flushPrint操作
     */
    private void flushPrint() {

    }

    /**
     * 释放打印机
     */
    private void close() throws IOException {
        if (mOutputStream != null) {
            mOutputStream.close();
        }
        if (mBluetoothSocket != null && mBluetoothSocket.isConnected()) {
            mBluetoothSocket.close();
        }
    }


    /**
     * 打印文字
     */
    private void printText(String text, boolean isCenter, boolean isLarge) throws Exception {
        byte[] textBytes = (text + "\r\n").getBytes("gbk");
        mOutputStream.write(COMMAND_CLEAR_FORMAT);
        if (isCenter) {
            mOutputStream.write(COMMAND_CENTER);
        }
        if (isLarge) {
            mOutputStream.write(COMMAND_DOUBLE_HEIGHT);
        }
        mOutputStream.write(textBytes);
        mOutputStream.flush();
    }


    /**
     * 打印条形码
     */
    private void printBarCode(String text) throws Exception {
        byte[] b = new byte[13];
        int i = 0;
        while (2 * i < text.length()) {
            String s = text.substring(2 * i, 2 * i + 2);
            b[i] = Byte.parseByte(s);
            i++;
        }
        //一维码打印
        mOutputStream.write(COMMAND_CLEAR_FORMAT);
        mOutputStream.write(COMMAND_HEIGHT);
        mOutputStream.write(COMMAND_WIDTH);
        mOutputStream.write(COMMAND_CENTER);
        mOutputStream.write(COMAND_TOP_FROMAT);
        mOutputStream.write(COMMAND_ONE_CODE);
        mOutputStream.write(b);
        mOutputStream.flush();
    }

    /**
     * 打印二维码
     */
    private void printQrcode(String text) throws Exception {
        Integer pl = (text.length() + 3) % 256;
        Integer ph = (text.length() + 3) / 256;
        mOutputStream.write(COMMAND_CLEAR_FORMAT);
        mOutputStream.write(new byte[]{0x1B, 0x61, 0x01});
        mOutputStream.write(new byte[]{0x1D, 0x28, 0x6B, 0x03, 0x00, 0x31, 0x43, 0x08});
        mOutputStream.write(new byte[]{0x1D, 0x28, 0x6B, 0x03, 0x00, 0x31, 0x45, 0x30});
        mOutputStream.write(new byte[]{0x1D, 0x28, 0x6B, pl.byteValue(), ph.byteValue(), 0x31, 0x50, 0x30});
        mOutputStream.write(text.getBytes());
        mOutputStream.write(new byte[]{0x1D, 0x28, 0x6B, 0x03, 0x00, 0x31, 0x51, 0x30});
        mOutputStream.flush();
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
}
