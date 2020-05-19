package com.youtushuju.lingdongapp;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Icon;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;

import com.youtushuju.lingdongapp.common.Common;
import com.youtushuju.lingdongapp.common.Configs;
import com.youtushuju.lingdongapp.common.FS;
import com.youtushuju.lingdongapp.common.FileBrowser;
import com.youtushuju.lingdongapp.gui.ActivityUtility;
import com.youtushuju.lingdongapp.gui.App;
import com.youtushuju.lingdongapp.gui.ArrayAdapter_base;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class LogActivity extends AppCompatActivity {
    private static final String ID_TAG = "LogActivity";
    private static final String ID_NOTIFICATION_CHANNEL_ID = "UPLOAD_LOG";
    private ListView m_listView;
    private LogListAdapter m_adapter;
    private FileBrowser m_fileBrowser = null;
    private UploadReceiver m_uploadReceiver = null;
    private IntentFilter m_intentFilter = null;
    private final String m_logItemMenuItems[] = {"查看", "上传"};
    private NotificationChannel m_notificationChannel = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.log_page);

        m_listView = (ListView)findViewById(R.id.log_page_list);
        m_fileBrowser = new FileBrowser(null);
        m_fileBrowser.SetIgnoreDotDot(true).SetOrder(FileBrowser.ID_ORDER_BY_TIME).SetSequence(FileBrowser.ID_SEQUENCE_DESC);
        m_fileBrowser.SetCurrentPath(Configs.Instance().GetFilePath(Configs.ID_CONFIG_LOG_DIRECTORY));

        m_adapter = new LogListAdapter(this);
        SetupUI();

        App.Instance().PushActivity(this);
    }

    private void SetupUI()
    {
        m_listView.setAdapter(m_adapter);
        m_listView.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            FileBrowser.FileModel item;

            item = m_fileBrowser.GetFileModel(position);
            OpenLogMenu(item.path);
            }
        });
    }

    private void LoadLogList()
    {
        m_adapter.clear();
        m_fileBrowser.Rescan();
        m_adapter.SetData(m_fileBrowser.FileList());
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.log_page_menu, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        File file;

        switch (item.getItemId())
        {
            case R.id.log_menu_view_runtime_log:
                file = (File)(Configs.Instance().GetConfig(Configs.ID_CONFIG_LOG_FILE));
                ViewLog(file);
                break;
            case R.id.log_menu_view_crash_log:
                file = Configs.Instance().GetFile(Configs.ID_CONFIG_CORE_DUMP_FILE);
                if(file != null && file.isFile())
                    ActivityUtility.OpenExternally(this, file.getAbsolutePath());
                else
                    Toast.makeText(this, "最后崩溃日志文件未被创建", Toast.LENGTH_LONG).show();
                break;
            case R.id.log_menu_upload_runtime_log:
                file = (File)Configs.Instance().GetConfig(Configs.ID_CONFIG_LOG_FILE);
                UploadLog(file);
                break;
            case R.id.log_menu_upload_crash_log:
                InitReceiver();
                UploadLogService.UploadCrashLog(LogActivity.this);
                Toast.makeText(this, "正在上传最后的崩溃日志文件", Toast.LENGTH_LONG).show();
                break;
            case R.id.log_menu_clean_log:
                CleanLog();
                break;
            default:
                return super.onOptionsItemSelected(item);
        }

        return true;
    }

    private void CleanLog()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("警告");
        builder.setMessage("确定要清空所有日志文件?");
        builder.setIcon(R.drawable.icon_warning);
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                boolean res = App.Instance().CleanLog();
                if(res)
                {
                    LoadLogList();
                    Toast.makeText(LogActivity.this, "日志文件已清空", Toast.LENGTH_LONG).show();
                }
                else
                    Toast.makeText(LogActivity.this, "清空日志文件失败", Toast.LENGTH_LONG).show();
            }
        });
        builder.setNegativeButton("取消", null);
        AlertDialog dialog = builder.create();
        
        dialog.show();
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View view,
                                    ContextMenu.ContextMenuInfo menuInfo) {
        MenuInflater inflater = new MenuInflater(this);
        inflater.inflate(R.menu.log_item_menu, menu);
        super.onCreateContextMenu(menu, view, menuInfo);
    }

    // UNUSED
    @Override
    public boolean onContextItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.log_item_menu_upload:
                break;
            case R.id.log_item_menu_view:
                break;
            default:
                break;
        }
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(m_uploadReceiver != null)
            registerReceiver(m_uploadReceiver, m_intentFilter);
        App.Instance().PopActivity();
    }

    private void ViewLog(File file)
    {
        if(file != null && file.isFile())
            ActivityUtility.OpenExternally(this, file.getAbsolutePath());
        else
            Toast.makeText(this, "当前日志文件未被创建", Toast.LENGTH_LONG).show();
    }

    private void UploadLog(File file)
    {
        InitReceiver();
        if(file != null && file.isFile())
        {
            UploadLogService.UploadRuntimeLog(LogActivity.this, file.getAbsolutePath());
        }
        else
            Toast.makeText(this, "正在上传本次运行日志文件", Toast.LENGTH_LONG).show();
    }

    private void OpenLogMenu(final String path)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("选择操作");
        builder.setItems(m_logItemMenuItems, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                switch (which)
                {
                    case 1:
                        UploadLog(new File(path));
                        break;
                    case 0:
                        ViewLog(new File(path));
                        break;
                    default: // nothing, must 0 or 1
                        break;
                }
            }
        });
        //builder.setIcon(R.drawable.ic_launcher);
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    @Override
    protected void onStart() {
        super.onStart();

        if(!ActivityUtility.IsGrantPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE))
        {
            if(!ActivityUtility.RequestPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE))
                OpenPermissionGrantFailDialog();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(ActivityUtility.IsGrantPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE))
            LoadLogList();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == ActivityUtility.ID_REQUEST_PERMISSION_RESULT)
        {
            int index = 0; // only storage
            if(grantResults[index] != PackageManager.PERMISSION_GRANTED)
            {
                OpenPermissionGrantFailDialog();
            }
        }
    }

    private void OpenPermissionGrantFailDialog()
    {
        final DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE:
                        ActivityUtility.OpenAppSetting(LogActivity.this);
                        break;
                    case DialogInterface.BUTTON_NEGATIVE:
                    default:
                        finish();
                        break;
                }
            }
        };
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("程序无权限访问外部存储!");
        builder.setMessage("请前往系统设置手动授权程序读取外部存储");
        builder.setIcon(R.drawable.icon_warning);
        builder.setCancelable(false);
        builder.setPositiveButton("确定", listener);
        builder.setNegativeButton("返回", listener);
        AlertDialog dialog = builder.create();
        dialog.show();
    }


    private class UploadReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            final Bundle bundle = intent.getExtras();
            String action = intent.getAction();
            if((getPackageName() + "." + UploadLogService.ID_UPLOAD_CRASH_LOG_RECEIVER).equals(action))
            {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                    String message = bundle.getString("message");
                    String url = bundle.getBoolean("result") ? bundle.getBundle("data").getString("url") : null;
                    ShowNotification("上传崩溃日志" + message, url);
                    }
                });
            }
            else if((getPackageName() + "." + UploadLogService.ID_UPLOAD_RUNTIME_LOG_RECEIVER).equals(action))
            {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        String message = bundle.getString("message");
                        String url = bundle.getBoolean("result") ? bundle.getBundle("data").getString("url") : null;
                        ShowNotification("上传运行日志" + message, url);
                    }
                });
            }
        }
    }

    private void InitReceiver()
    {
        if(m_uploadReceiver != null)
            return;
        m_uploadReceiver = new UploadReceiver();
        m_intentFilter = new IntentFilter();
        m_intentFilter.addAction(getPackageName() + "." + UploadLogService.ID_UPLOAD_CRASH_LOG_RECEIVER);
        m_intentFilter.addAction(getPackageName() + "." + UploadLogService.ID_UPLOAD_RUNTIME_LOG_RECEIVER);
        registerReceiver(m_uploadReceiver, m_intentFilter);
    }

    private void ShowNotification(String message, String url)
    {
        NotificationManager manager = (NotificationManager)(getSystemService(Context.NOTIFICATION_SERVICE));

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            if(m_notificationChannel == null)
            {
                m_notificationChannel = new NotificationChannel(ID_NOTIFICATION_CHANNEL_ID, "日志上传", NotificationManager.IMPORTANCE_DEFAULT);
                manager.createNotificationChannel(m_notificationChannel);
            }
        }

        Intent intent = null;
        if(url != null)
        {
            Uri uri = Uri.parse(url);
            intent = new Intent(Intent.ACTION_VIEW, uri);
        }
        else
        {
            intent = new Intent(this, LogActivity.class);
        }
        PendingIntent pi = PendingIntent.getActivity(this, 0, intent, 0);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, ID_NOTIFICATION_CHANNEL_ID);
        builder.setTicker("");
        builder.setContentTitle(message);
        builder.setContentText(url != null ? "文件地址: " + url : "");
        builder.setAutoCancel(true);
        builder.setContentInfo("");
        builder.setSubText("日志上传");
        builder.setWhen(System.currentTimeMillis());
        builder.setDefaults(Notification.DEFAULT_VIBRATE | Notification.DEFAULT_LIGHTS);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
        {
            Resources resources = getResources();
            builder.setLargeIcon(BitmapFactory.decodeResource(resources, R.mipmap.ic_launcher));
            builder.setSmallIcon(R.mipmap.ic_launcher);
        }
        builder.setContentIntent(pi);
        manager.notify(1, builder.build());
    }

    // internal
    private class LogListAdapter extends ArrayAdapter_base<FileBrowser.FileModel>
    {
        public LogListAdapter(Context context)
        {
            super(context, R.layout.log_deledate);
        }

        public View GenerateView(int position, View view, ViewGroup parent, FileBrowser.FileModel data)
        {
            TextView textView;

            textView = (TextView)view.findViewById(R.id.log_browser_delegate_name);
            textView.setText(data.name);
            textView = (TextView)view.findViewById(R.id.log_browser_delegate_size);
            textView.setText(FS.FormatSize(data.size));
            textView = (TextView)view.findViewById(R.id.log_browser_delegate_time);
            textView.setText(Common.TimestampToStr(data.time));
            ((ImageView)view.findViewById(R.id.log_browser_delegate_icon)).setImageResource(R.drawable.icon_file);

            return view;
        }
    }
}