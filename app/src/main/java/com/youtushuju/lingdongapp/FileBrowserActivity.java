package com.youtushuju.lingdongapp;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;

import com.youtushuju.lingdongapp.common.Logf;
import com.youtushuju.lingdongapp.device.LingDongApi;
import com.youtushuju.lingdongapp.common.Configs;
import com.youtushuju.lingdongapp.common.FS;
import com.youtushuju.lingdongapp.common.FileBrowser;
import com.youtushuju.lingdongapp.gui.ActivityUtility;
import com.youtushuju.lingdongapp.gui.App;
import com.youtushuju.lingdongapp.gui.ArrayAdapter_base;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.provider.Settings;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class FileBrowserActivity extends AppCompatActivity {
    private static final String ID_TAG = "FileBrowserActivity";
    public static final String ID_CURRENT_PAGE_PATH = "CURRENT_PAGE_PATH";

    private ListView m_menuView;
    private ViewPager m_viewPager;
    private DrawerLayout m_drawerLayout;
    private boolean m_inited = false;
    private LingDongApi m_lingDongApi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.file_browser_page);

        m_drawerLayout = (DrawerLayout)findViewById(R.id.file_browser_drawer);
        m_menuView = (ListView)findViewById(R.id.file_browser_menu);
        m_viewPager = (ViewPager)findViewById(R.id.file_browser_content);

        SetupUI();

        App.Instance().PushActivity(this);
    }

    private void SetupUI()
    {
        FileMenuListAdapter adapter;

        m_lingDongApi = Configs.Instance().GetLingDongApi(this);

        adapter = new FileMenuListAdapter(this);
        m_menuView.setAdapter(adapter);
        m_menuView.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                m_viewPager.setCurrentItem(position);
                m_drawerLayout.closeDrawer(Gravity.LEFT, true);
            }
        });
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.file_browser_page_menu, menu);
        return true;
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
        if(ActivityUtility.IsGrantPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) && !m_inited)
        {
            FileMenuModel item;
            final List<FileMenuModel> menuList;

            menuList = new ArrayList<FileMenuModel>();
            m_inited = true;
            item = new FileMenuModel("内部存储", m_lingDongApi.GetSystemPrimaryStoragePath(), 0, true);
            menuList.add(item);

            item = new FileMenuModel("外部sd卡", m_lingDongApi.GetSystemSdcardPath(), 0, true);
            menuList.add(item);

            item = new FileMenuModel("U盘", m_lingDongApi.GetSystemUSBPath(), 0,true);
            menuList.add(item);

            ((FileMenuListAdapter)m_menuView.getAdapter()).SetData(menuList);
            m_viewPager.setAdapter(new FileViewPagerAdapter(menuList));

            Bundle bundle = getIntent().getExtras();
            if(bundle != null)
            {
                int current = bundle.getInt(ID_CURRENT_PAGE_PATH);
                Logf.e(ID_TAG, "请求进入视图目录: " + current);
                m_viewPager.setCurrentItem(current);
            }
        }
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId())
        {
            case R.id.file_browser_menu_open_drawer:
                if(m_drawerLayout.isDrawerOpen(Gravity.LEFT))
                    m_drawerLayout.closeDrawer(Gravity.LEFT, true);
                else
                    m_drawerLayout.openDrawer(Gravity.LEFT, true);
                break;
            default:
                return super.onOptionsItemSelected(item);
        }

        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        App.Instance().PopActivity();
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
                        ActivityUtility.OpenAppSetting(FileBrowserActivity.this);
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

    // internal
    private class FileViewAdapter extends ArrayAdapter_base<FileBrowser.FileModel>
    {
        private FileBrowser m_fileBrowser;
        private TextView m_partner;

        public FileViewAdapter(Context context, String path, TextView partner)
        {
            super(context, R.layout.file_browser_deledate);
            m_partner = partner;
            m_fileBrowser = new FileBrowser(path);
            SetData(m_fileBrowser.FileList());
            m_fileBrowser.SetOnCurrentChangedListener(new FileBrowser.FileBrowserCurrentChangedListener() {
                @Override
                public void OnCurrentChanged(FileBrowser browser, int mask) {
                    if((mask | ID_FILE_BROWSER_CURRENT_CHANGE_LIST) != 0)
                        SetData(browser.FileList());
                    if(m_partner != null && ((mask | ID_FILE_BROWSER_CURRENT_CHANGE_PATH) != 0))
                        m_partner.setText(browser.CurrentPath());
                }
            });
        }

        public View GenerateView(int position, View view, ViewGroup parent, FileBrowser.FileModel data)
        {
            TextView textView;

            textView = (TextView)view.findViewById(R.id.file_browser_delegate_name);
            textView.setText(data.name);
            textView = (TextView)view.findViewById(R.id.file_browser_delegate_size);
            textView.setText(FS.FormatSize(data.size));
            ((ImageView)view.findViewById(R.id.file_browser_delegate_icon)).setImageResource(data.type == FileBrowser.FileModel.ID_FILE_TYPE_DIRECTORY ? R.drawable.icon_folder : R.drawable.icon_file);
            return view;
        }

        public void Open(int index)
        {
            FileBrowser.FileModel item;

            item = m_fileBrowser.GetFileModel(index);
            if(item == null)
                return;
            if(item.type != FileBrowser.FileModel.ID_FILE_TYPE_DIRECTORY) // TODO: 处理符号链接文件
            {
                Logf.d(ID_TAG, "打开文件(%s)", item.path);
                ActivityUtility.OpenExternally(FileBrowserActivity.this, item.path);
                return;
            }

            SetPath(item.path);
        }

        public void SetPath(String path)
        {
            m_fileBrowser.SetCurrentPath(path);
        }
    }

    private class FileView
    {
        public View view;
        private FileViewAdapter m_adapter;

        public FileView(View view)
        {
            this.view = view;

            SetupUI();
        }

        private void SetupUI()
        {
            ListView listView;

            listView = (ListView)view.findViewById(R.id.file_browser_list_view);
            m_adapter = new FileViewAdapter(view.getContext(), null, (TextView)view.findViewById(R.id.file_browser_title));
            listView.setAdapter(m_adapter);
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    FileViewAdapter adapter;

                    adapter = (FileViewAdapter)parent.getAdapter();
                    adapter.Open(position);
                }
            });
        }

        public void SetPath(String path)
        {
            m_adapter.SetPath(path);
        }
    }

    private class FileMenuModel
    {
        public String label;
        public String name;
        public int icon;
        public boolean enabled = true;
        public FileView fileView = null;

        public FileMenuModel(String label, String name, int icon, boolean enabled) {
            this.label = label;
            this.name = name;
            this.icon = icon;
            this.enabled = enabled;
        }
    }

    private class FileMenuListAdapter extends ArrayAdapter_base<FileMenuModel>
    {
        public FileMenuListAdapter(Context context)
        {
            super(context, R.layout.file_menu_deledate);
        }

        public View GenerateView(int position, View view, ViewGroup parent, FileMenuModel data)
        {
            TextView textView;

            textView = (TextView)view.findViewById(R.id.file_browser_menu_delegate_name);
            textView.setText(data.label);
            textView = (TextView)view.findViewById(R.id.file_browser_menu_delegate_path);
            textView.setText(data.name);
            return view;
        }
    }

    private class FileViewPagerAdapter extends PagerAdapter
    {
        private List<FileMenuModel> m_menuList;

        public FileViewPagerAdapter(List<FileMenuModel> menuList)
        {
            m_menuList = menuList;
        }

        @Override
        public int getCount() {
            return m_menuList.size();
        }

        @Override
        public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
            return view == object;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            LayoutInflater li;
            FileMenuModel item;

            li = LayoutInflater.from(container.getContext()); //getLayoutInflater();
            item = m_menuList.get(position);
            if(item.fileView == null)
            {
                View view = li.inflate(R.layout.file_browser, null, false);
                item.fileView = new FileView(view);
            }
            item.fileView.SetPath(item.name);
            container.addView(item.fileView.view);
            return item.fileView.view;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView(m_menuList.get(position).fileView.view);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return m_menuList.get(position).label;
        }
    }
}
