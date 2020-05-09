package com.youtushuju.lingdongapp.gui;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.app.ActivityCompat;

import com.youtushuju.lingdongapp.common.Configs;
import com.youtushuju.lingdongapp.common.FS;
import com.youtushuju.lingdongapp.common.Logf;

import java.io.File;
import java.util.Collections;
import java.util.Iterator;
import java.util.Stack;
import java.util.function.Predicate;

public final class App {
    private static final String ID_TAG = "App";
    private static App _app = null;
    private Stack<Activity> m_activityStack;

    private App()
    {
        m_activityStack = new Stack<Activity>();
    }

    public static App Instance()
    {
        if(_app == null)
        {
            _app = new App();
        }
        return _app;
    }

    public App PushActivity(Activity a)
    {
        m_activityStack.push(a);
        Logf.d(ID_TAG, m_activityStack.toString());
        return this;
    }

    public App PopActivity()
    {
        if(!m_activityStack.empty())
        {
            m_activityStack.pop();
        }
        Logf.d(ID_TAG, m_activityStack.toString());
        return this;
    }

    public App PopActivity(final Activity a)
    {
        if(!m_activityStack.empty())
        {
            // SDK >= 24
            /*m_activityStack.removeIf(new Predicate<Activity>() {
                @Override
                public boolean test(Activity activity) {
                    return activity == a;
                }
            });*/
             m_activityStack.remove(a);
        }
        Logf.d(ID_TAG, m_activityStack.toString());
        return this;
    }

    public void Clear()
    {
        Iterator<Activity> itor;

        itor = m_activityStack.iterator();
        while(itor.hasNext())
        {
            itor.next().finish();
        }
        m_activityStack.clear();
    }

    public void Exit(int code)
    {
        Clear();
        Logf.e("asdasd", m_activityStack.size());
        System.exit(code);
    }

    public boolean CleanLog()
    {
        File file = Configs.Instance().GetFile(Configs.ID_CONFIG_LOG_DIRECTORY);
        if(file == null || !file.isDirectory())
            return false;
        FS.rm(file);
        return true;
    }

    public long GetLogSize()
    {
        File file = Configs.Instance().GetFile(Configs.ID_CONFIG_LOG_DIRECTORY);
        if(file == null || !file.isDirectory())
            return 0L;
        return FS.du(file);
    }
}
