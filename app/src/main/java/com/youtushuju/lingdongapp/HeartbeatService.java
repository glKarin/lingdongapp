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
import com.youtushuju.lingdongapp.common.Configs;
import com.youtushuju.lingdongapp.common.Constants;
import com.youtushuju.lingdongapp.common.Logf;
import com.youtushuju.lingdongapp.common.Sys;

import java.util.Timer;
import java.util.TimerTask;

public class HeartbeatService extends Service {
    private static final String ID_TAG = "HeartbeatService";
    private int m_timerInterval = Configs.ID_PREFERENCE_DEFAULT_HEARTBEAT_INTERVAL;
    private Timer m_timer = null;
    private HeartbeatTimerTask m_timerTask = null;
    private HeartbeatBinder m_binder = new HeartbeatBinder();
    private static Intent _intent = null;

    private class HeartbeatTimerTask extends TimerTask {
        public void run()
        {
            DeviceApi.Heartbeat(Sys.GetIMEI(HeartbeatService.this), "1", "在线");
        }
    }

    public class HeartbeatBinder extends Binder
    {
    }

    @Override
    public void onCreate() {
        Logf.d(ID_TAG, "启动心跳服务");
        super.onCreate();

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        try
        {
            m_timerInterval = Integer.parseInt(preferences.getString(Constants.ID_PREFERENCE_HEARTBEAT_INTERVAL, "" + Configs.ID_PREFERENCE_DEFAULT_HEARTBEAT_INTERVAL));
         }
        catch (Exception e)
        {
            e.printStackTrace();
            m_timerInterval = Configs.ID_PREFERENCE_DEFAULT_HEARTBEAT_INTERVAL;
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Logf.d(ID_TAG, "开始心跳服务");
        if(m_timer == null)
        {
            m_timer = new Timer();
            m_timerTask = new HeartbeatTimerTask();
            m_timer.scheduleAtFixedRate(m_timerTask, 1000, m_timerInterval);
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
