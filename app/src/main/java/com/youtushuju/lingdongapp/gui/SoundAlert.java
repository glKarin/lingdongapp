package com.youtushuju.lingdongapp.gui;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.media.MediaDataSource;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;

import androidx.annotation.NonNull;

import com.youtushuju.lingdongapp.common.Logf;

import java.io.IOException;

public class SoundAlert {
    private static final String ID_TAG = "SoundAlert";
    private static final String ID_FILE_SUFFIX = "";

    public static final String ID_SOUND_ALERT_WELCOME = "welcome.ogg";
    public static final String ID_SOUND_ALERT_OPERATION_SUCCESS = "operation_success.ogg";
    public static final String ID_SOUND_ALERT_OPERATION_ERROR = "operation_error.ogg";

    private Context m_context = null;
    private MediaPlayer m_player = null;
    private MediaSource m_source = null;

    private class MediaSource
    {
        public static final int ID_LOAD_RESULT_NEW_SUCCESS = 1;
        public static final int ID_LOAD_RESULT_NEW_ERROR = -1;
        public static final int ID_LOAD_RESULT_NOT_CHANGED = 0;
        public String name = null;
        private Uri uri = null;
        private AssetFileDescriptor fd = null; // >= N

        public void Reset()
        {
            if(fd != null)
            {
                try
                {
                    fd.close();
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
                fd = null;
            }
            name = null;
            uri = null;
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
            uri = Uri.parse("file:///android_asset/" + filename);
            name = n;

            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) // 牛轧糖
            {
                try
                {
                    fd = m_context.getAssets().openFd(filename);
                    return fd != null ? ID_LOAD_RESULT_NEW_SUCCESS : ID_LOAD_RESULT_NEW_ERROR;
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                    return ID_LOAD_RESULT_NEW_ERROR;
                }
            }
            return ID_LOAD_RESULT_NEW_SUCCESS;
        }

        public boolean PlayerLoadMedia(MediaPlayer player)
        {
            try
            {
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) // 牛轧糖
                {
                    //Logf.e(ID_TAG, fd);
                    if(fd != null)
                        player.setDataSource(fd);
                    else
                        return false;
                }
                else
                {
                    player.setDataSource(m_context, uri);
                }
                player.prepare();
            }
            catch (IOException e)
            {
                e.printStackTrace();
                return false;
            }
            return true;
        }
    }

    public SoundAlert(Context context)
    {
        m_context = context;
        m_source = new MediaSource();
    }

    public void Play(String name)
    {
        if(m_player == null)
            m_player = new MediaPlayer();

        int res = m_source.Load(name);
        if(res == MediaSource.ID_LOAD_RESULT_NOT_CHANGED)
            Replay();
        else if(res == MediaSource.ID_LOAD_RESULT_NEW_SUCCESS)
        {
            Stop();
            m_source.PlayerLoadMedia(m_player);
            m_player.start();
        }
        else
            Logf.e(ID_TAG, "加载媒体文件失败");
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
}
