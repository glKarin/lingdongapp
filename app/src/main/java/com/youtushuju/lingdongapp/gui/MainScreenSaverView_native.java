package com.youtushuju.lingdongapp.gui;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ShapeDrawable;
import android.os.Handler;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.widget.TextView;

import com.youtushuju.lingdongapp.MainActivity;
import com.youtushuju.lingdongapp.R;
import com.youtushuju.lingdongapp.device.DropModeReqStruct;
import com.youtushuju.lingdongapp.device.HeartbeatRespStruct;

public class MainScreenSaverView_native extends MainScreenSaverView {
    private static final String ID_TAG = "MainScreenSaverView_native";
    private static final String CONST_DEFAULT_MESSAGE = "请选择垃圾投放类型";
    private TextView m_screenSaverMessage;
    private ScrollImage m_carousel;
    private TextView m_kitchenFaceButton;
    private TextView m_otherFaceButton;
    private TextView m_kitchenCodeButton;
    private TextView m_otherCodeButton;

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
                case R.id.main_screensaver_kitchen_waste_scancode_button:
                    m_mainActivity.ToDropWasteByScanCode("waste");
                    break;
                case R.id.main_screensaver_other_waste_scancode_button:
                    m_mainActivity.ToDropWasteByScanCode("other");
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
        m_kitchenFaceButton = m_mainActivity.findViewById(R.id.main_screensaver_kitchen_waste_button);
        m_kitchenFaceButton.setOnClickListener(m_buttonListener);
        m_otherFaceButton = m_mainActivity.findViewById(R.id.main_screensaver_other_waste_button);
        m_otherFaceButton.setOnClickListener(m_buttonListener);
        m_kitchenCodeButton = m_mainActivity.findViewById(R.id.main_screensaver_kitchen_waste_scancode_button);
        m_kitchenCodeButton.setOnClickListener(m_buttonListener);
        m_otherCodeButton = m_mainActivity.findViewById(R.id.main_screensaver_other_waste_scancode_button);
        m_otherCodeButton.setOnClickListener(m_buttonListener);
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

    public void SetDropMode(String mode)
    {
        Resources resources = m_mainActivity.getResources();
        boolean allowFace = DropModeReqStruct.AllowDropMode(DropModeReqStruct.CONST_DROP_MODE_FACE, mode);
        boolean allowCode = DropModeReqStruct.AllowDropMode(DropModeReqStruct.CONST_DROP_MODE_CODE, mode);

        if(allowFace)
        {
            m_kitchenFaceButton.setBackground(resources.getDrawable(R.drawable.kitchen_waste_button_style));
            m_otherFaceButton.setBackground(resources.getDrawable(R.drawable.other_waste_button_style));
            m_kitchenFaceButton.setTextColor(resources.getColor(R.color.kitchen_waste_text_color));
            m_otherFaceButton.setTextColor(resources.getColor(R.color.other_waste_text_color));
        }
        else
        {
            m_kitchenFaceButton.setBackground(resources.getDrawable(R.drawable.kitchen_waste_button_disable_style));
            m_otherFaceButton.setBackground(resources.getDrawable(R.drawable.other_waste_button_disable_style));
            m_kitchenFaceButton.setTextColor(resources.getColor(R.color.kitchen_waste_text_disable_color));
            m_otherFaceButton.setTextColor(resources.getColor(R.color.other_waste_text_disable_color));
        }
        if(allowCode)
        {
            m_kitchenCodeButton.setBackground(resources.getDrawable(R.drawable.kitchen_waste_button_style));
            m_otherCodeButton.setBackground(resources.getDrawable(R.drawable.other_waste_button_style));
            m_kitchenCodeButton.setTextColor(resources.getColor(R.color.kitchen_waste_text_color));
            m_otherCodeButton.setTextColor(resources.getColor(R.color.other_waste_text_color));
        }
        else
        {
            m_kitchenCodeButton.setBackground(resources.getDrawable(R.drawable.kitchen_waste_button_disable_style));
            m_otherCodeButton.setBackground(resources.getDrawable(R.drawable.other_waste_button_disable_style));
            m_kitchenCodeButton.setTextColor(resources.getColor(R.color.kitchen_waste_text_disable_color));
            m_otherCodeButton.setTextColor(resources.getColor(R.color.other_waste_text_disable_color));
        }
    }
}
