package com.youtushuju.lingdongapp;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Debug;
import android.os.Process;
import android.text.Html;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.youtushuju.lingdongapp.device.LingDongApi;
import com.youtushuju.lingdongapp.common.Configs;
import com.youtushuju.lingdongapp.gui.App;

import java.util.Timer;
import java.util.TimerTask;

public class SystemInfoActivity extends AppCompatActivity {
    private static final String ID_TAG = "SystemInfoActivity";
    private static final int ID_TIMER_INTERVAL = 5000;

    private LingDongApi m_lingdongApi;
    private Timer m_timer;

    private View.OnClickListener m_clickListener = new View.OnClickListener() {
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
                case R.id.system_info_dalvik_gc:
                    Toast.makeText(SystemInfoActivity.this, "Dalvik GC start", Toast.LENGTH_SHORT).show();
                    System.gc();
                    Toast.makeText(SystemInfoActivity.this, "Dalvik GC finished", Toast.LENGTH_LONG).show();
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
        textView.setOnClickListener(m_clickListener);
        textView = ((TextView)findViewById(R.id.system_info_sdcard_path_text));
        textView.setText(Html.fromHtml("<u>" + m_lingdongApi.GetSystemSdcardPath() + "</u>"));
        textView.setOnClickListener(m_clickListener);
        textView = ((TextView)findViewById(R.id.system_info_usb_path_text));
        textView.setText(Html.fromHtml("<u>" + m_lingdongApi.GetSystemUSBPath() + "</u>"));
        textView.setOnClickListener(m_clickListener);

        ((TextView)findViewById(R.id.system_info_screen_width_text)).setText("" + m_lingdongApi.GetSystemScreenWidth());
        ((TextView)findViewById(R.id.system_info_screen_height_text)).setText("" + m_lingdongApi.GetSystemScreenHeight());

        findViewById(R.id.system_info_dalvik_gc).setOnClickListener(m_clickListener);

        m_timer = new Timer();
        m_timer.scheduleAtFixedRate(new SystemInfoTimerTask(), 0, ID_TIMER_INTERVAL);
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

    private class SystemInfoTimerTask extends TimerTask{
        private TextView m_runningMemoryText = null;
        private TextView m_freeStorageMemoryText = null;
        private TextView m_dalvikMemText = null;
        private TextView m_nativeHeapMemText = null;
        private TextView m_gpuMemText = null;
        private TextView m_usedMemText = null;
        private TextView m_totalMemText = null;
        private final int m_processes[] = {Process.myPid()};

        public SystemInfoTimerTask()
        {
            m_runningMemoryText = (TextView)findViewById(R.id.system_info_running_memory_text);
            m_freeStorageMemoryText = (TextView)findViewById(R.id.system_info_internal_free_storage_memory_text);
            m_dalvikMemText = (TextView)findViewById(R.id.system_info_dalvik_mem_text);
            m_nativeHeapMemText = (TextView)findViewById(R.id.system_info_native_heap_mem_text);
            m_gpuMemText = (TextView)findViewById(R.id.system_info_gpu_mem_text);
            m_usedMemText = (TextView)findViewById(R.id.system_info_used_mem_text);
            m_totalMemText = (TextView)findViewById(R.id.system_info_total_mem_text);
        }

        public void run()
        {
            final ActivityManager am = (ActivityManager)getSystemService(Context.ACTIVITY_SERVICE);
            final int unit = 1024;
            final int unit2 = 1024 * 1024; // 1024 << 10
            final ActivityManager.MemoryInfo outInfo = new ActivityManager.MemoryInfo();

            am.getMemoryInfo(outInfo);
            int availMem = (int)(outInfo.availMem / unit2);
            int totalMem = (int)(outInfo.totalMem / unit2);
            int usedMem = (int)((outInfo.totalMem - outInfo.availMem) / unit2);

            Debug.MemoryInfo memInfos[] = am.getProcessMemoryInfo(m_processes);
            Debug.MemoryInfo memInfo = memInfos[0];
            int java_mem = 0;
            int native_mem = 0;
            int graphics_mem = 0;

            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            {
                java_mem = Integer.valueOf(memInfo.getMemoryStat("summary.java-heap"));
                native_mem = Integer.valueOf(memInfo.getMemoryStat("summary.native-heap"));
                graphics_mem = Integer.valueOf(memInfo.getMemoryStat("summary.graphics"));
            }
            else
            {
                java_mem = memInfo.dalvikPrivateDirty;
                native_mem = memInfo.nativePrivateDirty;
                graphics_mem = -1;
            }

            //String stack_mem = memInfo.getMemoryStat("summary.stack");
            //String code_mem = memInfo.getMemoryStat("summary.code");
            //String others_mem = memInfo.getMemoryStat("summary.system");
            final String dalvikMem = java_mem / unit + "M";
            final String nativeHeapMem = native_mem / unit + "M";
            final String gpuMem = (graphics_mem >= 0 ? graphics_mem / unit : -1) + "M";
            final String usedMemText = usedMem + "M";
            final String totalMemText = totalMem + "M";
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    m_runningMemoryText.setText(m_lingdongApi.GetSystemRunningMemory());
                    m_freeStorageMemoryText.setText(m_lingdongApi.GetSystemInternalFreeStorageMemory());

                    m_dalvikMemText.setText(dalvikMem);
                    m_nativeHeapMemText.setText(nativeHeapMem);
                    m_gpuMemText.setText(gpuMem);
                    m_usedMemText.setText(usedMemText);
                    m_totalMemText.setText(totalMemText); // TODO: 不需要每次
                }
            });
        }
    }
}
