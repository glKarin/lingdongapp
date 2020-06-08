package com.youtushuju.lingdongapp.gui;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.media.MediaDataSource;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;

import androidx.annotation.NonNull;

import com.youtushuju.lingdongapp.common.Common;
import com.youtushuju.lingdongapp.common.Configs;
import com.youtushuju.lingdongapp.common.FD;
import com.youtushuju.lingdongapp.common.Logf;

import java.io.File;
import java.io.IOException;

public class SoundAlert {
    private static final String ID_TAG = "SoundAlert";
    private static final String ID_FILE_SUFFIX = "";

    private static final String CONST_ALERT_DIR = "alert";
    public static final String ID_SOUND_ALERT_WELCOME = "welcome.ogg";
    public static final String ID_SOUND_ALERT_OPERATION_SUCCESS = "operation_success.ogg";
    public static final String ID_SOUND_ALERT_OPERATION_ERROR = "operation_error.ogg";
    // 欢迎语，循环播放
    public static final String CONST_SOUND_ALERT_MESSAGE_WELCOME = CONST_ALERT_DIR + File.separator + "1.mp3"; // 智能垃圾分类，共享绿色环境 使用请按键、扫码或人脸识别后，投放垃圾，扫码下载APP，可获得积分兑换礼品
    // 垃圾箱满
    public static final String CONST_SOUND_ALERT_WARNING_KITCHEN_WASTE_FULL = CONST_ALERT_DIR + File.separator + "2.mp3"; // 厨余垃圾箱已满，请去附近投放
    public static final String CONST_SOUND_ALERT_WARNING_OTHER_WASTE_FULL = CONST_ALERT_DIR + File.separator + "3.mp3"; // 其他垃圾箱已满，请去附近投放
    public static final String CONST_SOUND_ALERT_WARNING_RECYCLE_WASTE_FULL = CONST_ALERT_DIR + File.separator + "4.mp3"; // 可回收垃圾箱已满，请去附近投放
    public static final String CONST_SOUND_ALERT_WARNING_DANGER_WASTE_FULL = CONST_ALERT_DIR + File.separator + "5.mp3"; // 有害垃圾箱已满，请去附近投放
    // 设备故障
    public static final String CONST_SOUND_ALERT_ERROR_DEVICE_BROKEN = CONST_ALERT_DIR + File.separator + "6.mp3"; // 系统故障，请去附近投放
    // 投放
    public static final String CONST_SOUND_ALERT_NOTIFICATION_DOOR_OPENING = CONST_ALERT_DIR + File.separator + "7.mp3"; // 投放口已开启，请尽快投递
    public static final String CONST_SOUND_ALERT_NOTIFICATION_DOOR_CLOSING = CONST_ALERT_DIR + File.separator + "8.mp3"; // 投放口即将关闭，请注意
    public static final String CONST_SOUND_ALERT_NOTIFICATION_DROP_FINISHED = CONST_ALERT_DIR + File.separator + "9.mp3"; // 投放已完成，登录APP可使用积分兑换礼物哦！
    // 清运
    public static final String CONST_SOUND_ALERT_NOTIFICATION_MAINTENANCE_FINISHED = CONST_ALERT_DIR + File.separator + "10.mp3"; // 清运门已开启，完成后请关门

    public static final String CONST_SOUND_ALERT_MESSAGE_START_FACE = null;
    public static final String CONST_SOUND_ALERT_ERROR_FACE_NOT_IDENTIFIED = null;
    public static final String CONST_SOUND_ALERT_ERROR_OPEN_DOOR_FAILED = CONST_SOUND_ALERT_ERROR_DEVICE_BROKEN; // null;

    private Context m_context = null;
    private MediaPlayer m_player = null;
    private MediaSource m_source = null;
    private MediaListener m_listener = null;
    private boolean m_async = true;

    private class MediaSource
    {
        public static final int ID_LOAD_RESULT_NEW_SUCCESS = 1;
        public static final int ID_LOAD_RESULT_NEW_ERROR = -1;
        public static final int ID_LOAD_RESULT_NOT_CHANGED = 0;
        public String name = null;
        private FD fd = null; // >= N

        public void Reset()
        {
            if(fd != null)
            {
                fd.Close();
                fd = null;
            }
            name = null;
        }

        public boolean Compare(@NonNull String newName)
        {
            if(newName.equals(name))
                return true;
            return false;
        }

        public int Load(@NonNull String n)
        {
            if(Compare(n))
                return ID_LOAD_RESULT_NOT_CHANGED;
            Reset();

            String filename = n + SoundAlert.ID_FILE_SUFFIX;
            name = n;

            fd = FD.Open(m_context, filename, Configs.ID_CONFIG_WORK_DIRECTORY + "/assets");
            return fd != null ? ID_LOAD_RESULT_NEW_SUCCESS : ID_LOAD_RESULT_NEW_ERROR;
        }

        public boolean PlayerLoadMedia(MediaPlayer player)
        {
            if(FD.LoadPlayerAsMedia(fd, player))
            {
                if(false) // 不准备媒体
                {
                    try
                    {
                        Logf.e(ID_TAG, fd);
                        player.prepare();
                        return true;
                    }
                    catch (IOException e)
                    {
                        App.HandleException(e);
                        return false;
                    }
                }
                else
                    return true;
            }
            else
                return false;
        }
    }

    public SoundAlert(Context context)
    {
        m_context = context;
        m_source = new MediaSource();
    }

    public void SetMediaListener(MediaListener l)
    {
        m_listener = l;
    }

    private MediaPlayer MediaPlayer()
    {
        if(m_player == null)
        {
            m_player = new MediaPlayer();
            m_player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    if(m_listener != null)
                    {
                        m_listener.OnEnd(m_source.name);
                        if(m_listener.Once())
                            SetMediaListener(null);
                    }
                }
            });
        }
        return m_player;
    }

    public void Play(String name)
    {
        MediaPlayer();

        if(Common.StringIsEmpty(name))
        {
            //Stop();
            return;
        }

        int res = m_source.Load(name);
        if(res == MediaSource.ID_LOAD_RESULT_NOT_CHANGED)
            Replay();
        else if(res == MediaSource.ID_LOAD_RESULT_NEW_SUCCESS)
        {
            Stop();
            if(m_source.PlayerLoadMedia(m_player))
            {
                //m_player.start();
                PrepareAndStart(null);
            }
            else
                Logf.e(ID_TAG, "加载新的媒体文件失败");
        }
        else
            Logf.e(ID_TAG, "加载媒体文件错误");
    }

    public void Stop()
    {
        if(m_player == null)
            return;

        if(m_player.isPlaying())
            m_player.stop();
        m_player.reset();
    }

    public void Reset()
    {
        Stop();
        m_source.Reset();
    }

    public void Replay()
    {
        if(m_player == null)
            return;

        if(m_player.isPlaying())
            m_player.pause();
        m_player.seekTo(0);
        m_player.start();
    }

    public void Shutdown()
    {
        Reset();
        m_player.release();
        m_player = null;
    }

    public void PlayWithCallback(String name, MediaListener l)
    {
        MediaPlayer();

        if(Common.StringIsEmpty(name))
        {
            //Stop();
            return;
        }

        int res = m_source.Load(name);
        if(res == MediaSource.ID_LOAD_RESULT_NOT_CHANGED)
        {
            SetMediaListener(l);
            Replay();
        }
        else if(res == MediaSource.ID_LOAD_RESULT_NEW_SUCCESS)
        {
            Stop();
            if(m_source.PlayerLoadMedia(m_player))
            {
                PrepareAndStart(l);
               /* SetMediaListener(l);
                m_player.start();*/
            }
            else
                Logf.e(ID_TAG, "加载新的媒体文件失败");
        }
        else
            Logf.e(ID_TAG, "加载媒体文件失败");
    }


    public interface MediaListener
    {
        public void OnEnd(String name);
        public boolean Once();
    }

    public void SetAsync(boolean on)
    {
        m_async = on;
    }

    private void PrepareAndStart(final MediaListener listener)
    {
        if(m_async)
        {
            m_player.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    SetMediaListener(listener);
                    mp.start();
                    mp.setOnPreparedListener(null);
                    Logf.e(ID_TAG, "异步准备媒体再播放");
                }
            });
            m_player.prepareAsync();
        }
        else
        {
            try
            {
                m_player.prepare();
            }
            catch (IOException e)
            {
                App.HandleException(e);
                return;
            }
            SetMediaListener(listener);
            m_player.start();
            Logf.e(ID_TAG, "同步准备媒体再播放");
        }
    }
}
