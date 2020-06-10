package com.youtushuju.lingdongapp.gui;

import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.youtushuju.lingdongapp.MainActivity;
import com.youtushuju.lingdongapp.R;

public class MainQRCodeView {
    private static final String ID_TAG = "MainQRCodeView";
    private View m_view;
    private MainActivity m_mainActivity;
    private ViewHolder m_viewHolder = new ViewHolder();

    public MainQRCodeView(MainActivity activity, Handler handler)
    {
        m_mainActivity = activity;
    }

    private void Create()
    {
        View view;
        LayoutInflater inflater;

        inflater = LayoutInflater.from(m_mainActivity);
        view = inflater.inflate(R.layout.main_maintenance_panel, null);
    }

    public void Open(boolean anim)
    {
        m_view.setVisibility(View.VISIBLE);
    }

    public void Close(boolean anim)
    {
        m_view.setVisibility(View.GONE);
    }

    private class ViewHolder{
        public TextView labelView;
    };
}