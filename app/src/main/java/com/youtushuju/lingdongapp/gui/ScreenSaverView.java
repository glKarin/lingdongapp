package com.youtushuju.lingdongapp.gui;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.webkit.JavascriptInterface;
import android.webkit.JsResult;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.youtushuju.lingdongapp.R;
import com.youtushuju.lingdongapp.common.Common;
import com.youtushuju.lingdongapp.common.Logf;

import java.util.Arrays;
import java.util.List;

public class ScreenSaverView extends WebView
{
    private static final String ID_TAG = "ScreenSaverView";
    private static final String ID_WINDOW_OBJECT_NAME = "_Native_object";
    //private static final String ID_SCREENSAVER_URL = "file:///android_asset/screensaver.html";
    private static final String ID_SCREENSAVER_URL = "http://ryce2-test.youtushuju.cn/screensaver.html";

    private WindowObject m_object = null;
    private Handler m_handler = new Handler();

    public ScreenSaverView(Context context)
    {
        super(context);
        Setup();
    }

    public ScreenSaverView(Context context, AttributeSet attributeSet)
    {
        super(context, attributeSet);
        Setup();
    }

    public ScreenSaverView(Context context, AttributeSet attributeSet, int defStyleAttr)
    {
        super(context, attributeSet, defStyleAttr);
        Setup();
    }

    private void Setup() {
        WebSettings settings = getSettings();

        settings.setJavaScriptEnabled(true);
        settings.setUseWideViewPort(true);
        settings.setLoadWithOverviewMode(true);
        settings.setBuiltInZoomControls(false);
        settings.setDisplayZoomControls(false);
        settings.setSupportZoom(false);

        m_object = new WindowObject();
        addJavascriptInterface(m_object, ID_WINDOW_OBJECT_NAME);

        setWebChromeClient(new WebChromeClient(){
            @Override
            public boolean onJsAlert(WebView view, String url, String message, final JsResult result) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setTitle("Alert");
                builder.setMessage(message);
                builder.setIcon(R.drawable.icon_profile);
                builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        result.confirm();
                    }
                });
                AlertDialog dialog = builder.create();
                dialog.show();
                return true;
            }
        });
        setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                m_handler.post(new Runnable() {
                    @Override
                    public void run() {
                        loadUrl("javascript:typeof(OnPageFinished) == 'function' && OnPageFinished();");
                    }
                });
            }
        });
    }

    public void Load()
    {
        loadUrl(ID_SCREENSAVER_URL);
    }

    private class WindowObject
    {
        public WindowObject()
        {

        }

        @JavascriptInterface
        public void Get(final String name, final Object defaultValue)
        {
            m_handler.post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getContext(), String.format("获取属性: name(%s), 默认值(%s)", name, defaultValue != null ? defaultValue.toString() : null), Toast.LENGTH_SHORT).show();
                    loadUrl("javascript:typeof(Get_result) == 'function' && Get_result('" + name + "');");
                }
            });
        }

        @JavascriptInterface
        public void Set(final String name, final String value)
        {
            m_handler.post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getContext(), String.format("设置属性: name(%s), 值(%s)", name, value != null ? value.toString() : null), Toast.LENGTH_SHORT).show();
                    loadUrl("javascript:typeof(Set_result) == 'function' && Set_result('" + name + " -> " + value + "');");
                }
            });
        }

        @JavascriptInterface
        public void Call(final String func, final String args)
        {
            m_handler.post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getContext(), String.format("调用: 函数(%s), 参数(%s)", func, args), Toast.LENGTH_SHORT).show();
                    loadUrl("javascript:typeof(Call_result) == 'function' && Call_result('" + func + " -> " + args + "');", null);
                }
            });
        }

        @JavascriptInterface
        public void Toast(final String message)
        {
            m_handler.post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

 /*   @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        switch(keyCode)
        {
            case KeyEvent.KEYCODE_BACK:
            {
                if(m_webView.canGoBack())
                {
                    m_webView.goBack();
                    return true;
                }
            }
            break;
            default:
                break;
        }
        return super.onKeyDown(keyCode, event);
    }*/

}