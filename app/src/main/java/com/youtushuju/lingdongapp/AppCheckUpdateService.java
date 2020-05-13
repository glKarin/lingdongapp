package com.youtushuju.lingdongapp;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.widget.Toast;

import com.youtushuju.lingdongapp.common.Logf;
import com.youtushuju.lingdongapp.gui.App;
import com.youtushuju.lingdongapp.json.JsonMap;

import java.io.File;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class AppCheckUpdateService extends IntentService {
    private static final String ID_TAG = "AppCheckUpdateService";
    public static final String ID_CHECK_UPDATE_RECEIVER = "check_update_receiver";
    public static final String ID_DOWNLOAD_APP_RECEIVER = "download_app_receiver";

    private static final String ID_ACTION_CHECK_UPDATE = "com.youtushuju.lingdongapp.action.app_check_update";
    private static final String ID_ACTION_DOWNLOAD_APP = "com.youtushuju.lingdongapp.action.download_app";

    private static final String ID_ACTION_CHECK_UPDATE_PARAM_VERSION = "com.youtushuju.lingdongapp.extra.version";
    private static final String ID_ACTION_DOWNLOAD_APP_PARAM_FILENAME = "com.youtushuju.lingdongapp.extra.filename";
    private static final String ID_ACTION_DOWNLOAD_APP_PARAM_URL = "com.youtushuju.lingdongapp.extra.url";

    public AppCheckUpdateService() {
        super(ID_TAG);
    }

    public static void AppCheckUpdate(Context context, String version) {
        Intent intent = new Intent(context, AppCheckUpdateService.class);
        intent.setAction(ID_ACTION_CHECK_UPDATE);
        intent.putExtra(ID_ACTION_CHECK_UPDATE_PARAM_VERSION, version);
        context.startService(intent);
    }

    public static void DownloadUpdateApp(Context context, String filename, String url) {
        Intent intent = new Intent(context, AppCheckUpdateService.class);
        intent.setAction(ID_ACTION_DOWNLOAD_APP);
        intent.putExtra(ID_ACTION_DOWNLOAD_APP_PARAM_FILENAME, filename);
        intent.putExtra(ID_ACTION_DOWNLOAD_APP_PARAM_URL, url);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ID_ACTION_CHECK_UPDATE.equals(action)) {
                String version = intent.getStringExtra(ID_ACTION_CHECK_UPDATE_PARAM_VERSION);
                HandleAppCheckUpdate(version);
            }
            else if(ID_ACTION_DOWNLOAD_APP.equals(action))
            {
                String filename = intent.getStringExtra(ID_ACTION_DOWNLOAD_APP_PARAM_FILENAME);
                String url = intent.getStringExtra(ID_ACTION_DOWNLOAD_APP_PARAM_URL);
                HandleDownloadUpdateApp(filename, url);
            }
            else
            {
            }
        }
    }

    private void HandleAppCheckUpdate(String version) {
        Logf.e(ID_TAG, version);
        JsonMap result = App.Instance().CheckUpdate(version);
        Intent intent = new Intent(getPackageName() + "." + ID_CHECK_UPDATE_RECEIVER);
        Bundle bundle = new Bundle();

        try
        {
            if(result == null)
            {
                bundle.putBoolean("result", false);
                bundle.putString("message", "同步服务器错误");
            }
            else
            {
                int code = (int)result.get("result");
                boolean suc = code == 0;
                if(!suc)
                {
                    bundle.putBoolean("result", false);
                    bundle.putString("message", (String)result.get("description"));
                }
                else
                {
                    JsonMap data = (JsonMap)result.get("data");
                    if((boolean)data.get("result"))
                    {
                        bundle.putBoolean("result", true);
                        bundle.putString("message", (String)result.get("description"));
                        Bundle b = new Bundle();
                        JsonMap map = (JsonMap)result.get("data");
                        b.putString("file", (String)map.get("file"));
                        b.putString("url", (String)map.get("url"));
                        b.putString("version", (String)map.get("version"));
                        b.putString("changelogs", (String)map.get("changelogs"));
                        b.putString("release", (String)map.get("release"));
                        bundle.putBundle("data", b);
                    }
                    else
                    {
                        bundle.putBoolean("result", false);
                        bundle.putString("message", "当前已是最新版本");
                    }
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            bundle.putBoolean("result", false);
            bundle.putString("message", "检查更新异常");
        }

        intent.putExtras(bundle);
        sendBroadcast(intent);
    }

    private void HandleDownloadUpdateApp(String filename, String url) {
        Logf.e(ID_TAG, "%s -> %s", filename, url);
        File file = App.Instance().DownloadApp(filename, url);
        Intent intent = new Intent(getPackageName() + "." + ID_DOWNLOAD_APP_RECEIVER);
        Bundle bundle = new Bundle();

        if(file == null)
        {
            bundle.putBoolean("result", false);
            bundle.putString("message", "下载失败");
        }
        else
        {
            bundle.putBoolean("result", true);
            bundle.putString("message", "下载成功");;
            bundle.putString("data", file.getAbsolutePath());
        }

        intent.putExtras(bundle);
        sendBroadcast(intent);
    }

    @Override
    public IBinder onBind(Intent intent) {
        Logf.d(ID_TAG, "onBind");
        //return m_binder;
        return super.onBind(intent);
    }

    @Override
    public void onCreate() {
        Logf.d(ID_TAG, "onCreate");
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Logf.d(ID_TAG, "onStartCommand");
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        Logf.d(ID_TAG, "onDestroy");
        super.onDestroy();
    }

    private AppCheckUpdateBinder m_binder = new AppCheckUpdateBinder();
    public class AppCheckUpdateBinder extends Binder
    {
    }

}
