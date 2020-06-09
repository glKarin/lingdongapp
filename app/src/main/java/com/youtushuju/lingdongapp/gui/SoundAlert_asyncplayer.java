package com.youtushuju.lingdongapp.gui;

import android.content.Context;
import android.media.AsyncPlayer;

public class SoundAlert_asyncplayer extends SoundAlert {
    private static final String ID_TAG = "SoundAlert_asyncplayer";

    private AsyncPlayer m_player = null;

    private AsyncPlayer MediaPlayer()
    {
        if(m_player == null)
        {
            m_player = new AsyncPlayer(ID_TAG + "::AsyncPlayer");
        }
        return m_player;
    }

    public SoundAlert_asyncplayer(Context context)
    {
        super(context);
    }

    @Override
    public void Play(String name) {

    }

    @Override
    public void Stop() {

    }

    @Override
    public void Reset() {

    }

    @Override
    public void Replay() {

    }

    @Override
    public void Shutdown() {

    }

    @Override
    public void PlayWithCallback(String name, MediaListener l) {

    }
}
