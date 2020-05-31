package com.youtushuju.lingdongapp;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Binder;
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
import com.youtushuju.lingdongapp.gui.SerialPortDeviceDriver;
import com.youtushuju.lingdongapp.json.JsonMap;

import java.util.Timer;
import java.util.TimerTask;

public class HeartbeatService extends Service {
    private static final String ID_TAG = "HeartbeatService";
    private int m_timerInterval = 2000;
    private Timer m_timer = null;
    private HeartbeatTimerTask m_timerTask = null;
    private HeartbeatBinder m_binder = new HeartbeatBinder();
    private static Intent _intent = null;

    private class HeartbeatTimerTask extends TimerTask {
        public void run()
        {
            try
            {
                String res = DoHeartbeat();
                if(Common.StringIsEmpty(res))
                {
                    Logf.e(ID_TAG, "串口返回心跳设备状态错误");
                    return;
                }
                DeviceApiResp resp = DeviceApi.Heartbeat(Sys.GetIMEI(HeartbeatService.this), res, HeartbeatRespStruct.GetDeviceStatusName(res));
                if(resp == null)
                {
                    Logf.e(ID_TAG, "心跳Api请求错误");
                    return;
                }
                if(!resp.IsSuccess())
                {
                    Logf.e(ID_TAG, "心跳Api请求错误: " + resp.ResultString());
                    return;
                }
                if(resp.data == null || resp.data instanceof String) // device not registered
                {
                    Logf.e(ID_TAG, "心跳设备错误: " + resp.data);
                    return;
                }

                JsonMap data = (JsonMap)resp.data;
                int heartbeatTime = (int)data.get("heartbeatTime"); // 分钟
                m_timerInterval = Math.max(heartbeatTime * 60000, Configs.CONST_DEFAULT_HEARTBEAT_INTERVAL); // 毫秒
                String dropmode = data.<String>GetT("dropmode");

                DoSetDropMode(dropmode);
            }
            catch (Throwable e)
            {
                e.printStackTrace();
                Logf.e(ID_TAG, "心跳数据解析错误: " + Common.Now());
            }
            finally {
                // next heartbeat
                m_timer.purge();
                Logf.e(ID_TAG, m_timerInterval);
                //m_timerInterval = 10000; // for test
                m_timerTask = new HeartbeatTimerTask();
                m_timer.schedule(m_timerTask, m_timerInterval);
            }
        }

        private String DoHeartbeat()
        {
            SerialPortDeviceDriver driver = SerialPortDeviceDriver.Instance();
            if(!driver.CanIO())
                return null;
            SerialPortDeviceDriver.IOResult res = driver.IO(SerialPortDeviceDriver.ENUM_ACTION_HEARTBEAT, -1); // 会阻塞线程
            String ret = null;
            if(!res.IsSuccess())
            {
                // TODO: 处理???
                Logf.e(ID_TAG, "获取心跳设备状态失败: " + res.res);
            }
            else
            {
                SerialSessionStruct session = res.session;
                HeartbeatRespStruct resp = (HeartbeatRespStruct)session.resp;
                ret = resp.res;
                Logf.e(ID_TAG, "获取心跳设备状态结果: " + ret);
            }
            return ret;
        }

        private boolean DoSetDropMode(String dropmode)
        {
            SerialPortDeviceDriver driver = SerialPortDeviceDriver.Instance();
            if(!driver.CanIO())
                return false;
            SerialPortDeviceDriver.IOResult res = driver.IO(SerialPortDeviceDriver.ENUM_ACTION_DROP_SET_MODE, 0, dropmode); // 会阻塞线程
            boolean ret = res.IsSuccess();
            if(!ret)
            {
                // TODO: 处理???
                Logf.e(ID_TAG, "传递心跳投放模式失败: " + res.res);
            }
            return ret;
        }
    }

    public class HeartbeatBinder extends Binder
    {
    }

    @Override
    public void onCreate() {
        Logf.d(ID_TAG, "启动心跳服务");
        super.onCreate();
        m_timerInterval = Configs.CONST_DEFAULT_HEARTBEAT_INTERVAL;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Logf.d(ID_TAG, "开始心跳服务");
        if(m_timer == null)
        {
            m_timer = new Timer();
            m_timerTask = new HeartbeatTimerTask();
            m_timer.schedule(m_timerTask, 1000);
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        Logf.d(ID_TAG, "销毁心跳服务");
        super.onDestroy();

        if(m_timer != null)
        {
            m_timer.cancel();
            m_timer.purge();
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Logf.d(ID_TAG, "绑定心跳服务");
        return m_binder;
    }

    public static void Start(Context context)
    {
        if(_intent != null)
            return;
        _intent = new Intent();
        String packageName = context.getPackageName();
        _intent.setAction(packageName + ".HEARTBEAT_SERVICE");
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
}
