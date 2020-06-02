package com.youtushuju.lingdongapp.gui;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Handler;
import android.util.AttributeSet;
import android.webkit.JavascriptInterface;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.youtushuju.lingdongapp.R;
import com.youtushuju.lingdongapp.common.Common;
import com.youtushuju.lingdongapp.common.Configs;
import com.youtushuju.lingdongapp.common.Logf;

public class ScreenSaverView extends WebView
{
    private static final String ID_TAG = "ScreenSaverView";
    private static final String ID_WINDOW_OBJECT_NAME = "_Native_object";
    private static final String ID_SCREENSAVER_FILE_PATH = "file:///android_asset/screensaver/screensaver.html";
    private static final String ID_SCREENSAVER_URL = "http://ryce2-test.youtushuju.cn/screensaver.html";

    private String m_url = null;
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
        m_url = ActivityUtility.BuildOnDebug(getContext()) ? ID_SCREENSAVER_URL : ID_SCREENSAVER_FILE_PATH;
        //m_url = ID_SCREENSAVER_URL; // for test

        settings.setJavaScriptEnabled(true);
        settings.setUseWideViewPort(true);
        settings.setLoadWithOverviewMode(true);
        settings.setBuiltInZoomControls(false);
        settings.setDisplayZoomControls(false);
        settings.setSupportZoom(false);

        m_object = new WindowObject(getContext(), m_handler);
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
        if(Common.StringIsEmpty(getUrl()))
            loadUrl(m_url);
    }

    public void SetNativeObject(WindowObject obj)
    {
        m_object = obj;
        m_object.SetWebView(this);
        addJavascriptInterface(m_object, ID_WINDOW_OBJECT_NAME); // only one
    }

    public static class WindowObject
    {
        private static final String ID_TAG = "WindowObject";
        protected Handler m_handler = null;
        protected Context m_context = null;
        protected WebView m_webView = null;

        public WindowObject(Context context, Handler handler)
        {
            m_context = context;
            m_handler = handler;
        }

        private void SetWebView(WebView webView)
        {
            m_webView = webView;
        }

        @JavascriptInterface
        public void Get(final String name, final Object defaultValue)
        {
        }

        @JavascriptInterface
        public void Set(final String name, final String value)
        {
        }

        @JavascriptInterface
        public void Call(final String func, final String args)
        {
        }

        @JavascriptInterface
        public void Toast(final String message)
        {
            m_handler.post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(m_context, message, Toast.LENGTH_SHORT).show();
                }
            });
        }

        protected void CallJSFunc(final String func, Object...args)
        {
            StringBuffer sb = new StringBuffer();
            for(int i = 0; i < args.length; i++)
            {
                sb.append("'" + args[i].toString() + "'");
                if(i < args.length - 1)
                    sb.append(", ");
            }
            final String arg = sb.toString();
            m_handler.post(new Runnable() {
                @Override
                public void run() {
                    Logf.e(ID_TAG, String.format("调用js函数: 函数(%s), 参数(%s)", func, arg));
                    String script = "javascript:typeof(" + func + ") == 'function' && " + func + "(" + arg + ");";
                    Logf.e(ID_TAG, script);
                    m_webView.loadUrl(script, null);
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