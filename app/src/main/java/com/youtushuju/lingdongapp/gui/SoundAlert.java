package com.youtushuju.lingdongapp.gui;

import android.content.Context;
import android.media.AsyncPlayer;
import android.media.MediaPlayer;

import androidx.annotation.NonNull;

import com.youtushuju.lingdongapp.common.Common;
import com.youtushuju.lingdongapp.common.Configs;
import com.youtushuju.lingdongapp.common.FD;
import com.youtushuju.lingdongapp.common.Logf;

import java.io.File;
import java.io.IOException;

abstract public class SoundAlert {
    private static final String ID_TAG = "SoundAlert";
    private static final String ID_FILE_SUFFIX = "";

    public static final String ID_SOUND_ALERT_WELCOME = "welcome.ogg";
    public static final String ID_SOUND_ALERT_OPERATION_SUCCESS = "operation_success.ogg";
    public static final String ID_SOUND_ALERT_OPERATION_ERROR = "operation_error.ogg";
    private static final String CONST_SOUND_MEDIA_SUFFIX = ".mp3";
    //private static final String CONST_SOUND_MEDIA_SUFFIX = ".ogg";
    // 欢迎语，循环播放
    public static final String CONST_SOUND_ALERT_MESSAGE_WELCOME = "1" + CONST_SOUND_MEDIA_SUFFIX; // 智能垃圾分类，共享绿色环境 使用请按键、扫码或人脸识别后，投放垃圾，扫码下载APP，可获得积分兑换礼品
    // 垃圾箱满
    public static final String CONST_SOUND_ALERT_WARNING_KITCHEN_WASTE_FULL = "2" + CONST_SOUND_MEDIA_SUFFIX; // 厨余垃圾箱已满，请去附近投放
    public static final String CONST_SOUND_ALERT_WARNING_OTHER_WASTE_FULL = "3" + CONST_SOUND_MEDIA_SUFFIX; // 其他垃圾箱已满，请去附近投放
    public static final String CONST_SOUND_ALERT_WARNING_RECYCLE_WASTE_FULL = "4" + CONST_SOUND_MEDIA_SUFFIX; // 可回收垃圾箱已满，请去附近投放
    public static final String CONST_SOUND_ALERT_WARNING_DANGER_WASTE_FULL = "5" + CONST_SOUND_MEDIA_SUFFIX; // 有害垃圾箱已满，请去附近投放
    // 设备故障
    public static final String CONST_SOUND_ALERT_ERROR_DEVICE_BROKEN = "6" + CONST_SOUND_MEDIA_SUFFIX; // 系统故障，请去附近投放
    // 投放
    public static final String CONST_SOUND_ALERT_NOTIFICATION_DOOR_OPENING = "7" + CONST_SOUND_MEDIA_SUFFIX; // 投放口已开启，请尽快投递
    public static final String CONST_SOUND_ALERT_NOTIFICATION_DOOR_CLOSING = "8" + CONST_SOUND_MEDIA_SUFFIX; // 投放口即将关闭，请注意
    public static final String CONST_SOUND_ALERT_NOTIFICATION_DROP_FINISHED = "9" + CONST_SOUND_MEDIA_SUFFIX; // 投放已完成，登录APP可使用积分兑换礼物哦！
    // 清运
    public static final String CONST_SOUND_ALERT_NOTIFICATION_MAINTENANCE_FINISHED = "10" + CONST_SOUND_MEDIA_SUFFIX; // 清运门已开启，完成后请关门

    public static final String CONST_SOUND_ALERT_MESSAGE_START_FACE = null;
    public static final String CONST_SOUND_ALERT_ERROR_FACE_NOT_IDENTIFIED = null;
    public static final String CONST_SOUND_ALERT_ERROR_OPEN_DOOR_FAILED = CONST_SOUND_ALERT_ERROR_DEVICE_BROKEN; // null;

    protected Context m_context = null;
    protected MediaSource m_source = null;
    protected MediaListener m_listener = null;
    protected boolean m_async = true;

    protected class MediaSource
    {
        public static final int ID_LOAD_RESULT_NEW_SUCCESS = 1;
        public static final int ID_LOAD_RESULT_NEW_ERROR = -1;
        public static final int ID_LOAD_RESULT_NOT_CHANGED = 0;

        public String name = null;
        public FD fd = null; // >= N

        public void Reset()
        {
            Close();
            name = null;
        }

        public void Close()
        {
            if(fd != null)
            {
                fd.Close();
                fd = null;
            }
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
            boolean ok = (FD.LoadPlayerAsMedia(fd, player));
            Close();
            if(ok)
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

    public abstract void Play(String name);
    public abstract void Stop();
    public abstract void Reset();
    public abstract void Replay();
    public abstract void Shutdown();
    public abstract void PlayWithCallback(String name, MediaListener l);

    public interface MediaListener
    {
        public void OnEnd(String name);
        public boolean Once();
    }

    public void SetAsync(boolean on)
    {
        m_async = on;
    }
}
