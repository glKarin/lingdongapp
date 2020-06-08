package com.youtushuju.lingdongapp;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;

import com.youtushuju.lingdongapp.common.Logf;
import com.youtushuju.lingdongapp.common.Configs;
import com.youtushuju.lingdongapp.gui.App;
import com.youtushuju.lingdongapp.json.JsonMap;
import com.youtushuju.lingdongapp.network.HttpUtility;
import com.youtushuju.lingdongapp.json.*;

import java.io.File;

public class UploadLogService extends IntentService {
    private static final String ID_TAG = "UploadLogService";
    public static final String ID_UPLOAD_CRASH_LOG_RECEIVER = "upload_crash_log_receiver";
    public static final String ID_UPLOAD_RUNTIME_LOG_RECEIVER = "upload_runtime_log_receiver";

    private static final String ID_ACTION_UPLOAD_CRASH_LOG = "com.youtushuju.lingdongapp.action.upload_crash_log";
    private static final String ID_ACTION_UPLOAD_RUNTIME_LOG = "com.youtushuju.lingdongapp.action.upload_runtime_log";

    private static final String ID_ACTION_UPLOAD_FILE = "com.youtushuju.lingdongapp.extra.file";
    private static final String ID_ACTION_UPLOAD_URL = "com.youtushuju.lingdongapp.extra.url";

    public UploadLogService() {
        super(ID_TAG);
    }

    public static void UploadCrashLog(Context context) {
        String path = Configs.Instance().GetFilePath(Configs.ID_CONFIG_CORE_DUMP_FILE);
        Intent intent = new Intent(context, UploadLogService.class);
        intent.setAction(ID_ACTION_UPLOAD_CRASH_LOG);
        intent.putExtra(ID_ACTION_UPLOAD_FILE, path);
        intent.putExtra(ID_ACTION_UPLOAD_URL, App.ID_APP_UPLOAD_CRASH_LOG_URL);
        context.startService(intent);
    }

    public static void UploadRuntimeLog(Context context, String file) {
        Intent intent = new Intent(context, UploadLogService.class);
        intent.setAction(ID_ACTION_UPLOAD_RUNTIME_LOG);
        intent.putExtra(ID_ACTION_UPLOAD_FILE, file);
        intent.putExtra(ID_ACTION_UPLOAD_URL, App.ID_APP_UPLOAD_RUNTIME_LOG_URL);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ID_ACTION_UPLOAD_CRASH_LOG.equals(action)) {
                String path = intent.getStringExtra(ID_ACTION_UPLOAD_FILE);
                String url = intent.getStringExtra(ID_ACTION_UPLOAD_URL);
                HandleUploadCrashLogFile(path, url);
            }
            else if (ID_ACTION_UPLOAD_RUNTIME_LOG.equals(action)) {
                String path = intent.getStringExtra(ID_ACTION_UPLOAD_FILE);
                String url = intent.getStringExtra(ID_ACTION_UPLOAD_URL);
                HandleUploadRuntimeLogFile(path, url);
            }
            else
            {
            }
        }
    }

    private void HandleUploadCrashLogFile(String path, String url) {
        Logf.e(ID_TAG, "%s -> %s", path, url);
        File file = new File(path);
        byte respData[] = HttpUtility.UploadFile(file, url, "file");
        Intent intent = new Intent(getPackageName() + "." + ID_UPLOAD_CRASH_LOG_RECEIVER);
        Bundle bundle = new Bundle();

        try
        {
            if(respData == null)
            {
                bundle.putBoolean("result", false);
                bundle.putString("message", "上传失败");
            }
            else
            {
                JsonResult json = JSON.Parse(new String(respData));
                JsonMap result = (JsonMap)json;
                int code = (int)result.get("result");
                boolean suc = code == 0;
                if(!suc)
                {
                    bundle.putBoolean("result", false);
                    bundle.putString("message", (String)result.get("description"));
                }
                else
                {
                    bundle.putBoolean("result", true);
                    bundle.putString("message", (String)result.get("description"));
                    Bundle b = new Bundle();
                    JsonMap map = (JsonMap)result.get("data");
                    b.putString("url", (String)map.get("url"));
                    bundle.putBundle("data", b);
                }
            }
        }
        catch (Exception e)
        {
            App.HandleException(e);
            bundle.putBoolean("result", false);
            bundle.putString("message", "上传崩溃日志失败");
        }

        intent.putExtras(bundle);
        sendBroadcast(intent);
    }

    private void HandleUploadRuntimeLogFile(String path, String url) {
        Logf.e(ID_TAG, "%s -> %s", path, url);
        File file = new File(path);
        byte respData[] = HttpUtility.UploadFile(file, url, "file");
        Intent intent = new Intent(getPackageName() + "." + ID_UPLOAD_RUNTIME_LOG_RECEIVER);
        Bundle bundle = new Bundle();

        try
        {
            if(respData == null)
            {
                bundle.putBoolean("result", false);
                bundle.putString("message", "上传失败");
            }
            else
            {
                JsonResult json = JSON.Parse(new String(respData));
                JsonMap result = (JsonMap)json;
                int code = (int)result.get("result");
                boolean suc = code == 0;
                if(!suc)
                {
                    bundle.putBoolean("result", false);
                    bundle.putString("message", (String)result.get("description"));
                }
                else
                {
                    bundle.putBoolean("result", true);
                    bundle.putString("message", (String)result.get("description"));
                    Bundle b = new Bundle();
                    JsonMap map = (JsonMap)result.get("data");
                    b.putString("url", (String)map.get("url"));
                    bundle.putBundle("data", b);
                }
            }
        }
        catch (Exception e)
        {
            App.HandleException(e);
            bundle.putBoolean("result", false);
            bundle.putString("message", "上传运行日志失败");
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

}
