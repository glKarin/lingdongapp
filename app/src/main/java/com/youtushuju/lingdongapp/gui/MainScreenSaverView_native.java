package com.youtushuju.lingdongapp.gui;

import android.graphics.Color;
import android.os.Handler;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.widget.TextView;

import com.youtushuju.lingdongapp.MainActivity;
import com.youtushuju.lingdongapp.R;
import com.youtushuju.lingdongapp.device.HeartbeatRespStruct;

public class MainScreenSaverView_native extends MainScreenSaverView {
    private static final String ID_TAG = "MainScreenSaverView_native";
    private static final String CONST_DEFAULT_MESSAGE = "请选择垃圾投放类型";
    private TextView m_screenSaverMessage;
    private ScrollImage m_carousel;

    private View.OnClickListener m_buttonListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId())
            {
                case R.id.main_screensaver_kitchen_waste_button:
                    m_mainActivity.ToDropWaste("waste");
                    break;
                case R.id.main_screensaver_other_waste_button:
                    m_mainActivity.ToDropWaste("other");
                    break;
                case R.id.main_screensaver_menu_button:
                    m_mainActivity.ToOpenMenu();
                    break;
                default:
                    break;
            }
        }
    };

    public MainScreenSaverView_native(MainActivity view, Handler handler)
    {
        super(view, handler);

        m_screenSaverMessage = (TextView) m_mainActivity.findViewById(R.id.main_screensaver_message);
        m_carousel = (ScrollImage) m_mainActivity.findViewById(R.id.main_screensaver_carousel);
    }

    public void Setup()
    {
        View view;

        view = m_mainActivity.findViewById(R.id.main_screensaver_menu_button);
        view.setOnClickListener(m_buttonListener);
        view = m_mainActivity.findViewById(R.id.main_screensaver_kitchen_waste_button);
        view.setOnClickListener(m_buttonListener);
        view = m_mainActivity.findViewById(R.id.main_screensaver_other_waste_button);
        view.setOnClickListener(m_buttonListener);
    }

    public void onResume()
    {
        m_carousel.Start(0);
        m_view.setVisibility(View.VISIBLE);
    }

    public void onPause()
    {
        //m_view.setVisibility(View.INVISIBLE);
        m_carousel.Pause();
        m_view.setVisibility(View.GONE);
    }

    public void NotifyDeviceStatus(String code, String desc)
    {
        boolean work = HeartbeatRespStruct.DeviceIsNormal(code);
        m_screenSaverMessage.setTextColor(work ? Color.BLACK : Color.RED);
        m_screenSaverMessage.setText(work ? CONST_DEFAULT_MESSAGE : desc);
    }

}
