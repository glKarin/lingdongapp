package com.youtushuju.lingdongapp;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.youtushuju.lingdongapp.common.Common;
import com.youtushuju.lingdongapp.common.Logf;
import com.youtushuju.lingdongapp.gui.ActivityUtility;
import com.youtushuju.lingdongapp.gui.App;
import com.youtushuju.lingdongapp.gui.ArrayAdapter_base;
import com.youtushuju.lingdongapp.json.JsonMap;

import java.util.ArrayList;
import java.util.List;

public class AboutActivity extends AppCompatActivity {
    private static final String ID_TAG = "AboutActivity";
    private ListView m_contentMenu;
    private AlertDialog m_loadingDialog = null;
    private CheckUpdateReceiver m_checkUpdateReceiver = null;
    private IntentFilter m_intentFilter = null;
    /*private IBinder m_binder = null;
    private ServiceConnection m_serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceDisconnected(ComponentName name) {
            Logf.e(ID_TAG, "onServiceDisconnected: " + name.toString());
            m_binder = null;
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Logf.e(ID_TAG, "onServiceConnected: " + name.toString());
            m_binder = service;
        }
    };*/

    private class CheckUpdateReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            final Bundle bundle = intent.getExtras();
            String action = intent.getAction();
            if((getPackageName() + ".check_update_receiver").equals(action))
            {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(m_loadingDialog != null)
                        {
                            m_loadingDialog.dismiss();
                            m_loadingDialog = null;
                        }
                        boolean needUpdate = bundle.getBoolean("result");
                        if(needUpdate)
                        {
                            Bundle b = bundle.getBundle("data");
                            OpenUpdateDialog(b);
                        }
                        else
                        {
                            String message = bundle.getString("message");
                            Toast.makeText(AboutActivity.this, message, Toast.LENGTH_LONG).show();
                        }
                        //unregisterReceiver(m_checkUpdateReceiver);
                    }
                });
            }
            else if((getPackageName() + ".download_app_receiver").equals(action))
            {
                boolean suc = bundle.getBoolean("result");
                if(suc)
                {
                    ActivityUtility.OpenExternally(AboutActivity.this, bundle.getString("data"));
                }
                else
                {
                    String message = bundle.getString("message");
                    Toast.makeText(AboutActivity.this, message, Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.about_page);

        m_contentMenu = (ListView)findViewById(R.id.about_page_content);
        SetupUI();

        App.Instance().PushActivity(this);
    }

    private void SetupUI()
    {
        final List<AboutMenuModel> menuList;
        AboutMenuModel item;
        AboutMenuListAdapter adapter;
        String version = null;

        menuList = new ArrayList<>();

        try
        {
            PackageManager manager = getPackageManager();
            PackageInfo info = manager.getPackageInfo(getPackageName(), 0);
            version = info.versionName;
            ((TextView)findViewById(R.id.about_page_app_name)).setText(manager.getApplicationLabel(info.applicationInfo));
            ((ImageView)findViewById(R.id.about_page_app_icon)).setImageDrawable(manager.getApplicationIcon(info.applicationInfo));
            ((TextView)findViewById(R.id.about_page_app_ver)).setText(version);
            ((TextView)findViewById(R.id.about_page_app_copyright)).setText(App.ID_COPYRIGHT);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        item = new AboutMenuModel("版本更新", "update", R.drawable.icon_profile, new Runnable() {
            @Override
            public void run() {
                OpenVersionDialog();
            }
        }, true, Common.TimestampToDateStr(BuildConfig.BUILD_TIMESTAMP));
        menuList.add(item);

        item = new AboutMenuModel("关于", "about", R.drawable.icon_profile, new Runnable() {
            @Override
            public void run() {
                OpenAboutDialog();
            }
        }, true, null);
        menuList.add(item);

        item = new AboutMenuModel("检查更新", "update", R.drawable.icon_profile, new Runnable() {
            @Override
            public void run() {
                AppCheckUpdate();
            }
        }, true, version);
        menuList.add(item);

        adapter = new AboutMenuListAdapter(this, R.layout.about_menu_deledate);
        adapter.SetData(menuList);
        m_contentMenu.setAdapter(adapter);
        m_contentMenu.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                AboutMenuModel item;

                item = menuList.get(position);
                if(item.enabled && item.action != null)
                {
                    item.action.run();
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if(m_checkUpdateReceiver != null)
            registerReceiver(m_checkUpdateReceiver, m_intentFilter);

        App.Instance().PopActivity();
    }

    private void AppCheckUpdate()
    {

        if(m_checkUpdateReceiver == null)
        {
            m_checkUpdateReceiver = new CheckUpdateReceiver();
            m_intentFilter = new IntentFilter();
            m_intentFilter.addAction(getPackageName() + "." + AppCheckUpdateService.ID_CHECK_UPDATE_RECEIVER);
            m_intentFilter.addAction(getPackageName() + "." + AppCheckUpdateService.ID_DOWNLOAD_APP_RECEIVER);
            registerReceiver(m_checkUpdateReceiver, m_intentFilter);
        }

        try {
            PackageManager manager = getPackageManager();
            PackageInfo info = manager.getPackageInfo(getPackageName(), 0);

            m_loadingDialog = CreateUpdateProgressDialog();
            m_loadingDialog.show();

            AppCheckUpdateService.AppCheckUpdate(this, info.versionName);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Toast.makeText(this, "检查更新异常", Toast.LENGTH_LONG).show();
            m_loadingDialog.dismiss();
            m_loadingDialog = null;
            //unregisterReceiver(m_checkUpdateReceiver);
        }
    }

    private void OpenVersionDialog()
    {
        StringBuffer sb = new StringBuffer();
        String ver = null;
        try
        {
            PackageInfo info = getPackageManager().getPackageInfo(getPackageName(), 0);
            ver = info.versionName;
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        sb.append("版本: " + ver + "\n");
        sb.append("编译时间: " + Common.TimestampToStr(BuildConfig.BUILD_TIMESTAMP) + "\n");
        sb.append("更新: \n");
        String updates[] = getResources().getStringArray(R.array.update_info);
        if(updates != null && updates.length > 0)
        {
            for (int i = 0; i < updates.length; i++)
                sb.append((i + 1) + ". " + updates[i] + "\n");
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("版本详情");
        builder.setMessage(sb.toString());
        builder.setIcon(R.drawable.icon_profile);
        builder.setPositiveButton("确定", null);
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void OpenAboutDialog()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("关于程序");
        builder.setMessage("优途数据灵动人脸程序");
        builder.setIcon(R.drawable.icon_profile);
        builder.setPositiveButton("确定", null);
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private AlertDialog CreateUpdateProgressDialog()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("检查更新中...");
        ProgressBar bar = new ProgressBar(builder.getContext());
        builder.setView(bar);
        //builder.setIcon(R.drawable.icon_profile);
        AlertDialog dialog = builder.create();
        dialog.setCancelable(false);
        return dialog;
    }

    private void OpenUpdateDialog(final Bundle b)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("App更新");
        StringBuffer sb = new StringBuffer();
        sb.append("版本: " + b.getString("version")).append('\n');
        sb.append("发布时间: " + b.getString("release")).append('\n');
        sb.append("更新内容: ").append('\n');
        String changelogs = b.getString("changelogs");
        if(!Common.StringIsBlank(changelogs))
        {
            String arr[] = changelogs.split("\n");
            for (String str : arr)
                sb.append(str + '\n');
        }
        builder.setMessage(sb.toString());
        builder.setIcon(R.drawable.icon_profile);
        builder.setNeutralButton("下载", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                Uri uri = Uri.parse(b.getString("url"));
                Intent intent  = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
            }
        });
        builder.setPositiveButton("更新", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                AppCheckUpdateService.DownloadUpdateApp(AboutActivity.this, b.getString("file"), b.getString("url"));
                Toast.makeText(AboutActivity.this, "后台更新中...", Toast.LENGTH_LONG).show();
            }
        });
        builder.setNegativeButton("取消", null);
        AlertDialog dialog = builder.create();
        dialog.show();
    }



    // internal
    private class AboutMenuModel
    {
        public String label;
        public String subtitle;
        public String name;
        public int icon;
        public Runnable action;
        public boolean enabled = true;

        public AboutMenuModel(String label, String name, int icon, Runnable action, boolean enabled, String subtitle) {
            this.label = label;
            this.name = name;
            this.icon = icon;
            this.action = action;
            this.enabled = enabled;
            this.subtitle = subtitle;
        }
    }

    private class AboutMenuListAdapter extends ArrayAdapter_base<AboutMenuModel>
    {
        public AboutMenuListAdapter(Context context, int resource)
        {
            super(context, resource);
        }

        public View GenerateView(int position, View view, ViewGroup parent, AboutMenuModel data)
        {
            TextView textView = (TextView)view.findViewById(R.id.about_menu_delegate_label);
            textView.setText(data.label);
            if(!Common.StringIsEmpty(data.subtitle))
            {
                textView = (TextView)view.findViewById(R.id.about_menu_delegate_subtitle);
                textView.setText(data.subtitle);
            }
            return view;
        }
    }
}
