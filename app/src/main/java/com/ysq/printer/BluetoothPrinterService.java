package com.ysq.printer;

import android.app.IntentService;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.util.Log;

import java.io.IOException;
import java.io.OutputStream;
import java.util.UUID;

public class BluetoothPrinterService extends IntentService {

    /**
     * 打印文档KEY
     */
    public static final String EXTRA_TEXT = "com.ysq.printer.action.EXTRA_TEXT";
    /**
     * 意图类型，0：启动打印机，1：打印内容，2：开始打印，3：断开打印机
     */
    public static final String EXTRA_TYPE = "EXTRA_TYPE";
    public static final String EXTRA_ADDRESS = "EXTRA_ADDRESS";
    private static final UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    private BluetoothSocket mBluetoothSocket;
    private OutputStream mOutputStream;

    private static final byte[] CLEAR_FORMAT = {0x1b, 0x40}; //复位打印机
    private static final byte[] COMMAND = {0x1B, 0x21, 0x08};


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
                byte[] textStr = intent.getStringExtra(EXTRA_TEXT).getBytes("gbk");
                mOutputStream.write(CLEAR_FORMAT);
                mOutputStream.write(textStr);
                mOutputStream.flush();
            } else if (intExtra == 3) {
                close();
            }
        } catch (Exception ignored) {
            Log.i("test", ignored.getMessage());
        }
    }

    /**
     * 初始化打印机
     */
    private void init(String address) throws Exception {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        BluetoothDevice remoteDevice = bluetoothAdapter.getRemoteDevice(address);
        mBluetoothSocket = remoteDevice.createRfcommSocketToServiceRecord(uuid);
        mBluetoothSocket.connect();
        mOutputStream = mBluetoothSocket.getOutputStream();
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
}
