package com.youtushuju.lingdongapp.gui;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;

import com.youtushuju.lingdongapp.common.Common;
import com.youtushuju.lingdongapp.common.Configs;
import com.youtushuju.lingdongapp.common.Constants;
import com.youtushuju.lingdongapp.common.Logf;
import com.youtushuju.lingdongapp.common.Sys;
import com.youtushuju.lingdongapp.device.PutOpenDoorReqStruct;
import com.youtushuju.lingdongapp.device.SerialPortFunc;
import com.youtushuju.lingdongapp.device.SerialReqStruct;
import com.youtushuju.lingdongapp.device.SerialRespStruct;

public final class DeviceFunc {
    private static final String ID_TAG = "DeviceFunc";
    public static final int ID_STATE_INIT = 0; // 初始化
    public static final int ID_STATE_READY = 1; // 等待中
    public static final int ID_STATE_SENDING = 2; // 准备发送
    public static final int ID_STATE_SENDED = 3; // 发送完成
    public static final int ID_STATE_RECVING = 4; // 接收数据中
    public static final int ID_STATE_DONE = 5; // 完成流程
    public static final int ID_STATE_UNSEND = -1; // 未发送成功
    public static final int ID_STATE_TIMEOUT = -2; // 超时

    //private static final char ID_END_CHARACTER = '\r';
    private static final char ID_END_CHARACTER = '}';

    private String m_buffer = ""; // 缓存的数据
    private String m_lastRecvData = "";
    private String m_lastSendData = "";
    private int m_state = ID_STATE_INIT;
    private long m_sendTime = 0;
    private Activity m_activity = null;
    private Handler m_handler = null;
    private OnSerialPortListener m_serialPortListener = null;
    //private boolean m_running = false;

    private SerialPortFunc m_serialPortDriver = null;
    private SerialReqStruct m_serialReq = null;

    private SerialPortFunc.OnDataReceivedListener m_dataReceivedListener = new SerialPortFunc.OnDataReceivedListener() {
        @Override
        public void OnDataReceived(byte[] data, int length) {
            String str = new String(data, 0, length);
            Logf.d(ID_TAG, "从串口接收数据(%s), 长度(%d)", str, length);
            if(m_state == ID_STATE_SENDED)
                SetState(ID_STATE_RECVING);
            if(m_state == ID_STATE_RECVING)
            {
                synchronized (m_buffer) {
                    m_buffer += str;
                }
            }
        }
    };

    public interface OnSerialPortListener {
        public void OnOpened();
        public void OnClosed();
        public void OnMessage(String msg);
        public void OnError(String error);
        public void OnTimeout(String sendData, int timeout);
        public void OnRecv(String recvData, String sendData, SerialReqStruct req);
        public void OnSend(String data, SerialReqStruct req, boolean success);
        public void OnStateChanged(int state);
    }

    public DeviceFunc(Activity activity, Handler handler) {
        m_activity = activity;
        m_serialReq = new PutOpenDoorReqStruct();
        if(handler == null)
            m_handler = new Handler(Looper.getMainLooper());
        else
            m_handler = handler;
    }

    public void SetOnSerialPortListener(OnSerialPortListener l) {
        m_serialPortListener = l;
    }

    public String LastRecvData() {
        return m_lastRecvData;
    }

    public void SetDoorId(String doorId) {
        m_serialReq.door_id = doorId;
        Logf.e(ID_TAG, m_serialReq.toString());
    }

    private boolean OpenSerialPortDriver() {
        if (m_serialPortDriver == null) {
            m_serialPortDriver = Configs.Instance().GetLingDongSerialDriver(m_activity);

            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(m_activity);
            String path = preferences.getString(Constants.ID_PREFERENCE_SERIAL_PATH, Configs.ID_PREFERENCE_DEFAULT_SERIAL_PATH);
            int baudrate = Configs.ID_PREFERENCE_DEFAULT_SERIAL_BAUDRATE;
            try {
                baudrate = Integer.parseInt(preferences.getString(Constants.ID_PREFERENCE_SERIAL_BAUDRATE, "" + Configs.ID_PREFERENCE_DEFAULT_SERIAL_BAUDRATE));
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (!m_serialPortDriver.InitSerialPort(path, baudrate)) {
                if (m_serialPortListener != null)
                    m_serialPortListener.OnError("串口初始化错误");
                return false;
            }
            if (!m_serialPortDriver.Open()) {
                if (m_serialPortListener != null)
                    m_serialPortListener.OnError("串口打开错误");
                return false;
            }

            m_serialPortDriver.SetOnDataReceivedListener(m_dataReceivedListener);
        }

        if (m_serialPortListener != null)
            m_serialPortListener.OnOpened();
        return m_serialPortDriver != null;
    }

    private void CloseSerialPortDriver() {
        if (m_serialPortDriver == null)
            return;
        m_serialPortDriver.ShutdownSerialPort();
        m_serialPortDriver = null;
        if (m_serialPortListener != null)
            m_serialPortListener.OnClosed();
    }

    private boolean Send(SerialReqStruct req) {
        if (m_serialPortDriver == null)
            OpenSerialPortDriver();

        if (m_serialPortDriver == null || !m_serialPortDriver.IsOpened()) {
            if (m_serialPortListener != null)
                m_serialPortListener.OnError("发送数据时串口未打开");
            return false;
        }

        SetState(ID_STATE_SENDING);
        ReadySend();
        m_lastSendData = req.Dump() + '\r'; // 添加回车符
        Logf.d(ID_TAG, "发送串口数据(%s), 长度(%d)", m_lastSendData, m_lastSendData.length());

        SetState(ID_STATE_SENDED); // 提前设置为发送完成
        final int len = m_serialPortDriver.Send(
                //m_lastSendData.getBytes()
                Common.String8BitsByteArray(m_lastSendData) // TODO: 8bits
        );
        if (len <= 0) {
            SetState(ID_STATE_UNSEND);
            if (m_serialPortListener != null)
                m_serialPortListener.OnError("串口写入错误: " + len);
            if (m_serialPortListener != null)
                m_serialPortListener.OnSend(m_lastSendData, req,false);
            return false;
        }

        m_sendTime = System.currentTimeMillis();

        if (m_serialPortListener != null)
            m_serialPortListener.OnSend(m_lastSendData,  req,true);

        return true;
    }

    // timeout = 0: 不等待, < 0 无限等待, > 0 毫秒
    public boolean OpenDoor(int timeout)
    {
        if(m_state == ID_STATE_INIT)
        {
            if (m_serialPortListener != null)
                m_serialPortListener.OnMessage("开始发送请求");
            SetState(ID_STATE_READY);
        }

        if(m_state != ID_STATE_READY)
        {
            if (m_serialPortListener != null)
                m_serialPortListener.OnError("上次发送流程未结束");
            return false;
        }

        if(!m_serialReq.IsValid())
        {
            if (m_serialPortListener != null)
                m_serialPortListener.OnError("缺失门ID");
            return false;
        }

        boolean ok = Send(m_serialReq);
        if(!ok)
        {
            if (m_serialPortListener != null)
                m_serialPortListener.OnError("发送失败");
            return false;
        }

        if(timeout == 0) // 不等待
            return true;
        else
        {
            StringBuffer sb = new StringBuffer();
            String data = null;
            boolean r = true;
            while(r)
            {
                synchronized (m_buffer) {
                    if(!Common.StringIsEmpty(m_buffer))
                    {
                        int i = 0;
                        for (; i < m_buffer.length(); i++)
                        {
                            char ch = m_buffer.charAt(i);
                            if(ch != ID_END_CHARACTER)
                                sb.append(ch);
                            else
                            {
                                sb.append(ch);
                                Logf.d(ID_TAG, "读取到结束符");
                                data = sb.toString();
                                break;
                            }
                        }
                        if(i >= m_buffer.length() - 1)
                            m_buffer = "";
                        else
                            m_buffer = m_buffer.substring(i); // 移除已经读取的字符
                    }
                }

                if(data != null) // 数据接收完毕
                {
                    int index = data.indexOf('{');
                    m_lastRecvData = data.substring(index);
                    if (m_serialPortListener != null)
                        m_serialPortListener.OnRecv(m_lastRecvData, m_lastSendData, m_serialReq);
                    SetState(ID_STATE_DONE);
                    Logf.d(ID_TAG, "读取结束: " + m_lastRecvData);
                    return true;
                }

                if(timeout > 0)
                {
                    long now = System.currentTimeMillis();
                    if(now - m_sendTime >= timeout)
                    {
                        Logf.w(ID_TAG, "读取超时");
                        SetState(ID_STATE_TIMEOUT);
                        if (m_serialPortListener != null)
                            m_serialPortListener.OnTimeout(m_lastSendData, timeout);
                        return false;
                    }
                }
            }
            return false;
        }
    }

    // 执行动作时调用
    private void ReadySend()
    {
        m_serialReq.Finish();
        m_lastRecvData = "";
        m_lastSendData = "";
        m_buffer = "";
    }

    public int State()
    {
        return m_state;
    }

    synchronized private void SetState(int s)
    {
        if(m_state != s)
        {
            m_state = s;
            if (m_serialPortListener != null)
                m_serialPortListener.OnStateChanged(m_state);
        }
    }

    // 重置状态, 对外部, 内部不会调用
    public void Ready()
    {
        SetState(ID_STATE_READY);
        m_buffer = "";
        m_lastRecvData = "";
        m_lastSendData = "";
        m_sendTime = 0L;
    }

    public void Reset()
    {
        SetState(ID_STATE_INIT);
        m_buffer = "";
        m_lastRecvData = "";
        m_lastSendData = "";
        m_sendTime = 0L;
    }

    public void Shutdown()
    { // TODO: others?
        Reset();
        CloseSerialPortDriver();
    }

    public boolean IsRunning()
    {
        return m_state == ID_STATE_READY || m_state == ID_STATE_RECVING || m_state == ID_STATE_SENDING || m_state == ID_STATE_SENDED;
    }

    public boolean IsFinished()
    {
        return m_state == ID_STATE_DONE || m_state == ID_STATE_UNSEND || m_state == ID_STATE_TIMEOUT;
    }

    public boolean IsSuccess()
    {
        return m_state == ID_STATE_DONE;
    }

    public boolean IsFail()
    {
        return m_state == ID_STATE_UNSEND || m_state == ID_STATE_TIMEOUT;
    }

    public boolean IsCanStart()
    {
        return m_state == ID_STATE_INIT || IsFinished();
    }

    public boolean IsReady()
    {
        return m_state == ID_STATE_INIT;
    }
}
