package com.youtushuju.lingdongapp.gui;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.preference.PreferenceManager;

import com.youtushuju.lingdongapp.common.Common;
import com.youtushuju.lingdongapp.common.Configs;
import com.youtushuju.lingdongapp.common.Constants;
import com.youtushuju.lingdongapp.common.Logf;
import com.youtushuju.lingdongapp.common.Sys;
import com.youtushuju.lingdongapp.device.DropModeReqStruct;
import com.youtushuju.lingdongapp.device.DropModeRespStruct;
import com.youtushuju.lingdongapp.device.GetOpenDoorReqStruct;
import com.youtushuju.lingdongapp.device.GetOpenDoorRespStruct;
import com.youtushuju.lingdongapp.device.HeartbeatReqStruct;
import com.youtushuju.lingdongapp.device.HeartbeatRespStruct;
import com.youtushuju.lingdongapp.device.PutOpenDoorReqStruct;
import com.youtushuju.lingdongapp.device.PutOpenDoorRespStruct;
import com.youtushuju.lingdongapp.device.SerialPortFunc;
import com.youtushuju.lingdongapp.device.SerialReqStruct;
import com.youtushuju.lingdongapp.device.SerialRespStruct;
import com.youtushuju.lingdongapp.device.SerialSessionStruct;

import java.util.Queue;

public final class SerialPortDeviceDriver {
    private static final String ID_TAG = "DeviceFunc";
    public static final int ID_STATE_INIT = 0; // 初始化
    public static final int ID_STATE_READY = 1; // 等待中
    public static final int ID_STATE_SENDING = 2; // 准备发送
    public static final int ID_STATE_SENDED = 3; // 发送完成
    public static final int ID_STATE_RECVING = 4; // 接收数据中
    public static final int ID_STATE_DONE = 5; // 完成流程
    public static final int ID_STATE_UNSEND = -1; // 未发送成功
    public static final int ID_STATE_TIMEOUT = -2; // 超时

    public static final int ENUM_RESULT_LOCKED = -999; // 读写锁住
    public static final int ENUM_RESULT_EXCEPTION = -998; // 读写异常
    public static final int ENUM_RESULT_FUNC_EXCEPT = -997; // 无效动作
    public static final int ENUM_RESULT_NOT_WAIT = 0; // 不等待直接返回
    public static final int ENUM_RESULT_LAST_NOT_FINISHED = -1; // 上次读写流程未结束
    public static final int ENUM_RESULT_MISSING_PARAMETER = -2; // 参数缺失
    public static final int ENUM_RESULT_SEND_ERROR = -3; // 发送错误
    public static final int ENUM_RESULT_TIMEOUT = -4; // 读取超时
    public static final int ENUM_RESULT_OTHER = -5; // 其他错误

    public static final int CONST_RETURN_DELAY = 0; // 延时返回

    public static final String ENUM_ACTION_OPEN_DOOR = "OpenDoor";
    public static final String ENUM_ACTION_HEARTBEAT = "Heartbeat";
    public static final String ENUM_ACTION_OPEN_MAINTENANCE_DOOR = "OpenMaintenanceDoor";
    public static final String ENUM_ACTION_DROP_SET_MODE = "SetDropMode";

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
    private String m_device = null;
    private static SerialPortDeviceDriver _instance = null;
    private boolean m_lock = false;
    private boolean m_usingCallback = false; // 使用回调接收数据
    private int m_delay = CONST_RETURN_DELAY; // 延时返回时间间隔

    private SerialPortFunc m_serialPortDriver = null;
    private SerialReqStruct m_serialReq = null;
    private SerialRespStruct m_serialResp = null;
    private SerialSessionStruct m_serialSession = null;

    public static class IOResult{
        public int res;
        public SerialSessionStruct session;

        private IOResult(){}

        private IOResult(int res, SerialSessionStruct session)
        {
            this.res = res;
            this.session = session;
        }

        public boolean IsSuccess()
        {
            return res >= 0;
        }
    }

    // TODO: UNUSED
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
        public void OnFatal(String error);
        public void OnTimeout(String sendData, int timeout);
        public void OnRecv(String recvData, String sendData, SerialRespStruct resp, SerialReqStruct req);
        public void OnSend(String data, SerialReqStruct req, boolean success);
        public void OnStateChanged(int state);
    }

    private SerialPortDeviceDriver() {
        if(_instance != null)
            throw new Error("已经实例化, 调用static::Instance()获取实例.");

        /*m_activity = activity;

        if(handler == null)
            m_handler = new Handler(Looper.getMainLooper());
        else
            m_handler = handler;*/
        _instance = this;
    }

    public void SetOnSerialPortListener(OnSerialPortListener l) {
        m_serialPortListener = l;
    }

    public String LastRecvData() {
        return m_lastRecvData;
    }

    public String LastSendData() {
        return m_lastSendData;
    }

    public SerialReqStruct LastRequest() {
        return m_serialReq;
    }

    public SerialRespStruct LastResponse() {
        return m_serialResp;
    }

    public SerialSessionStruct LastSession() {
        return m_serialSession;
    }

    private void SetDoorId(String doorId) {
        m_device = doorId;
        Logf.e(ID_TAG, "设备ID: " + m_device);
    }

    private boolean InitSerialPortDriver()
    {
        Configs configs = Configs.Instance();
        m_serialPortDriver = configs.GetLingDongSerialDriver(null);

        String path = (String)configs.GetConfig(Configs.ID_CONFIG_SERIAL_PORT_DEVICE_PATH, Configs.ID_PREFERENCE_DEFAULT_SERIAL_PATH);
        int baudrate = (int)configs.GetConfig(Configs.ID_CONFIG_SERIAL_PORT_DEVICE_BAUDRATE, Configs.ID_PREFERENCE_DEFAULT_SERIAL_BAUDRATE);

        Logf.e(ID_TAG, "当前串口信息: IO Driver(%s), Path(%s), Baudrate(%d)", configs.GetConfig(Configs.ID_CONFIG_SERIAL_PORT_DEVICE_DRIVER), path, baudrate);

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

        if(m_usingCallback)
            m_serialPortDriver.SetOnDataReceivedListener(m_dataReceivedListener); // TODO: UNUSED
        return true;
    }

    private boolean OpenSerialPortDriver() {
        if (m_serialPortDriver == null) {
            if(!InitSerialPortDriver())
                return false;
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
    // 不会返回NULL
    public synchronized IOResult IO(String func, int timeout, Object ...args)
    {
        IOResult result = new IOResult();
        if (IsLock())
        {
            result.res = ENUM_RESULT_LOCKED;
            return result;
        }
        Lock();
        m_serialReq = null;
        m_serialResp = null;
        m_serialSession = null;
        int ret = 0;
        try
        {
            if(ENUM_ACTION_OPEN_DOOR.equals(func))
                ret = OpenDoor((String)args[0], timeout);
            else if(ENUM_ACTION_OPEN_MAINTENANCE_DOOR.equals(func))
                ret = OpenMaintenanceDoor((String)args[0], timeout);
            else if(ENUM_ACTION_HEARTBEAT.equals(func))
                ret = Heartbeat(timeout);
            else if(ENUM_ACTION_DROP_SET_MODE.equals(func))
                ret = SetDropMode((String)args[0], timeout);
            else
                ret = ENUM_RESULT_FUNC_EXCEPT;
            if(ret >= 0)
            {
                if(m_delay > 0)
                {
                    try
                    {
                        Thread.sleep(m_delay);
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                }
            }
        }
        catch (Throwable t)
        {
            t.printStackTrace();
            ret = ENUM_RESULT_EXCEPTION;
            m_serialSession = null;
        }
        finally {
            Shutdown(); // 自动关闭, 每次读写重新打开和关闭
            Unlock();
        }
        result.res = ret;
        result.session = m_serialSession;
        return result;
    }

    public int OpenDoor(String device, int timeout)
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
                m_serialPortListener.OnFatal("上次发送流程未结束");
            return ENUM_RESULT_LAST_NOT_FINISHED;
        }

        if(Common.StringIsBlank(device))
        {
            if (m_serialPortListener != null)
                m_serialPortListener.OnFatal("缺失门ID");
            return ENUM_RESULT_MISSING_PARAMETER;
        }

        SetDoorId(device);
        PutOpenDoorReqStruct req = new PutOpenDoorReqStruct(m_device);
        m_serialReq = req;
        m_serialResp = null;
        SerialSessionStruct session = new SerialSessionStruct();
        m_serialSession = session; // always new instance
        session.Request(req);

        boolean ok = Send(req);
        if(!ok)
        {
            if (m_serialPortListener != null)
                m_serialPortListener.OnFatal("发送失败");
            return -3;
        }

        if(timeout == 0) // 不等待
        {
            return ENUM_RESULT_NOT_WAIT;
        }
        else
        {
            int res = ReadData(timeout);
            if(res > 0) // success
            {
                PutOpenDoorRespStruct resp = new PutOpenDoorRespStruct();
                m_serialResp = resp;
                boolean suc = resp.Restore(m_lastRecvData);
                if(!suc)
                {
                    // TODO: 继续???
                }
                session.Response(resp);
                if (m_serialPortListener != null && m_state == ID_STATE_RECVING)
                    m_serialPortListener.OnRecv(m_lastRecvData, m_lastSendData, resp, req);
                SetState(ID_STATE_DONE);
                Logf.d(ID_TAG, "读取结束: " + m_lastRecvData);
                return m_lastRecvData.length();
            }
            else if(res == -2)
            {
                Logf.w(ID_TAG, "读取超时");
                SetState(ID_STATE_TIMEOUT);
                if (m_serialPortListener != null && (m_state == ID_STATE_SENDED || m_state == ID_STATE_RECVING))
                    m_serialPortListener.OnTimeout(m_lastSendData, timeout);
                return ENUM_RESULT_TIMEOUT;
            }
            return ENUM_RESULT_OTHER;
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
        Unlock();
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

    public int OpenMaintenanceDoor(String device, int timeout)
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
                m_serialPortListener.OnFatal("上次发送流程未结束");
            return ENUM_RESULT_LAST_NOT_FINISHED;
        }

        if(Common.StringIsBlank(device))
        {
            if (m_serialPortListener != null)
                m_serialPortListener.OnFatal("缺失门ID");
            return ENUM_RESULT_MISSING_PARAMETER;
        }

        SetDoorId(device);
        GetOpenDoorReqStruct req = new GetOpenDoorReqStruct(m_device);
        m_serialReq = req;
        m_serialResp = null;
        SerialSessionStruct session = new SerialSessionStruct();
        m_serialSession = session; // always new instance
        session.Request(req);

        boolean ok = Send(req);
        if(!ok)
        {
            if (m_serialPortListener != null)
                m_serialPortListener.OnFatal("发送失败");
            return ENUM_RESULT_SEND_ERROR;
        }

        if(timeout == 0) // 不等待
            return ENUM_RESULT_NOT_WAIT;
        else
        {
            int res = ReadData(timeout);
            if(res > 0) // success
            {
                GetOpenDoorRespStruct resp = new GetOpenDoorRespStruct();
                m_serialResp = resp;
                boolean suc = resp.Restore(m_lastRecvData);
                if(!suc)
                {
                    // TODO: 继续???
                }
                session.Response(resp);
                if (m_serialPortListener != null && m_state == ID_STATE_RECVING)
                    m_serialPortListener.OnRecv(m_lastRecvData, m_lastSendData, resp, req);
                SetState(ID_STATE_DONE);
                Logf.d(ID_TAG, "读取结束: " + m_lastRecvData);
                return 1;
            }
            else if(res == -2)
            {
                Logf.w(ID_TAG, "读取超时");
                SetState(ID_STATE_TIMEOUT);
                if (m_serialPortListener != null && (m_state == ID_STATE_SENDED || m_state == ID_STATE_RECVING))
                    m_serialPortListener.OnTimeout(m_lastSendData, timeout);
                return ENUM_RESULT_TIMEOUT;
            }
            return ENUM_RESULT_OTHER;
        }
    }

    // 结果暂时写入 m_lastRecvData
    private int ReadData(int timeout)
    {
        if(timeout == 0)
            return 0;

        if(!m_usingCallback)
            SetState(ID_STATE_RECVING);

        StringBuffer sb = new StringBuffer();
        String data = null;
        boolean r = true;
        while(r)
        {
            if(m_state == ID_STATE_RECVING)
            {
                m_buffer = m_serialPortDriver.ReadBuffer();
                //Logf.e(ID_TAG, m_buffer);
                //synchronized (m_buffer)
                {
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
            }

            if(data != null) // 数据接收完毕
            {
                int index = data.indexOf('{');
                String ret = data.substring(index);
                m_lastRecvData = ret;
                return ret.length();
            }

            if(timeout > 0)
            {
                long now = System.currentTimeMillis();
                if(now - m_sendTime >= timeout)
                {
                    return -2;
                }
            }
        }
        return -1;
    }

    public int Heartbeat(int timeout)
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
                m_serialPortListener.OnFatal("上次发送流程未结束");
            return ENUM_RESULT_LAST_NOT_FINISHED;
        }

        HeartbeatReqStruct req = new HeartbeatReqStruct();
        m_serialReq = req;
        m_serialResp = null;
        SerialSessionStruct session = new SerialSessionStruct();
        m_serialSession = session; // always new instance
        session.Request(req);

        boolean ok = Send(req);
        if(!ok)
        {
            if (m_serialPortListener != null)
                m_serialPortListener.OnFatal("发送失败");
            return ENUM_RESULT_SEND_ERROR;
        }

        if(timeout == 0) // 不等待
        {
            return ENUM_RESULT_NOT_WAIT;
        }
        else
        {
            int res = ReadData(timeout);
            if(res > 0) // success
            {
                HeartbeatRespStruct resp = new HeartbeatRespStruct();
                m_serialResp = resp;
                boolean suc = resp.Restore(m_lastRecvData);
                if(!suc)
                {
                    // TODO: 继续???
                }
                session.Response(resp);
                if (m_serialPortListener != null && m_state == ID_STATE_RECVING)
                    m_serialPortListener.OnRecv(m_lastRecvData, m_lastSendData, resp, req);
                SetState(ID_STATE_DONE);
                Logf.d(ID_TAG, "读取结束: " + m_lastRecvData);
                return m_lastRecvData.length();
            }
            else if(res == -2)
            {
                Logf.w(ID_TAG, "读取超时");
                SetState(ID_STATE_TIMEOUT);
                if (m_serialPortListener != null && (m_state == ID_STATE_SENDED || m_state == ID_STATE_RECVING))
                    m_serialPortListener.OnTimeout(m_lastSendData, timeout);
                return ENUM_RESULT_TIMEOUT;
            }
            return ENUM_RESULT_OTHER;
        }
    }

    public int SetDropMode(String dropMode, int timeout)
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
                m_serialPortListener.OnFatal("上次发送流程未结束");
            return ENUM_RESULT_LAST_NOT_FINISHED;
        }

        if(Common.StringIsBlank(dropMode))
        {
            if (m_serialPortListener != null)
                m_serialPortListener.OnFatal("缺失投放模式");
            return ENUM_RESULT_MISSING_PARAMETER;
        }

        DropModeReqStruct req = new DropModeReqStruct(dropMode);
        m_serialReq = req;
        m_serialResp = null;
        SerialSessionStruct session = new SerialSessionStruct();
        m_serialSession = session; // always new instance
        session.Request(req);

        boolean ok = Send(req);
        if(!ok)
        {
            if (m_serialPortListener != null)
                m_serialPortListener.OnFatal("发送失败");
            return ENUM_RESULT_SEND_ERROR;
        }

        if(timeout == 0) // 不等待
        {
            return ENUM_RESULT_NOT_WAIT;
        }
        else
        {
            int res = ReadData(timeout);
            if(res > 0) // success
            {
                DropModeRespStruct resp = new DropModeRespStruct();
                m_serialResp = resp;
                boolean suc = resp.Restore(m_lastRecvData);
                if(!suc)
                {
                    // TODO: 继续???
                }
                session.Response(resp);
                if (m_serialPortListener != null && m_state == ID_STATE_RECVING)
                    m_serialPortListener.OnRecv(m_lastRecvData, m_lastSendData, resp, req);
                SetState(ID_STATE_DONE);
                Logf.d(ID_TAG, "读取结束: " + m_lastRecvData);
                return m_lastRecvData.length();
            }
            else if(res == -2)
            {
                Logf.w(ID_TAG, "读取超时");
                SetState(ID_STATE_TIMEOUT);
                if (m_serialPortListener != null && (m_state == ID_STATE_SENDED || m_state == ID_STATE_RECVING))
                    m_serialPortListener.OnTimeout(m_lastSendData, timeout);
                return ENUM_RESULT_TIMEOUT;
            }
            return ENUM_RESULT_OTHER;
        }
    }

    private /*synchronized*/ void Lock()
    {
        //synchronized (this){
            m_lock = true;
        //}
    }

    private /*synchronized*/ boolean IsLock()
    {
        //synchronized (this){
            return m_lock;
        //}
    }

    private /*synchronized*/ void Unlock()
    {
       // synchronized (this){
            m_lock = false;
        //}
    }

    public boolean CanIO()
    {
        return !IsLock();
    }

    public static SerialPortDeviceDriver Instance()
    {
        if(_instance == null)
            _instance = new SerialPortDeviceDriver();
        return _instance;
    }
    /*private Queue<Runnable> m_queue = null;
    public void Push(Runnable runnable)
    {

    }*/
}
