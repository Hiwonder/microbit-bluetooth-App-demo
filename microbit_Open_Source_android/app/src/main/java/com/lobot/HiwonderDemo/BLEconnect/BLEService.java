package com.lobot.HiwonderDemo.BLEconnect;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.Log;


import com.lobot.HiwonderDemo.util.ItonAdecimalConver;
import com.lobot.HiwonderDemo.util.LogUtil;

import java.util.List;

/**
 * 蓝牙4.0通讯用Service
 *
 * @author hejie
 */
public class BLEService extends Service {

    private static final String BLE_NAME = "com.lobot.microcar.le";
    /**
     * Action，蓝牙GATT已连接上
     */
    public static final String ACTION_GATT_CONNECTED = BLE_NAME + ".ACTION_GATT_CONNECTED";
    /**
     * Action，蓝牙GATT连接失败
     */
    public static final String ACTION_GATT_CONNECT_FAIL = BLE_NAME + ".ACTION_GATT_CONNECT_FAIL";
    /**
     * Action，蓝牙GATT已断开
     */
    public static final String ACTION_GATT_DISCONNECTED = BLE_NAME + ".ACTION_GATT_DISCONNECTED";
    /**
     * Action，发现蓝牙GATT服务
     */
    public static final String ACTION_GATT_SERVICES_DISCOVERED = BLE_NAME + ".ACTION_GATT_SERVICE_DISCOVERED";
    /**
     * Action，蓝牙GATT数据到达
     */
    public static final String ACTION_DATA_AVAILABLE = BLE_NAME + ".ACTION_DATA_AVAILABLE";
    /**
     * 蓝牙GATT数据的Key
     */
    public static final String EXTRA_DATA = BLE_NAME + ".EXTRA_DATA";
    private static final String TAG = "hh";
    //BLEService.class.getSimpleName();

    private final BLEBinder mBinder = new BLEBinder();

    private int mConnectState = BluetoothProfile.STATE_DISCONNECTED;

    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothGatt mBluetoothGatt;
    private String mBluetoothDeviceAddress;

    boolean connectState = false;
    boolean servicesDiscovered = false;
    boolean descriptorWrite = false;

    public interface NotificationListener {
        void onNotification(BluetoothGattCharacteristic characteristic);
    }

    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {

        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
//            Log.e(TAG, "连接前***：connectState:"+connectState+","+"servicesDiscovered:"+servicesDiscovered+","+"descriptorWrite:"+descriptorWrite );
            LogUtil.e(TAG, "onConnectionStateChange*** status = " + status + " state = " + newState);
            switch (newState) {
                case BluetoothProfile.STATE_CONNECTED:
                    Log.e(TAG, "连接1: ");
                    connectState = true;
                    if (connectState == true && servicesDiscovered == true && descriptorWrite == true) {
                        Log.e(TAG, "onConnectionStateChange:" + "蓝牙连接成功1");
                        sendBroadcast(ACTION_GATT_CONNECTED);
                    }
                    LogUtil.i(TAG, "onConnectionStateChange --- connected!");
                    // Attempts to discover services after successful connection.
                    LogUtil.i(TAG, "Attempting to start service discovery:" + mBluetoothGatt.discoverServices());
//                    BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(mBluetoothDeviceAddress); TODO 无法绑定
//                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
//                        LogUtil.i(TAG, "create bond");
//                        device.createBond();
//                    }
                    break;
                case BluetoothProfile.STATE_DISCONNECTED:
                    Log.e(TAG, "onConnectionStateChange: " + "蓝牙连接断开");
                    connectState = false;
                    servicesDiscovered = false;
                    descriptorWrite = false;

                    if (mConnectState == BluetoothProfile.STATE_CONNECTING) {
                        sendBroadcast(ACTION_GATT_CONNECT_FAIL);
                        LogUtil.i(TAG, "onConnectionStateChange --- connect failed!");
                    } else {
                        sendBroadcast(ACTION_GATT_DISCONNECTED);
                        LogUtil.i(TAG, "onConnectionStateChange --- disconnected!");
                    }
                    break;
                case BluetoothProfile.STATE_CONNECTING:
                    LogUtil.i(TAG, "onConnectionStateChange --- connecting!");
                    break;
                case BluetoothProfile.STATE_DISCONNECTING:
                    LogUtil.i(TAG, "onConnectionStateChange --- disconnecting!");
                    break;
            }
            mConnectState = newState;
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            LogUtil.e(TAG, "onServicesDiscovered*** status = " + status);
            servicesDiscovered = true;
            Log.e(TAG, "连接2: ");
            if (connectState == true && servicesDiscovered == true && descriptorWrite == true) {
                Log.e(TAG, "onConnectionStateChange:" + "蓝牙连接成功2");
                sendBroadcast(ACTION_GATT_CONNECTED);
            }
            if (status == BluetoothGatt.GATT_SUCCESS) {
                sendBroadcast(ACTION_GATT_SERVICES_DISCOVERED);
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
//            LogUtil.e(TAG, "onCharacteristicRead status");
//            Log.e("TAG", "接收：" + ItonAdecimalConver.AsciiStringToString(ItonAdecimalConver.bytesToHexString(characteristic.getValue())));
            String str = ItonAdecimalConver.AsciiStringToString(ItonAdecimalConver.bytesToHexString(characteristic.getValue()));
            if (str.contains("CMD|02") || str.contains("CMD|15")) {
                Log.e("UHANDBIT--", "接收: " + str);
            }
            String data = new String(characteristic.getValue());
            sendBroadcast(ACTION_DATA_AVAILABLE, data);
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
//            LogUtil.e(TAG, "onCharacteristicWrite status = " + status);
            String tag = ItonAdecimalConver.AsciiStringToString(ItonAdecimalConver.bytesToHexString(characteristic.getValue()));
//            Log.e(TAG, "发送: " + tag);
            if (tag.contains("CMD|02") || tag.contains("CMD|0F")) {
                Log.e("UHANDBIT--", "uhand发送: " + tag);
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            LogUtil.e(TAG, "onCharacteristicRead status = " + status);
            String data = new String(characteristic.getValue());
            sendBroadcast(ACTION_DATA_AVAILABLE, data);
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorWrite(gatt, descriptor, status);
            descriptorWrite = true;
            Log.e(TAG, "onDescriptorWrite***: status=" + status);
            Log.e(TAG, "连接3: ");
            if (connectState == true && servicesDiscovered == true && descriptorWrite == true) {
                Log.e(TAG, "onConnectionStateChange:" + "蓝牙连接成功3");
                sendBroadcast(ACTION_GATT_CONNECTED);
            }
            LogUtil.e(TAG, "onDescriptorWrite status = " + status);
        }

    };

    @Override
    public void onCreate() {
        // TODO Auto-generated method stub

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // TODO Auto-generated method stub
        return super.onStartCommand(intent, flags, startId);
    }

    public boolean init() {
        if (mBluetoothManager == null) { // 获取系统的蓝牙管理器
            mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            if (mBluetoothManager == null) {
                LogUtil.e(TAG, "Unable to initialize BluetoothManager.");
                return false;
            }
        }

        mBluetoothAdapter = mBluetoothManager.getAdapter();
        if (mBluetoothAdapter == null) {
            LogUtil.e(TAG, "Unable to obtain a BluetoothAdapter.");
            return false;
        }

        return true;
    }

    public boolean writeCharacteristic(BluetoothGattCharacteristic characteristic) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            LogUtil.w(TAG, "BluetoothAdapter not initialized");
            return false;
        }
        if (characteristic != null) {
            if (mBluetoothGatt.writeCharacteristic(characteristic)) {
                LogUtil.i(TAG, "write success");
                return true;
            } else {
                LogUtil.i(TAG, "write fail");
                return false;
            }
        } else {
            LogUtil.w(TAG, "characteristic is null");
            return false;
        }
    }

    /**
     * Request a read on a given {@code BluetoothGattCharacteristic}. The read result is reported
     * asynchronously through the {@code BluetoothGattCallback#onCharacteristicRead(android.bluetooth.BluetoothGatt, android.bluetooth.BluetoothGattCharacteristic, int)}
     * callback.
     *
     * @param characteristic The characteristic to read from.
     */
    public void readCharacteristic(BluetoothGattCharacteristic characteristic) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            LogUtil.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.readCharacteristic(characteristic);
    }

    /**
     * Enables or disables notification on a give characteristic.
     *
     * @param characteristic Characteristic to act on.
     * @param enabled        If true, enable notification.  False otherwise.
     */
    public void setCharacteristicNotification(BluetoothGattCharacteristic characteristic,
                                              boolean enabled, NotificationListener listener) {
        Log.e(TAG, "setCharacteristicNotification: ");
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            LogUtil.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.setCharacteristicNotification(characteristic, enabled);
        if (listener != null) {
            listener.onNotification(characteristic);
        }
    }

    public void writeDescriptor(BluetoothGattDescriptor descriptor) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            LogUtil.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.writeDescriptor(descriptor);
    }

    /**
     * Connects to the GATT server hosted on the Bluetooth LE device.
     *
     * @param address The device address of the destination device.
     * @return Return true if the connection is initiated successfully. The connection result
     * is reported asynchronously through the
     * {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
     * callback.
     */
    public boolean connect(final String address) {
        Log.e(TAG, "connect: ");
        connectState = false;
        servicesDiscovered = false;
        descriptorWrite = false;
        if (mBluetoothAdapter == null || address == null) {
            LogUtil.w(TAG, "BluetoothAdapter not initialized or unspecified address.");
            return false;
        }

        // Previously connected device.  Try to reconnect.
        if (mBluetoothDeviceAddress != null && address.equals(mBluetoothDeviceAddress)
                && mBluetoothGatt != null) {
            LogUtil.d(TAG, "Trying to use an existing mBluetoothGatt for connection.");
            if (mBluetoothGatt.connect()) {
                mConnectState = BluetoothProfile.STATE_CONNECTING;
                return true;
            } else {
                return false;
            }
        }

        final BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        if (device == null) {
            LogUtil.e(TAG, "Device not found.  Unable to connect.");
            return false;
        }
        // We want to directly connect to the device, so we are setting the autoConnect
        // parameter to false.
        mBluetoothGatt = device.connectGatt(this, false, mGattCallback);
        LogUtil.e(TAG, "Trying to create a new connection.");
        mBluetoothDeviceAddress = address;
        mConnectState = BluetoothProfile.STATE_CONNECTING;
        return true;
    }

    /**
     * 尝试重连上次连接的设备
     *
     * @return 是否能够重连
     */
    public boolean reconnect() {
        Log.e(TAG, "reconnect: ");
        if (mBluetoothDeviceAddress != null && mBluetoothGatt != null) {
            LogUtil.d(TAG, "Trying to use an existing mBluetoothGatt for connection.");
            if (mBluetoothGatt.connect()) {
                mConnectState = BluetoothProfile.STATE_CONNECTING;
                return true;
            }
        }
        return false;
    }

    /**
     * Disconnects an existing connection or cancel a pending connection. The disconnection result
     * is reported asynchronously through the
     * {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
     * callback.
     */
    public void disconnect() {
        Log.e(TAG, "disconnect: ");
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            LogUtil.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.disconnect();
     //   close();////////////////////
    //    close_BLE();
    }

    /**
     * After using a given BLE device, the app must call this method to ensure resources are
     * released properly.
     *
     * 测试过程：
     * 两部手机来回切换与设备的蓝牙连接；
     * 会出现一部手机连接过后，再连接不上去设备的问题；
     * 猜想原因：在频繁切换的时候硬件蓝牙模块导致的；
     * 猜测理由：当硬件模块重启复位后不会在出现该现象；
     * 代码中已做的处理：close()方法用来释放当前的BLE Gatt连接资源
     *
     */
    public void close() {
        Log.e(TAG, "close: ");
        if (mBluetoothGatt == null) {
            return;
        }
        mBluetoothGatt.close();
        mBluetoothGatt = null;
    }

    public void close_BLE() {
        if (mBluetoothGatt == null) {
            return;
        }

        Log.d(TAG, "mBluetoothGatt closed");

        mBluetoothDeviceAddress = null;

        disconnect();

        mBluetoothGatt.close();

        mBluetoothGatt = null;
    }

    /**
     * Retrieves a list of supported GATT services on the connected device. This should be
     * invoked only after {@code BluetoothGatt#discoverServices()} completes successfully.
     *
     * @return A {@code List} of supported services.
     */
    public List<BluetoothGattService> getSupportedGattServices() {
        if (mBluetoothGatt == null) return null;

        return mBluetoothGatt.getServices();
    }

    private void sendBroadcast(String action) {
        sendBroadcast(action, null);
    }

    private void sendBroadcast(String action, String data) {
        Intent intent = new Intent(action);
        if (!TextUtils.isEmpty(data)) {
            intent.putExtra(EXTRA_DATA, data);
        }
        sendBroadcast(intent);
    }

    private void sendBroadcast(byte[] data, String action) {
        Intent intent = new Intent(action);
        if (data.length > 0) {
            intent.putExtra(EXTRA_DATA, data);
        }
        sendBroadcast(intent);
    }

    /**
     * @return the Connect State
     */
    public int getConnectState() {
        return mConnectState;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public class BLEBinder extends Binder {

        public BLEService getService() {
            return BLEService.this;
        }
    }


}
