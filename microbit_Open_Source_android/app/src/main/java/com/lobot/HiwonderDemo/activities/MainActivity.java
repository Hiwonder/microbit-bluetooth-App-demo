package com.lobot.HiwonderDemo.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.ButtonBarLayout;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.lobot.HiwonderDemo.R;
import com.lobot.HiwonderDemo.application.MyApplication;
import com.lobot.HiwonderDemo.BLEconnect.BLEManager;
import com.lobot.HiwonderDemo.BLEconnect.BLEService;
import com.lobot.HiwonderDemo.BLEconnect.ByteCommand;
import com.lobot.HiwonderDemo.BLEconnect.Command;
import com.lobot.HiwonderDemo.BLEconnect.Constants;
import com.lobot.HiwonderDemo.util.BluetoothUtils;
import com.lobot.HiwonderDemo.util.DirectionControlInterface;
import com.lobot.HiwonderDemo.util.NoDoubleClickListener;

import com.lobot.HiwonderDemo.widget.HandShake;
import com.lobot.HiwonderDemo.widget.PromptDialog;

import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    private ImageView rightBle;

    private LinearLayout llTop, llBottom, llLeft, llRight;
    private Button btTop, btBottom, btLeft, btRight;
    private static final int Top = 1;
    private static final int Bottom = 2;
    private static final int Left = 3;
    private static final int Right = 4;

    public static float versionNum = 0; //固件版本


    //蓝牙相关
    public static BluetoothAdapter mBluetoothAdapter = null;
    public static BLEManager bleManager;
    private Handler mHandler;

    private int connectTimes; //连接次数
    private static final int RETRY_TIMES = 3;

    public static boolean isConnected; //蓝牙连接状态
    public boolean recv_flag = false; //是否有控制指令的返回值

    Timer timer = new Timer();


    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            BLEService bleService = ((BLEService.BLEBinder) service).getService();
            BLEManager.getInstance().init(bleService);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            BLEManager.getInstance().destroy();
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);
        MyApplication.getInstance().addActivity(this);
        mHandler = new Handler(new MsgCallBack());
        if (!BluetoothUtils.isSupport(BluetoothAdapter.getDefaultAdapter())) {
            Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show();
            finish();
        }
        initView();

        setListener();

        Intent intent = new Intent(this, BLEService.class);
        startService(intent);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
        BLEManager.getInstance().register(this);
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        connectTimes = 0;


    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.e(TAG, "onResume: ");
        bleManager = BLEManager.getInstance();
        isConnected = bleManager.isConnected();
        bleManager.setHandler(mHandler);
        setBluetoothAnima(isConnected);
        callback();
    }


    @SuppressLint("ClickableViewAccessibility")
    private void initView() {
        ButtonListener b = new ButtonListener();
        rightBle = findViewById(R.id.right_button);

        llTop = findViewById(R.id.llTop);
        llBottom = findViewById(R.id.llBottom);
        llLeft = findViewById(R.id.llLeft);
        llRight = findViewById(R.id.llRight);

        btTop = findViewById(R.id.btTop);
        btTop.setOnClickListener(b);
        btTop.setOnTouchListener(b);

        btBottom = findViewById(R.id.btBottom);
        btBottom.setOnClickListener(b);
        btBottom.setOnTouchListener(b);

        btLeft = findViewById(R.id.btLeft);
        btLeft.setOnClickListener(b);
        btLeft.setOnTouchListener(b);

        btRight = findViewById(R.id.btRight);
        btRight.setOnClickListener(b);
        btRight.setOnTouchListener(b);

    }




    class ButtonListener implements View.OnClickListener, View.OnTouchListener {

        public void onClick(View v) {
        }

        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    switch (v.getId()) {
                        case R.id.btTop:
                            sendRunCarActionCmdNoThread("A");
                            llTop.setBackgroundColor(ContextCompat.getColor(MainActivity.this, R.color.theme_blue));
                            break;
                        case R.id.btBottom:
                            sendRunCarActionCmdNoThread("B");
                            llBottom.setBackgroundColor(ContextCompat.getColor(MainActivity.this, R.color.theme_blue));
                            break;
                        case R.id.btLeft:
                            sendRunCarActionCmdNoThread("C");
                            llLeft.setBackgroundColor(ContextCompat.getColor(MainActivity.this, R.color.theme_blue));
                            break;
                        case R.id.btRight:
                            sendRunCarActionCmdNoThread("D");
                            llRight.setBackgroundColor(ContextCompat.getColor(MainActivity.this, R.color.theme_blue));
                            break;
                    }
                    break;
                case MotionEvent.ACTION_UP:
                    sendRunCarActionCmd("E");  //停止
                    switch (v.getId()) {
                        case R.id.btTop:
                            llTop.setBackgroundColor(ContextCompat.getColor(MainActivity.this, R.color.theme_blue2));
                            break;
                        case R.id.btBottom:
                            llBottom.setBackgroundColor(ContextCompat.getColor(MainActivity.this, R.color.theme_blue2));
                            break;
                        case R.id.btLeft:
                            llLeft.setBackgroundColor(ContextCompat.getColor(MainActivity.this, R.color.theme_blue2));
                            break;
                        case R.id.btRight:
                            llRight.setBackgroundColor(ContextCompat.getColor(MainActivity.this, R.color.theme_blue2));
                            break;
                    }
                    break;
            }

            return false;
        }

    }


    private void sendRunCarActionCmdNoThread(String index)//发送动作组命令
    {
        if (isConnected) {
            String cmdStr = index + "$";
            Command.Builder builder = new Command.Builder();
            builder.addCommand(cmdStr, 20);
            BLEManager.getInstance().send(builder.createCommands());
        }
    }

    private void sendRunCarActionCmd(String index)//发送动作组命令
    {
        if (isConnected) {
            String cmdStr = index + "$";
            Command.Builder builder = new Command.Builder();
            builder.addCommand(cmdStr, 20);
            BLEManager.getInstance().send(builder.createCommands());
            new Thread(new cmdRecvThread()).start();
        }
    }


    public class cmdRecvThread implements Runnable {
        @Override
        public void run() {
            try {
                Thread.sleep(150);
            } catch (Exception e) {
                e.printStackTrace();
            }
            int i = 0;
            Log.d("hh_aaa", "recv_flag : =" + recv_flag);
            while (!recv_flag && i < 10)//发5次命令
            {
                Log.e("TAG", "run: 开启停止线程");
                i++;
                sendRunCarActionCmdNoThread("E");
                try {
                    Thread.sleep(100);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            recv_flag = false;
        }
    }

    private void setListener() {

        rightBle.setOnClickListener(new NoDoubleClickListener() {
            @Override
            protected void onNoDoubleClick(View v) { //点击右侧蓝牙按钮
                if (isConnected) {
                    PromptDialog.create(MainActivity.this, getFragmentManager(), getString(R.string.disconnect_tips_title), getString(R.string.disconnect_tips_connect), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (DialogInterface.BUTTON_POSITIVE == which) {
                                bleManager.stop();
                                versionNum = 0;
                                isConnected = false;
                                startActivity(new Intent(getBaseContext(), BluetoothActivity.class));
                            }
                        }
                    });
                } else {
                    mayRequestLocation(); //开启定位权限
                    Intent open = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(open, 0);
                }
            }
        });

    }


    public void setBluetoothAnima(boolean start) {
        if (!start) {
            final Animation setAnim = AnimationUtils.loadAnimation(this, R.anim.bluetooth_anim);
            rightBle.startAnimation(setAnim);
        } else {
            rightBle.clearAnimation();
        }
    }


    /**
     * 位置权限
     */
    private void mayRequestLocation() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            //检查权限（NEED_PERMISSION）是否被授权 PackageManager.PERMISSION_GRANTED表示同意授权
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                //用户已经拒绝过一次，再次弹出权限申请对话框需给用户一个解释
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_COARSE_LOCATION)) {
                    //toast解释
                    Toast.makeText(MainActivity.this, getString(R.string.open_local_permission_or_ble_not_use_normal), Toast.LENGTH_LONG).show();
                }
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 0);
            } else {
                Log.e(TAG, "***权限已开启");
            }
        } else {
            Log.e(TAG, "****系统版本小于23");
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 0) {
            switch (resultCode) {
                case Activity.RESULT_OK:
                    Log.e(TAG, "蓝牙已开启");
                    startActivity(new Intent(MainActivity.this, BluetoothActivity.class));
                    break;
                case Activity.RESULT_CANCELED:
                    Log.e(TAG, "蓝牙开启请求被拒绝");
                    Toast.makeText(MainActivity.this, getResources().getString(R.string.refuse_to_open_bluetooth), Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        timer.cancel();
        unbindService(mConnection);
        BLEManager.getInstance().unregister(this);
    }

    private boolean confirm;

    @Override
    public void onBackPressed() {
        if (BLEManager.getInstance().isConnected()) {
            PromptDialog.create(this, getFragmentManager(), getString(R.string.exit_tips_title),
                    getString(R.string.exit_tips_content), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (DialogInterface.BUTTON_POSITIVE == which) {
                                BLEManager.getInstance().stop();
                                MainActivity.super.onBackPressed();
                                MyApplication.getInstance().exit();
                            }
                        }
                    });
        } else {
            if (!confirm) {
                confirm = true;
                Toast.makeText(this, R.string.exit_remind, Toast.LENGTH_SHORT).show();
                Timer timer = new Timer();
                timer.schedule(new TimerTask() {

                    @Override
                    public void run() {
                        confirm = false;
                    }
                }, 2000);
            } else {
                Intent intent = new Intent(this, BLEService.class);
                stopService(intent);
                BLEManager.getInstance().destroy();
                super.onBackPressed();
                android.os.Process.killProcess(android.os.Process.myPid());
            }
        }
    }

    /**
     * 处理消息
     */
    class MsgCallBack implements Handler.Callback {

        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case Constants.MessageID.MSG_CONNECT_SUCCEED:
                    Toast.makeText(getBaseContext(), R.string.bluetooth_state_connected, Toast.LENGTH_SHORT).show();
                    isConnected = true;
                    setBluetoothAnima(isConnected);

                    break;
                case Constants.MessageID.MSG_CONNECT_FAILURE:
                    if (connectTimes < RETRY_TIMES) {
                        connectTimes++;
                        mHandler.sendEmptyMessageDelayed(Constants.MessageID.MSG_CONNECT_RECONNECT, 300);
                    } else {
                        connectTimes = 0;
                        Toast.makeText(getBaseContext(), R.string.bluetooth_state_connect_failure, Toast.LENGTH_SHORT).show();
                    }
                    isConnected = false;
                    setBluetoothAnima(isConnected);
                    break;
                case Constants.MessageID.MSG_CONNECT_RECONNECT:
                    break;
                case Constants.MessageID.MSG_CONNECT_LOST:
                    Toast.makeText(getBaseContext(), R.string.disconnect_tips_succeed, Toast.LENGTH_SHORT).show();
                    isConnected = false;

                    setBluetoothAnima(isConnected);
                    break;
                case Constants.MessageID.MSG_SEND_COMMAND:
                    bleManager.send((ByteCommand) msg.obj);
                    Message sendMsg = mHandler.obtainMessage(Constants.MessageID.MSG_SEND_COMMAND, msg.arg1, -1, msg.obj);
                    mHandler.sendMessageDelayed(sendMsg, msg.arg1);
                    break;

                case Constants.MessageID.MSG_SEND_NOT_CONNECT:
                    Toast.makeText(getBaseContext(), R.string.send_tips_no_connected, Toast.LENGTH_SHORT).show();
                    break;
                case Constants.MessageID.MSG_HANDSHAKE_STOP_CMD:
                    if (directionControlInterface != null) {
                        directionControlInterface.directionBack(true);
                    }
                    break;

            }
            return true;
        }
    }

    DirectionControlInterface directionControlInterface; //方向控制返回值接口

    public void getDirectionBack(DirectionControlInterface directionControlInterface) {
        this.directionControlInterface = directionControlInterface;
    }

    private void callback() {
        getDirectionBack(new DirectionControlInterface() {
            @Override
            public void directionBack(boolean directionBack) {
                recv_flag = directionBack;
            }
        });
    }


}
