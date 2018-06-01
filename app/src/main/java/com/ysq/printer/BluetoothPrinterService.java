package com.ysq.printer;

import android.app.IntentService;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

import java.io.IOException;
import java.io.OutputStream;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;

import woyou.aidlservice.jiuiv5.IWoyouService;

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

    //用来控制线程，将异步转成同步
    private CountDownLatch mCountDownLatch;
    //银商打印对象
    private IWoyouService mPrinter;

    private BluetoothSocket mBluetoothSocket;
    private OutputStream mOutputStream;

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


    public BluetoothPrinterService() {
        super(BluetoothPrinterService.class.getSimpleName());
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        try {
            int intExtra = intent.getIntExtra(EXTRA_TYPE, 0);
            if (intExtra == 0) {
                init(intent.getStringExtra(EXTRA_ADDRESS));
            } else if (intExtra == 1 && mPrinter != null) {
                try {
                    byte[] textStr = intent.getStringExtra(EXTRA_TEXT).getBytes("gbk");
                    mOutputStream.write(CLEAR_FORMAT);
                    mOutputStream.write(textStr);
                    mOutputStream.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else if (intExtra == 3) {
                close();
            }
        } catch (Exception ignored) {
        }
    }

    /**
     * 初始化打印机
     */
    private void init(String address) throws Exception {
        mCountDownLatch = new CountDownLatch(1);
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        BluetoothDevice remoteDevice = bluetoothAdapter.getRemoteDevice(address);
        mBluetoothSocket = remoteDevice.createRfcommSocketToServiceRecord(uuid);
        mBluetoothSocket.connect();
        mOutputStream = mBluetoothSocket.getOutputStream();
        mCountDownLatch.await();
    }

    /**
     * 释放打印机
     */
    private void close() {
        unbindService(mServiceConnection);
    }
}
