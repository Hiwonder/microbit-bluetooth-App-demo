package com.lobot.HiwonderDemo.BLEconnect;

/**
 * 常量类
 */
public class Constants {

    /**
     * 消息id类
     *
     * @author hejie
     */
    public static class MessageID {
        /**
         * 请求连接
         */
        public static final int MSG_REQUEST_CONNECT = 1;
        /**
         * 蓝牙状态改变
         */
        public static final int MSG_STATE_CHANGE = MSG_REQUEST_CONNECT + 1;
        /**
         * 连接成功
         */
        public static final int MSG_CONNECT_SUCCEED = MSG_STATE_CHANGE + 1;
        /**
         * 连接失败
         */
        public static final int MSG_CONNECT_FAILURE = MSG_CONNECT_SUCCEED + 1;
        /**
         * 重新连接
         */
        public static final int MSG_CONNECT_RECONNECT = MSG_CONNECT_FAILURE + 1;
        /**
         * 连接断开
         */
        public static final int MSG_CONNECT_LOST = MSG_CONNECT_RECONNECT + 1;
        /**
         * 发送数据
         */
        public static final int MSG_SEND_DATA = MSG_CONNECT_LOST + 1;
        /**
         * 发送字符串
         */
        public static final int MSG_SEND_STRING = MSG_SEND_DATA + 1;
        /**
         * 发送指令集
         */
        public static final int MSG_SEND_COMMAND_LIST = MSG_SEND_STRING + 1;
        /**
         * 发送指令集，不可取消
         */
        public static final int MSG_SEND_COMMAND_STOP = MSG_SEND_COMMAND_LIST + 1;
        /**
         * 发送指令时，蓝牙未连接
         */
        public static final int MSG_SEND_NOT_CONNECT = MSG_SEND_COMMAND_STOP + 1;
        /**
         * 发送成功
         */
        public static final int MSG_SEND_SUCCESS = MSG_SEND_NOT_CONNECT + 1;
        /**
         * 已处理（可能未处理完成）
         */
        public static final int MSG_SEND_HANDLED = MSG_SEND_SUCCESS + 1;
        /**
         * 发送失败
         */
        public static final int MSG_SEND_FAILURE = MSG_SEND_HANDLED + 1;
        /**
         * 接收到数据
         */
        public static final int MSG_RECEIVE_DATA = MSG_SEND_FAILURE + 1;
        /**
         * 发送指令
         */
        public static final int MSG_SEND_COMMAND = MSG_RECEIVE_DATA + 1;
        /**
         * 隐藏语音结果显示文本
         */
        public static final int MSG_HIDE_RESULT = MSG_SEND_COMMAND + 1;
        /**
         * 更新语音结果显示文本
         */
        public static final int MSG_UPDATE_RESULT = MSG_HIDE_RESULT + 1;


        public static final int MSG_SEND_BYTECOMMAND = MSG_UPDATE_RESULT + 1;

        public static final int MSG_RECV_DISTANCE = MSG_SEND_BYTECOMMAND + 1;

        public static final int MSG_RECV_ROM_VERSION = MSG_RECV_DISTANCE + 1;

        public static final int MSG_RECV_ROM_VERSION_TIMEOUT = MSG_RECV_ROM_VERSION + 1;

        public static final int MSG_RECV_NORMAL_CMD = MSG_RECV_ROM_VERSION_TIMEOUT + 1;

        public static final int MSG_50ms_MSG = MSG_RECV_NORMAL_CMD + 1;

        public static final int MSG_TEMPERATURE_UPDATE = MSG_50ms_MSG + 1;

        public static final int MSG_SOUND_UPDATE = MSG_TEMPERATURE_UPDATE + 1;

        public static final int MSG_LIGHT_UPDATE = MSG_SOUND_UPDATE + 1;

        public static final int MSG_BATTERY_UPDATE = MSG_LIGHT_UPDATE + 1;

        public static final int MSG_COMM_ERRON_CMD = MSG_BATTERY_UPDATE + 1;


        public static final int IS_SOFA_HAVE_PERSON = MSG_COMM_ERRON_CMD + 1;


        /**
         * wifi模块启动UDP接收
         */
        public static final int WIFI_MODULE_START_UDP_RECE = IS_SOFA_HAVE_PERSON + 1;

        /**
         * wifi模块TCP与服务器连接
         */
        public static final int WIFI_MODULE_CONNECT_TCP_SERVER = WIFI_MODULE_START_UDP_RECE + 1;


        public static final int MSG_HANDSHAKE_STOP_CMD = WIFI_MODULE_CONNECT_TCP_SERVER + 1;
        public static final int MSG_HANDSHAKE_UP_CMD = MSG_HANDSHAKE_STOP_CMD + 1;
        public static final int MSG_HANDSHAKE_DOWN_CMD = MSG_HANDSHAKE_UP_CMD + 1;
        public static final int MSG_HANDSHAKE_LEFT_CMD = MSG_HANDSHAKE_DOWN_CMD + 1;
        public static final int MSG_HANDSHAKE_RIGHT_CMD = MSG_HANDSHAKE_LEFT_CMD + 1;


        public static final int MSG_USUAL_CONTROL_MODEL_CMD = MSG_HANDSHAKE_RIGHT_CMD + 1;
        public static final int MSG_TRACKING_MODEL_CMD = MSG_USUAL_CONTROL_MODEL_CMD + 1;
        public static final int MSG_ULTRASONIC_CMD = MSG_TRACKING_MODEL_CMD + 1;
        public static final int MSG_GAME_MODEL_CMD = MSG_ULTRASONIC_CMD + 1;


        /**
         * uHandbit动作组返回
         */
        public static final int MSG_UHANDBIT_RECEIVE_CMD1 = MSG_GAME_MODEL_CMD + 1;
        public static final int MSG_UHANDBIT_RECEIVE_CMD2 = MSG_UHANDBIT_RECEIVE_CMD1 + 1;
        public static final int MSG_UHANDBIT_RECEIVE_CMD3 = MSG_UHANDBIT_RECEIVE_CMD2 + 1;
        public static final int MSG_UHANDBIT_RECEIVE_CMD4 = MSG_UHANDBIT_RECEIVE_CMD3 + 1;
        public static final int MSG_UHANDBIT_RECEIVE_CMD5 = MSG_UHANDBIT_RECEIVE_CMD4 + 1;
        public static final int MSG_UHANDBIT_RECEIVE_CMD6 = MSG_UHANDBIT_RECEIVE_CMD5 + 1;
        public static final int MSG_UHANDBIT_RECEIVE_CMD7 = MSG_UHANDBIT_RECEIVE_CMD6 + 1;


    }
}
