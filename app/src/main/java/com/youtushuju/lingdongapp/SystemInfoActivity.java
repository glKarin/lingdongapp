package com.youtushuju.lingdongapp;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import android.text.Html;
import android.view.View;
import android.widget.TextView;

import com.youtushuju.lingdongapp.device.LingDongApi;
import com.youtushuju.lingdongapp.common.Configs;
import com.youtushuju.lingdongapp.gui.App;

import java.util.Timer;
import java.util.TimerTask;

public class SystemInfoActivity extends AppCompatActivity {
    private static final String ID_TAG = "SystemInfoActivity";
    private static final int ID_TIMER_INTERVAL = 2000;

    private LingDongApi m_lingdongApi;
    private Timer m_timer;

    private View.OnClickListener m_pathClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            int index = -1;
            switch (v.getId())
            {
                case R.id.system_info_primary_storage_path_text:
                    index = 0;
                    break;
                case R.id.system_info_sdcard_path_text:
                    index = 1;
                    break;
                case R.id.system_info_usb_path_text:
                    index = 2;
                    break;
                default:
                    break;
            }
            if(index != -1)
            {
                Intent intent = new Intent(SystemInfoActivity.this, FileBrowserActivity.class);
                Bundle extras = new Bundle();
                extras.putInt(FileBrowserActivity.ID_CURRENT_PAGE_PATH, index);
                intent.putExtras(extras);
                SystemInfoActivity.this.startActivity(intent);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.system_info_page);

        m_lingdongApi = Configs.Instance().GetLingDongApi(this);
        SetupUI();

        App.Instance().PushActivity(this);
    }

    private void SetupUI()
    {
        TextView textView;

        ((TextView)findViewById(R.id.system_info_api_version_text)).setText(m_lingdongApi.GetSystemApiVersion());
        ((TextView)findViewById(R.id.system_info_android_os_version_text)).setText(m_lingdongApi.GetSystemAndroidOsVersion());
        ((TextView)findViewById(R.id.system_info_kernel_version_text)).setText(m_lingdongApi.GetSystemKernelVersion());
        ((TextView)findViewById(R.id.system_info_device_model_text)).setText(m_lingdongApi.GetSystemDeviceModel());
        ((TextView)findViewById(R.id.system_info_builder_number_display_text)).setText(m_lingdongApi.GetSystemBuilderNumberDisplay());
        ((TextView)findViewById(R.id.system_info_running_memory_text)).setText(m_lingdongApi.GetSystemRunningMemory());
        ((TextView)findViewById(R.id.system_info_internal_storage_memory_text)).setText(m_lingdongApi.GetSystemInternalStorageMemory());
        ((TextView)findViewById(R.id.system_info_internal_free_storage_memory_text)).setText(m_lingdongApi.GetSystemInternalFreeStorageMemory());

        textView = ((TextView)findViewById(R.id.system_info_primary_storage_path_text));
        textView.setText(Html.fromHtml("<u>" + m_lingdongApi.GetSystemPrimaryStoragePath() + "</u>"));
        textView.setOnClickListener(m_pathClickListener);
        textView = ((TextView)findViewById(R.id.system_info_sdcard_path_text));
        textView.setText(Html.fromHtml("<u>" + m_lingdongApi.GetSystemSdcardPath() + "</u>"));
        textView.setOnClickListener(m_pathClickListener);
        textView = ((TextView)findViewById(R.id.system_info_usb_path_text));
        textView.setText(Html.fromHtml("<u>" + m_lingdongApi.GetSystemUSBPath() + "</u>"));
        textView.setOnClickListener(m_pathClickListener);

        ((TextView)findViewById(R.id.system_info_screen_width_text)).setText("" + m_lingdongApi.GetSystemScreenWidth());
        ((TextView)findViewById(R.id.system_info_screen_height_text)).setText("" + m_lingdongApi.GetSystemScreenHeight());

        m_timer = new Timer();
        m_timer.scheduleAtFixedRate(new TimerTask(){
            public void run()
            {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ((TextView)findViewById(R.id.system_info_running_memory_text)).setText(m_lingdongApi.GetSystemRunningMemory());
                        ((TextView)findViewById(R.id.system_info_internal_free_storage_memory_text)).setText(m_lingdongApi.GetSystemInternalFreeStorageMemory());
                    }
                });
            }
        }, ID_TIMER_INTERVAL, ID_TIMER_INTERVAL);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if(m_timer != null)
        {
            m_timer.cancel();
            m_timer.purge();
        }
        App.Instance().PopActivity();
    }
}
