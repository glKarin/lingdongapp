package com.youtushuju.lingdongapp;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

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
        ((TextView)findViewById(R.id.system_info_api_version_text)).setText(m_lingdongApi.GetSystemApiVersion());
        ((TextView)findViewById(R.id.system_info_android_os_version_text)).setText(m_lingdongApi.GetSystemAndroidOsVersion());
        ((TextView)findViewById(R.id.system_info_kernel_version_text)).setText(m_lingdongApi.GetSystemKernelVersion());
        ((TextView)findViewById(R.id.system_info_device_model_text)).setText(m_lingdongApi.GetSystemDeviceModel());
        ((TextView)findViewById(R.id.system_info_builder_number_display_text)).setText(m_lingdongApi.GetSystemBuilderNumberDisplay());
        ((TextView)findViewById(R.id.system_info_running_memory_text)).setText(m_lingdongApi.GetSystemRunningMemory());
        ((TextView)findViewById(R.id.system_info_internal_storage_memory_text)).setText(m_lingdongApi.GetSystemInternalStorageMemory());
        ((TextView)findViewById(R.id.system_info_internal_free_storage_memory_text)).setText(m_lingdongApi.GetSystemInternalFreeStorageMemory());

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
