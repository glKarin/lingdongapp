package com.youtushuju.lingdongapp;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.preference.PreferenceManager;

import com.youtushuju.lingdongapp.common.Configs;
import com.youtushuju.lingdongapp.common.Constants;
import com.youtushuju.lingdongapp.common.Logf;
import com.youtushuju.lingdongapp.gui.App;

import java.util.Timer;

public class BackgroundService extends Service {
    private static final String ID_TAG = "BackgroundService";
    private static final String CONST_SERVICE_NAME = "BACKGROUND_SERVICE";
    private int m_playEndInterval = Configs.ID_PREFERENCE_DEFAULT_BGM_INTERVAL;
    private BackgroundBinder m_binder = new BackgroundBinder();
    private static Intent _intent = null;
    private MediaPlayer m_player = null;
    private Timer m_timer = null;
    private HandlerThread m_thread;
    private Handler m_handler;
    private Runnable m_runnable = new Runnable() {
        @Override
        public void run() {
            //Play();
            Replay();
        }
    };

    private MediaPlayer.OnCompletionListener m_playEndListener = new MediaPlayer.OnCompletionListener()
    {
        @Override
        public void onCompletion(MediaPlayer mp) {
            if(m_playEndInterval > 0)
            {
                /*try
                {
                    m_timer.purge();
                    m_timer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            Play();
                        }
                    }, m_playEndInterval);
                }
                catch (Exception e)
                {
                    Logf.e(ID_TAG, "启动下次心跳任务异常");
                    e.printStackTrace(); // TODO: sometime throw task canceled.
                }*/
                if(m_handler != null)
                    m_handler.postDelayed(m_runnable, m_playEndInterval);
            }
        }
    };

    public class BackgroundBinder extends Binder
    {
        public void Play()
        {
            BackgroundService.this.Play();
        }

        public void Stop()
        {
            BackgroundService.this.Stop();
        }

        public void Pause()
        {
            Logf.e(ID_TAG, "暂停播放背景音乐");
            BackgroundService.this.Pause();
            m_handler.removeCallbacks(m_runnable);
        }

        public void Replay()
        {
            Logf.e(ID_TAG, "开始播放背景音乐");
            m_handler.post(m_runnable);
            //BackgroundService.this.Replay();
        }
    }

    @Override
    public void onCreate() {
        Logf.d(ID_TAG, "启动背景服务");
        super.onCreate();
        m_thread = new HandlerThread("_Background_thread");
        m_thread.start();
        m_handler = new Handler(m_thread.getLooper());

        try
        {
            m_playEndInterval = Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(this).getString(Constants.ID_PREFERENCE_BGM_INTERVAL, "" + Configs.ID_PREFERENCE_DEFAULT_BGM_INTERVAL));
        }
        catch (Exception e)
        {
            App.HandleException(e);
            m_playEndInterval = Configs.ID_PREFERENCE_DEFAULT_BGM_INTERVAL;
        }
        Logf.e(ID_TAG, "背景音播放间隔: " + m_playEndInterval);
        m_player = MediaPlayer.create(this, R.raw.bgm2);
        if(m_playEndInterval <= 0)
            m_player.setLooping(true);
        /*else
            m_timer = new Timer();*/
        m_player.setOnCompletionListener(m_playEndListener);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Logf.d(ID_TAG, "开始背景服务");
        this.Play();
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        Logf.d(ID_TAG, "销毁背景服务");
        super.onDestroy();
        if(m_timer != null)
        {
            m_timer.purge();
            m_timer.cancel();
            m_timer = null;
        }

        this.Stop();
        if(m_player != null)
        {
            m_player.release();
            m_player = null;
        }

        m_thread.quit();
        m_thread = null;
        m_handler = null;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Logf.d(ID_TAG, "绑定背景服务");
        return m_binder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        //return super.onUnbind(intent);
        Logf.d(ID_TAG, "解绑背景服务");
        return true;
    }

    private void Play()
    {
        if(!PlayerIsValid())
            return;
        if(!m_player.isPlaying())
        {
            m_player.start();
        }
    }

    private void Replay()
    {
        if(!PlayerIsValid())
            return;
        if(m_player.isPlaying())
            m_player.pause();
        m_player.seekTo(0);
        m_player.start();
    }

    private void Pause()
    {
        if(!PlayerIsValid())
            return;
        if(m_player.isPlaying())
        {
            m_player.pause();
        }
    }

    private void Stop()
    {
        if(!PlayerIsValid())
            return;
        if(m_player.isPlaying())
        {
            m_player.stop();
        }
    }

    public boolean PlayerIsValid()
    {
        return m_player != null;
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
