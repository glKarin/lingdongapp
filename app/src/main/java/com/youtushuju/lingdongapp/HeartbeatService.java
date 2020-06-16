package com.youtushuju.lingdongapp;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.preference.PreferenceManager;

import androidx.annotation.Nullable;

import com.youtushuju.lingdongapp.api.DeviceApi;
import com.youtushuju.lingdongapp.api.DeviceApiResp;
import com.youtushuju.lingdongapp.common.Common;
import com.youtushuju.lingdongapp.common.Configs;
import com.youtushuju.lingdongapp.common.Constants;
import com.youtushuju.lingdongapp.common.Logf;
import com.youtushuju.lingdongapp.common.Sys;
import com.youtushuju.lingdongapp.device.HeartbeatRespStruct;
import com.youtushuju.lingdongapp.device.SerialSessionStruct;
import com.youtushuju.lingdongapp.gui.App;
import com.youtushuju.lingdongapp.gui.SerialPortDeviceDriver;
import com.youtushuju.lingdongapp.gui.StatusMachine;
import com.youtushuju.lingdongapp.json.JsonMap;

import java.util.Timer;
import java.util.TimerTask;

public class HeartbeatService extends Service {
    private static final String ID_TAG = "HeartbeatService";
    private static final String CONST_SERVICE_NAME = "HEARTBEAT_SERVICE";
    private int m_timerInterval = 0;
    //private Timer m_timer = null;
    private HeartbeatBinder m_binder = new HeartbeatBinder();
    private static Intent _intent = null;
    private HandlerThread m_thread;
    private Handler m_handler;
    private HeartbeatTimerTask m_timerTask = new HeartbeatTimerTask();

    private class HeartbeatTimerTask
            // extends TimerTask
        implements Runnable
    {
        public void run()
        {
            if(m_timerInterval <= 0) // 第一次心跳时为0
                m_timerInterval = Configs.CONST_DEFAULT_HEARTBEAT_INTERVAL;
            StatusMachine statusMachine = StatusMachine.Instance();
            try
            {
                long ts = System.currentTimeMillis();
                Logf.e(ID_TAG, "开始同步心跳: " + Common.TimestampToStr(ts));
                statusMachine.heartbeat_start_timestamp = ts;
                statusMachine.heartbeat_count++;

                String res = DoHeartbeat();
                if(Common.StringIsEmpty(res)) // !!!如果心跳IO时串口读写被占用/请求+答复有错误, 则不修改当前的设备状态!!!
                {
                    Logf.e(ID_TAG, "串口返回心跳设备状态错误");
                    statusMachine.heartbeat_err_count++;
                    return;
                }

                statusMachine.device_status = res;
                String desc = HeartbeatRespStruct.GetDeviceStatusName(res);
                m_binder.GetDeviceStatus(res, desc);
                /*if(true)
                    return;*/
                DeviceApiResp resp = DeviceApi.Heartbeat(Sys.GetIMEI(HeartbeatService.this), res, desc);
                // test
                /*String __mode = "[01]";
                m_binder.GetDropMode(__mode);
                statusMachine.drop_mode = __mode;*/

                if(resp == null)
                {
                    Logf.e(ID_TAG, "心跳Api请求错误");
                    statusMachine.heartbeat_err_count++;
                    return;
                }
                if(!resp.IsSuccess())
                {
                    Logf.e(ID_TAG, "心跳Api请求错误: " + resp.ResultString());
                    statusMachine.heartbeat_err_count++;
                    return;
                }
                if(resp.data == null || resp.data instanceof String) // device not registered
                {
                    Logf.e(ID_TAG, "心跳Api错误: " + resp.data);
                    statusMachine.heartbeat_err_count++;
                    return;
                }

                JsonMap data = (JsonMap)resp.data;
                if(!data.Contains("heartbeatTime") || !data.Contains("dropmode"))
                {
                    Logf.e(ID_TAG, "心跳Api缺失必要字段: " + resp.data);
                    statusMachine.heartbeat_err_count++;
                    return;
                }

                int heartbeatTime = (int)data.get("heartbeatTime"); // 分钟
                m_timerInterval = Math.max(heartbeatTime * 60000, Configs.CONST_DEFAULT_HEARTBEAT_INTERVAL); // 毫秒
                String dropmode = data.<String>GetT("dropmode");
                Logf.e(ID_TAG, "获取投放模式: " + dropmode);
                statusMachine.drop_mode = dropmode;
                m_binder.GetDropMode(dropmode);

                try
                {
                    Thread.sleep(5000);
                }
                catch (Exception e)
                {
                    App.HandleException(e);
                }

                DoSetDropMode(dropmode);
                statusMachine.heartbeat_suc_count++;
                statusMachine.heartbeat_timestamp = ts;
            }
            catch (Throwable e)
            {
                App.HandleException(e);
                statusMachine.heartbeat_err_count++;
                Logf.e(ID_TAG, "心跳数据解析错误: " + Common.Now());
            }
            finally {
                // next heartbeat
                Logf.e(ID_TAG, "下次心跳时间间隔: " + m_timerInterval);
                //m_timerInterval = 10000; // for test
                /*HeartbeatTimerTask m_timerTask = new HeartbeatTimerTask();
                try
                {
                    // !!! m_timer.cancel(); !!!
                    m_timer.purge();
                    m_timer.schedule(m_timerTask, m_timerInterval);
                }
                catch (Exception e)
                {
                    Logf.e(ID_TAG, "启动下次心跳任务异常");
                    e.printStackTrace(); // TODO: sometime throw task canceled.
                }*/
                if(m_handler != null)
                    m_handler.postDelayed(m_timerTask, m_timerInterval);
            }
        }

        private String DoHeartbeat()
        {
            SerialPortDeviceDriver driver = SerialPortDeviceDriver.Instance();
            if(!driver.CanIO())
            {
                Logf.e(ID_TAG, "心跳IO时串口被占用");
                return null;
            }
            SerialPortDeviceDriver.IOResult res = driver.IO(SerialPortDeviceDriver.ENUM_ACTION_HEARTBEAT, -1); // 会阻塞线程
            String ret = null;
            if(!res.IsSuccess())
            {
                // TODO: 处理???
                Logf.e(ID_TAG, "获取心跳设备状态->读写错误: " + res.res);
            }
            else if(!res.SessionIsSuccess())
            {
                // TODO: 处理???
                String reason = "对话为NULL";
                if(res.session.req != null)
                    reason = "请求: " + res.session.req.toString();
                else
                    reason = "请求: NULL";
                if(res.session.resp != null)
                    reason += ", 答复: " + res.session.resp.toString();
                else
                    reason += ", 答复: NULL";
                Logf.e(ID_TAG, "获取心跳设备状态->对话无效: " + reason);
            }
            else
            {
                SerialSessionStruct session = res.session;
                HeartbeatRespStruct resp = (HeartbeatRespStruct)session.resp;
                ret = resp.res;
                Logf.e(ID_TAG, "获取心跳设备状态->结果: " + ret);
            }
            return ret;
        }

        private boolean DoSetDropMode(String dropmode)
        {
            SerialPortDeviceDriver driver = SerialPortDeviceDriver.Instance();
            if(!driver.CanIO())
            {
                Logf.e(ID_TAG, "设置投放模式IO时串口被占用");
                return false;
            }
            int timeout = 5000; // 0
            SerialPortDeviceDriver.IOResult res = driver.IO(SerialPortDeviceDriver.ENUM_ACTION_DROP_SET_MODE, timeout, dropmode); // 会阻塞线程
            boolean ret = res.IsSuccess();
            if(!ret)
            {
                // TODO: 处理???
                Logf.e(ID_TAG, "传递心跳投放模式失败: " + res.res + "???(一般该错误可忽略)");
            }
            return ret;
        }
    }

    public class HeartbeatBinder extends Binder
    {
        private DeviceStatusListener m_listener = null;

        public void SetDeviceStatusListener(DeviceStatusListener l)
        {
            m_listener = l;
        }

        private void GetDeviceStatus(String code, String desc)
        {
            if(m_listener != null)
                m_listener.OnGetDeviceStatus(code, desc);
        }

        private void GetDropMode(String mode)
        {
            if(m_listener != null)
                m_listener.OnGetDropMode(mode);
        }
    }

    public interface DeviceStatusListener
    {
        public void OnGetDeviceStatus(String code, String desc);
        public void OnGetDropMode(String mode);
    }

    @Override
    public void onCreate() {
        Logf.d(ID_TAG, "启动心跳服务");
        super.onCreate();
        m_thread = new HandlerThread("_Background_thread");
        m_thread.start();
        m_handler = new Handler(m_thread.getLooper());
        m_handler.postDelayed(m_timerTask, 1000);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Logf.d(ID_TAG, "开始心跳服务");
        /*if(m_timer == null)
        {
            m_timer = new Timer();
            HeartbeatTimerTask m_timerTask = new HeartbeatTimerTask();
            m_timer.schedule(m_timerTask, 1000);
        }*/
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        Logf.d(ID_TAG, "销毁心跳服务");
        super.onDestroy();

        /*if(m_timer != null)
        {
            m_timer.cancel();
            m_timer.purge();
        }*/

        m_thread.quit();
        m_handler = null;
        m_thread = null;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Logf.d(ID_TAG, "绑定心跳服务");
        return m_binder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        //return super.onUnbind(intent);
        Logf.d(ID_TAG, "解绑心跳服务");
        return true;
    }

    public static void Start(Context context)
    {
        if(_intent != null)
            return;
        _intent = new Intent();
        String packageName = context.getPackageName();
        _intent.setAction(packageName + "." + CONST_SERVICE_NAME);
        _intent.setPackage(packageName);
        context.startService(_intent);
    }

    public static void Stop(Context context)
    {
        if(_intent == null)
            return;
        context.stopService(_intent);
        _intent = null;
    }

    public static void Bind(Context context, ServiceConnection conn)
    {
        Intent intent = new Intent();
        String packageName = context.getPackageName();
        intent.setAction(packageName + "." + CONST_SERVICE_NAME);
        intent.setPackage(packageName);
        context.bindService(intent, conn, 0);
    }

    public static void Unbind(Context context, ServiceConnection conn)
    {
        context.unbindService(conn);
    }
}
