package com.youtushuju.lingdongapp.gui;

import android.os.Handler;
import android.view.View;
import android.webkit.JavascriptInterface;

import com.youtushuju.lingdongapp.MainActivity;
import com.youtushuju.lingdongapp.R;

public class MainScreenSaverView_webview extends MainScreenSaverView {
    private static final String ID_TAG = "MainScreenSaverView_webview";
    private ScreenSaverView m_webView;

    public MainScreenSaverView_webview(MainActivity view, Handler handler)
    {
        super(view, handler);

        m_webView = (ScreenSaverView)m_mainActivity.findViewById(R.id.main_screensaver_content);
    }

    public void Setup()
    {
        m_webView.SetNativeObject(m_windowObject);
    }

    public void onResume()
    {
        m_webView.onResume();
        m_webView.Load();
        m_view.setVisibility(View.VISIBLE);
    }

    public void onPause()
    {
        m_webView.onPause();
        //m_view.setVisibility(View.INVISIBLE);
        m_view.setVisibility(View.GONE);
    }

    public void NotifyDeviceStatus(String code, String desc)
    {
        m_windowObject.NotifyDeviceStatus(code, desc);
    }

    public void SetDropMode(String mode)
    {
        // UNIMPLEMENTS
    }

    // 屏保webview宿主对象
    private ScreenSaverWindowObject m_windowObject = new ScreenSaverWindowObject();
    private class ScreenSaverWindowObject extends ScreenSaverView.WindowObject {
        public ScreenSaverWindowObject()
        {
            super(m_mainActivity, MainScreenSaverView_webview.this.m_handler);
        }
        // 开始刷脸
        @JavascriptInterface
        public void ToFace(final String name)
        {
            m_mainActivity.ToDropWaste(name);
        }

        @JavascriptInterface
        public void OpenMenu()
        {
            m_mainActivity.ToOpenMenu();
        }

        public void NotifyDeviceStatus(String code, String desc)
        {
            CallJSFunc("setbottomMsg", desc, code);
        }
    };
}
