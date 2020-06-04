package com.youtushuju.lingdongapp;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.youtushuju.lingdongapp.gui.App;
import com.youtushuju.lingdongapp.gui.StatusMachine;

import java.util.Timer;
import java.util.TimerTask;

public class RuntimeStatusActivity extends AppCompatActivity {
    private static final String ID_TAG = "RuntimeStatusActivity";
    private static final int ID_TIMER_INTERVAL = 5000;

    private TextView m_content;
    private Timer m_timer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.runtime_status_page);
        m_content = (TextView)findViewById(R.id.runtime_status_content);
        SetupUI();

        App.Instance().PushActivity(this);
    }

    private void SetupUI()
    {
        m_content.setText(StatusMachine.Instance().toString());

        m_timer = new Timer();
        m_timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        m_content.setText(StatusMachine.Instance().toString());
                    }
                });
            }
        }, 0, ID_TIMER_INTERVAL);
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
