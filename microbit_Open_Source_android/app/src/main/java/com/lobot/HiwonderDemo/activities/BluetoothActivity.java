package com.lobot.HiwonderDemo.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.RotateAnimation;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.lobot.HiwonderDemo.R;
import com.lobot.HiwonderDemo.BLEconnect.BLEManager;
import com.lobot.HiwonderDemo.BLEconnect.Command;
import com.lobot.HiwonderDemo.BLEconnect.Constants;
import com.lobot.HiwonderDemo.util.LogUtil;

import com.lobot.HiwonderDemo.widget.BluetoothDataAdapter;


import java.util.ArrayList;
import java.util.Timer;

public class BluetoothActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {
    private static final String TAG = "TAG";

    private static final int DISPLAY_BLE_LIST = 555;
    private static final int SEND_FIRMWARE_INFORMATION_CMD = 556;
    private static final int UNNABLE_GET_FIRMWARE_INFORMATION = 557;
    private static final int BEGIN_SCAN_BLE = 558;

    private ImageView back, bleList;
    private TextView connectText;

    private static final int REQUEST_FINE_LOCATION = 0;
    private static final int RETRY_TIMES = 3;
    private Handler mHandler;
    private boolean isConnected = false;

    /**
     * 连接次数
     */
    private int connectTimes;

    private BluetoothAdapter mBluetoothAdapter = null;

    private BLEManager bleManager;

    private BluetoothDevice mBluetoothDevice;

    private Timer timer = new Timer();

    private boolean isReceFirmwareInformation = false; //判断是否收到的固件信息返回的布尔值

    public static BluetoothDataAdapter mAdapter;
    public static ArrayList<BluetoothDevice> devices = new ArrayList<BluetoothDevice>();

    private BluetoothAdapter.LeScanCallback leCallBack = new BluetoothAdapter.LeScanCallback() {

        @Override
        public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
            String name = device.getName();
            if (name != null && name.contains("BBC") && device.getBondState() != BluetoothDevice.BOND_BONDED && !devices.contains(device)) {
                addDevice(device);
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_bluetooth);
        Log.e(TAG, "onCreate");
        mHandler = new Handler(new MsgCallBack());
        connectText = findViewById(R.id.connect_status);
        back = findViewById(R.id.iv_back);
        back.setOnClickListener(listener);
        bleList = findViewById(R.id.iv_ble_list);
        bleList.setOnClickListener(listener);
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        mayRequestLocation();

        ListView listView = findViewById(R.id.listview_dialog);
        mAdapter = new BluetoothDataAdapter(BluetoothActivity.this, devices);
        listView.setAdapter(mAdapter);
        listView.setOnItemClickListener(this);
    }


    @Override
    public void onResume() {
        super.onResume();
        Log.e(TAG, "onResume:" + System.currentTimeMillis());
        bleManager = BLEManager.getInstance();
        isConnected = bleManager.isConnected();
        Log.e(TAG, "onResume: " + isConnected);
        bleManager.setHandler(mHandler);
        addDevice(null);

        handler.postDelayed(beginScanBleRunnable, 1000);
        isReceFirmwareInformation = false;
    }

    Runnable beginScanBleRunnable = new Runnable() {
        @Override
        public void run() {
            Log.e(TAG, "run: 开启蓝牙扫描:" + System.currentTimeMillis());
            handler.sendEmptyMessage(BEGIN_SCAN_BLE);
        }
    };

    @SuppressLint("HandlerLeak")
    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case SEND_FIRMWARE_INFORMATION_CMD:
                    queryFirmwareInformation(true);
                    break;
                case UNNABLE_GET_FIRMWARE_INFORMATION:
                    BluetoothActivity.this.finish();
                    bleManager.stop();
                    Toast.makeText(getBaseContext(), "获取固件版本失败", Toast.LENGTH_SHORT).show();
                    break;
                case BEGIN_SCAN_BLE:
                    mBluetoothAdapter.startLeScan(leCallBack);
                    break;
            }

        }
    };

    /**
     * 查询固件信息
     */
    private void queryFirmwareInformation(boolean isOpenThread) {
        Log.e(TAG, "queryFirmwareInformation: 固件查询2222");
        if (!isConnected) {
            return;
        }
        String cmdStr = "Q$";
        Command.Builder builder = new Command.Builder();
        builder.addCommand(cmdStr, 20);
        BLEManager.getInstance().send(builder.createCommands());
        if (isOpenThread && !isReceFirmwareInformation) {
            new Thread(new cmdFirmwareInformationRecvThread()).start();
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        onDeviceSelected(devices.get(position));
        connectText.setText(getString(R.string.connecting_device));
    }

    /**
     * 开线程多次发送获取固件信息指令，以保证能够获取固件信息
     */
    public class cmdFirmwareInformationRecvThread implements Runnable {
        @Override
        public void run() {
            try {
                Thread.sleep(150);
            } catch (Exception e) {
                e.printStackTrace();
            }
            int i = 0;
            while (!isReceFirmwareInformation && i < 20)//发10次命令
            {
                i++;
                queryFirmwareInformation(false);
                try {
                    Thread.sleep(150);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            if (i == 20 && !isReceFirmwareInformation) {

                Log.d("hh", "hh_no   没有收到固件");
                handler.sendEmptyMessage(UNNABLE_GET_FIRMWARE_INFORMATION);
            }
            isReceFirmwareInformation = false;
        }
    }

    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            handler.sendEmptyMessage(DISPLAY_BLE_LIST);
        }
    };
    Runnable runnableFirmwareInformation = new Runnable() {
        @Override
        public void run() {
            if (handler != null) {
                Log.e(TAG, "run: 固件查询111");
                handler.sendEmptyMessage(SEND_FIRMWARE_INFORMATION_CMD);
            }
        }
    };

    @Override
    protected void onPause() {
        super.onPause();
        handler.removeCallbacks(runnable);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.e(TAG, "onDestroy");
        mBluetoothAdapter.stopLeScan(leCallBack);
        timer.cancel();
    }


    View.OnClickListener listener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.iv_back: //返回
                    mBluetoothAdapter.cancelDiscovery();
                    finish();
                    break;
                case R.id.iv_ble_list: //刷新蓝牙列表
                    Animation rotateAnimation = new RotateAnimation(0, 360, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
                    rotateAnimation.setDuration(1000);
                    addDevice(null);
                    bleList.startAnimation(rotateAnimation);
                    mBluetoothAdapter.startLeScan(leCallBack);
                    break;
            }
        }
    };

    public void addDevice(BluetoothDevice device) {
        if (device == null) {
            devices.clear();
        } else {
            devices.add(device);
        }
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onBackPressed() {
        BluetoothActivity.this.finish();
    }

    private void mayRequestLocation() {
        if (mBluetoothAdapter.isEnabled()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                int checkCallPhonePermission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION);
                if (checkCallPhonePermission != PackageManager.PERMISSION_GRANTED) {
                    if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_COARSE_LOCATION)) {

                    }
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_FINE_LOCATION);
                    return;
                } else {

                }
            } else {

            }
        }
    }


    public void onDeviceSelected(BluetoothDevice device) {
        if (device != null) {
            LogUtil.i(TAG, "bond state = " + device.getBondState());
            mBluetoothDevice = device;
            bleManager.connect(device);
            mBluetoothAdapter.stopLeScan(leCallBack);
        } else {
            devices.clear();
            mBluetoothAdapter.stopLeScan(leCallBack);
            mBluetoothAdapter.startLeScan(leCallBack);
        }


    }


    class MsgCallBack implements Handler.Callback {

        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case Constants.MessageID.MSG_CONNECT_SUCCEED:
                    LogUtil.i(TAG, "connected ");
                    Log.e(TAG, "蓝牙连接成功");
                    isConnected = true;
                    handler.postDelayed(runnableFirmwareInformation, 300); // 200ms后发送查询固件信息指令
                    break;

                case Constants.MessageID.MSG_CONNECT_FAILURE:
                    Log.e(TAG, "蓝牙连接失败");
                    if (connectTimes < RETRY_TIMES) {
                        connectTimes++;
                        mHandler.sendEmptyMessageDelayed(Constants.MessageID.MSG_CONNECT_RECONNECT, 300);
                    } else {
                        connectTimes = 0;
                        Toast.makeText(getBaseContext(), getString(R.string.bluetooth_state_connect_failure), Toast.LENGTH_SHORT).show();
                    }
                    isReceFirmwareInformation = false;
                    break;
                case Constants.MessageID.MSG_CONNECT_RECONNECT:
                    LogUtil.i(TAG, "reconnect bluetooth" + mBluetoothDevice.getName() + " " + connectTimes);
                    bleManager.connect(mBluetoothDevice);
                    break;
                case Constants.MessageID.MSG_CONNECT_LOST:
                    Log.e(TAG, "蓝牙断开连接");
                    isReceFirmwareInformation = false;
                    Toast.makeText(getBaseContext(), getString(R.string.disconnect_tips_succeed), Toast.LENGTH_SHORT).show();
                    connectText.setText(getString(R.string.disconnect_tips_succeed));
                    break;

                case Constants.MessageID.MSG_RECV_ROM_VERSION: //获取到固件信息
                    isReceFirmwareInformation = true;
                    Toast.makeText(getBaseContext(), getString(R.string.read_version_success), Toast.LENGTH_SHORT).show();
                    finish();
                    break;

                case Constants.MessageID.MSG_RECV_ROM_VERSION_TIMEOUT:
                    bleManager.stop();
                    connectText.setText(getString(R.string.read_version_timeout));
                    break;

                case Constants.MessageID.MSG_COMM_ERRON_CMD:
                    Toast.makeText(getBaseContext(), getString(R.string.com_erro), Toast.LENGTH_LONG).show();
                    break;
            }
            return true;
        }
    }


}
