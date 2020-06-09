package com.youtushuju.lingdongapp.gui;

import android.content.Context;
import android.media.MediaPlayer;

import androidx.annotation.NonNull;

import com.youtushuju.lingdongapp.common.Common;
import com.youtushuju.lingdongapp.common.Configs;
import com.youtushuju.lingdongapp.common.FD;
import com.youtushuju.lingdongapp.common.Logf;

import java.io.File;
import java.io.IOException;

public class SoundAlert_mediaplayer extends SoundAlert{
    private static final String ID_TAG = "SoundAlert_mediaplayer";

    private static final String CONST_ALERT_DIR = "alert";

    private MediaPlayer m_player = null;

    public SoundAlert_mediaplayer(Context context)
    {
        super(context);
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
            m_player.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                @Override
                public boolean onError(MediaPlayer mp, int what, int extra) {
                    Logf.e(ID_TAG, "播放音频发生错误: " + m_source.name + " -> " + what + "(" + extra + ")");
                    return false;
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

        int res = m_source.Load(GetFileName(name));
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

        int res = m_source.Load(GetFileName(name));
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

    private String GetFileName(String name)
    {
        return CONST_ALERT_DIR + File.separator + name;
    }
}
