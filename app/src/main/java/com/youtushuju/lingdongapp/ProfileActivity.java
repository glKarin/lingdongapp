package com.youtushuju.lingdongapp;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.youtushuju.lingdongapp.gui.App;
import com.youtushuju.lingdongapp.gui.ArrayAdapter_base;

import java.util.ArrayList;
import java.util.List;

public class ProfileActivity extends AppCompatActivity {
    private static final String ID_TAG = "ProfileActivity";
    private ListView m_contentMenu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.profile_page);

        App app = App.Instance();
        //app.Init(this);

        m_contentMenu = (ListView)findViewById(R.id.profile_content);

        SetupUI();

        app.PushActivity(this);
    }

    private void SetupUI()
    {
        final List<ProfileMenuModel> menuList;
        ProfileMenuModel item;
        ProfileMenuListAdapter adapter;

        menuList = new ArrayList<ProfileMenuModel>();

        item = new ProfileMenuModel("历史记录", "record", R.drawable.icon_record, new Runnable() {
            @Override
            public void run() {
                Intent intent;

                intent = new Intent(ProfileActivity.this, RecordActivity.class);
                ProfileActivity.this.startActivity(intent);
            }
        }, true);
        menuList.add(item);

        item = new ProfileMenuModel("系统信息", "system_info", R.drawable.icon_systeminfo, new Runnable() {
            @Override
            public void run() {
                Intent intent;

                intent = new Intent(ProfileActivity.this, SystemInfoActivity.class);
                ProfileActivity.this.startActivity(intent);
            }
        }, true);
        menuList.add(item);

        item = new ProfileMenuModel("运行状态", "runtime_status", R.drawable.icon_terminal, new Runnable() {
            @Override
            public void run() {
                Intent intent;

                intent = new Intent(ProfileActivity.this, RuntimeStatusActivity.class);
                ProfileActivity.this.startActivity(intent);
            }
        }, true);
        menuList.add(item);

        item = new ProfileMenuModel("文件系统", "file_system", R.drawable.icon_fs, new Runnable() {
            @Override
            public void run() {
                Intent intent;

                intent = new Intent(ProfileActivity.this, FileBrowserActivity.class);
                ProfileActivity.this.startActivity(intent);
            }
        }, true);
        menuList.add(item);

        item = new ProfileMenuModel("系统控制", "system_control", R.drawable.icon_control, new Runnable() {
            @Override
            public void run() {
                Intent intent;

                intent = new Intent(ProfileActivity.this, ControlActivity.class);
                ProfileActivity.this.startActivity(intent);
            }
        }, true);
        menuList.add(item);

        item = new ProfileMenuModel("串口操作", "serial", R.drawable.icon_serialport, new Runnable() {
            @Override
            public void run() {
                Intent intent;

                intent = new Intent(ProfileActivity.this, SerialActivity.class);
                ProfileActivity.this.startActivity(intent);
            }
        }, true);
        menuList.add(item);

        item = new ProfileMenuModel("运行日志", "log", R.drawable.icon_log, new Runnable() {
            @Override
            public void run() {
                Intent intent;

                intent = new Intent(ProfileActivity.this, LogActivity.class);
                ProfileActivity.this.startActivity(intent);
            }
        }, true);
        menuList.add(item);

        item = new ProfileMenuModel("设置", "setting", R.drawable.icon_settings, new Runnable() {
            @Override
            public void run() {
                OpenSettingsPage();
            }
        }, true);
        menuList.add(item);

        item = new ProfileMenuModel("关于", "about", R.drawable.icon_about, new Runnable() {
            @Override
            public void run() {
                Intent intent;

                intent = new Intent(ProfileActivity.this, AboutActivity.class);
                ProfileActivity.this.startActivity(intent);
            }
        }, true);
        menuList.add(item);

        item = new ProfileMenuModel("退出", "exit", R.drawable.icon_quit, new Runnable() {
            @Override
            public void run() {
                Exit();
            }
        }, true);
        menuList.add(item);

        adapter = new ProfileMenuListAdapter(this, R.layout.profile_menu_deledate);
        adapter.SetData(menuList);
        m_contentMenu.setAdapter(adapter);
        m_contentMenu.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ProfileMenuModel item;

                item = menuList.get(position);
                if(item.enabled && item.action != null)
                {
                    item.action.run();
                }
            }
        });
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.profile_page_menu, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId())
        {
            case R.id.profile_menu_sdk_demo:
                Intent intent = new Intent(ProfileActivity.this, DemoActivity.class);
                ProfileActivity.this.startActivity(intent);
                break;
            default:
                return super.onOptionsItemSelected(item);
        }

        return true;
    }

    private void OpenSettingsPage()
    {
        Intent intent;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
        {
            intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
        }
        else
            Toast.makeText(this, "安卓系统版本过低(至少需要Android N以上)", Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        App.Instance().PopActivity();
    }

    private void Exit()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("退出程序");
        builder.setMessage("退出灵动人脸程序?");
        builder.setIcon(R.drawable.icon_profile);
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                App.Instance().Exit(0);
            }
        });
        builder.setNegativeButton("取消", null);
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    // internal
    private class ProfileMenuModel
    {
        public String label;
        public String name;
        public int icon;
        public Runnable action;
        public boolean enabled = true;

        public ProfileMenuModel(String label, String name, int icon, Runnable action, boolean enabled) {
            this.label = label;
            this.name = name;
            this.icon = icon;
            this.action = action;
            this.enabled = enabled;
        }
    }

    private class ProfileMenuListAdapter extends ArrayAdapter_base<ProfileMenuModel>
    {
        public ProfileMenuListAdapter(Context context, int resource)
        {
            super(context, resource);
        }

        public View GenerateView(int position, View view, ViewGroup parent, ProfileMenuModel data)
        {
            TextView textView = (TextView)view.findViewById(R.id.profile_menu_delegate_label);
            textView.setText(data.label);
            ImageView iconView = (ImageView)view.findViewById(R.id.profile_menu_delegate_icon);
            iconView.setImageResource(data.icon);
            return view;
        }
    }
}