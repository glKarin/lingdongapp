package com.youtushuju.lingdongapp;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.youtushuju.lingdongapp.device.LingDongApi;
import com.youtushuju.lingdongapp.common.Configs;
import com.youtushuju.lingdongapp.gui.App;

public class ControlActivity extends AppCompatActivity {
    private static final String ID_TAG = "ControlActivity";
    private LingDongApi m_lingdongApi;
    private boolean m_orientationSelected = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.control_page);

        m_lingdongApi = Configs.Instance().GetLingDongApi(this);
        SetupUI();

        App.Instance().PushActivity(this);
    }

    private void SetupUI()
    {
        ((Button)findViewById(R.id.control_reboot)).setOnClickListener(m_clickListener);
        ((Button)findViewById(R.id.control_shutdown)).setOnClickListener(m_clickListener);
        ((Button)findViewById(R.id.control_wakeup)).setOnClickListener(m_clickListener);
        ((Button)findViewById(R.id.control_sleep)).setOnClickListener(m_clickListener);
        ((Button)findViewById(R.id.control_reset)).setOnClickListener(m_clickListener);

        ((Spinner)findViewById(R.id.control_orientation)).setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // 初始化完成后会自动调用一次
                if(!m_orientationSelected)
                {
                    m_orientationSelected = true;
                    return;
                }
                String rotations[] = ControlActivity.this.getResources().getStringArray(R.array.rotation);
                int rotation = Integer.parseInt(rotations[position]);
                m_lingdongApi.DeviceSetOrientation(rotation);
                Toast.makeText(ControlActivity.this, "设备屏幕方向设置为" + rotation + "度", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        final TextView brightnessText = (TextView)findViewById(R.id.control_brightness_text);
        ((SeekBar)findViewById(R.id.control_brightness)).setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(!fromUser)
                    return;
                m_lingdongApi.DeviceSetBrightness(progress);
                brightnessText.setText("" + progress);
                Toast.makeText(ControlActivity.this, "设备屏幕亮度设置为" + progress, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    private View.OnClickListener m_clickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId())
            {
                case R.id.control_reboot:
                    ShowConfirmDialog("重启设备", new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(ControlActivity.this, "重启中...", Toast.LENGTH_SHORT).show();
                            m_lingdongApi.DeviceReboot();
                        }
                    });
                    break;
                case R.id.control_reset:
                    ShowConfirmDialog("设备恢复出厂", new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(ControlActivity.this, "恢复出厂设置中...", Toast.LENGTH_SHORT).show();
                            m_lingdongApi.DeviceReset();
                        }
                    });
                    break;
                case R.id.control_shutdown:
                    ShowConfirmDialog("设备关机", new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(ControlActivity.this, "关机中...", Toast.LENGTH_SHORT).show();
                            m_lingdongApi.DeviceShutdown();
                        }
                    });
                    break;
                case R.id.control_wakeup:
                    m_lingdongApi.DeviceWakeUp();
                    Toast.makeText(ControlActivity.this, "设备已唤醒", Toast.LENGTH_SHORT).show();
                    break;
                case R.id.control_sleep:
                    m_lingdongApi.DeviceSleep();
                    Toast.makeText(ControlActivity.this, "设备已休眠", Toast.LENGTH_SHORT).show();
                    break;
                default:
                    break;
            }
        }
    };

    private void ShowConfirmDialog(String message, final Runnable action)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("警告");
        builder.setMessage("确定要" + message + "?");
        builder.setIcon(R.drawable.icon_profile);
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                action.run();
            }
        });
        builder.setNegativeButton("取消", null);
                AlertDialog dialog = builder.create();
        dialog.show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        App.Instance().PopActivity();
    }
}
