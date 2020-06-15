package com.youtushuju.lingdongapp.gui;

import android.os.Handler;
import android.view.View;

import com.youtushuju.lingdongapp.MainActivity;
import com.youtushuju.lingdongapp.R;

abstract public class MainScreenSaverView {
    protected static final int ID_SCREEN_SAVER_ANIM_DELAY = 500;
    protected MainActivity m_mainActivity;
    protected View m_view;
    protected Handler m_handler;
    private Runnable m_openScreenSaver = new Runnable() {
        @Override
        public void run() {
            onResume();
        }
    };
    private Runnable m_closeScreenSaver = new Runnable() {
        @Override
        public void run() {
            onPause();
        }
    };

    public MainScreenSaverView(MainActivity activity, Handler handler)
    {
        m_mainActivity = activity;
        m_handler = handler;
        m_view = activity.findViewById(R.id.main_screensaver_view);
    }

    public abstract void Setup();
    public abstract void onResume();
    public abstract void onPause();
    public abstract void NotifyDeviceStatus(String code, String message);
    public abstract void SetDropMode(String mode);

    public boolean IsVisible()
    {
        return m_view.getVisibility() == View.VISIBLE;
    }

    // 打开屏保
    public void Open(boolean anim)
    {
        if(anim)
        {
            m_view.animate().setDuration(ID_SCREEN_SAVER_ANIM_DELAY).alpha(1.0f).withStartAction(m_openScreenSaver).start();
        }
        else
        {
            m_openScreenSaver.run();
        }
    }

    // 关闭屏保
    public void Close(boolean anim)
    {
        if(anim)
        {
            m_view.animate().setDuration(ID_SCREEN_SAVER_ANIM_DELAY).alpha(0.0f).withEndAction(m_closeScreenSaver).start();
        }
        else
        {
            m_closeScreenSaver.run();
        }
    }
}
