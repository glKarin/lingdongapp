package com.youtushuju.lingdongapp.gui;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.os.Handler;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.youtushuju.lingdongapp.MainActivity;
import com.youtushuju.lingdongapp.R;
import com.youtushuju.lingdongapp.common.Logf;

import java.util.ArrayList;
import java.util.List;

public abstract class MainActivityView_base {
    private static final String ID_TAG = "MainActivityView_base";
    protected View m_view;
    protected MainActivity m_mainActivity;
    protected ViewHolder m_viewHolder = null;
    protected AnimatorSet m_openAnimation = null; // 打开动画
    protected AnimatorSet m_closeAnimation = null; // 关闭动画

    public MainActivityView_base(MainActivity activity, Handler handler)
    {
        m_mainActivity = activity;
    }

    public void Create()
    {
        LayoutInflater inflater;

        inflater = LayoutInflater.from(m_mainActivity);
        m_view = GenView(inflater);
        m_viewHolder = GenViewHolder(m_view);
        m_openAnimation = GetOpenAnimation();
        m_closeAnimation = GetCloseAnimation();
        m_openAnimation.setTarget(m_view);
        m_closeAnimation.setTarget(m_view);
        AddView(m_view, GetContentView());
        Logf.e(ID_TAG, "创建主界面视图");
    }

    protected abstract View GenView(LayoutInflater inflater);
    protected abstract ViewHolder GenViewHolder(View view);
    protected abstract AnimatorSet GetOpenAnimation();
    protected abstract AnimatorSet GetCloseAnimation();
    protected abstract void AddView(View view, FrameLayout layout);

    public void Shutdown()
    {
        Logf.e(ID_TAG, "主界面视图销毁");
        if(m_openAnimation != null)
        {
            m_openAnimation.setTarget(null);
            m_openAnimation = null;
        }
        if(m_closeAnimation != null)
        {
            m_closeAnimation.setTarget(null);
            m_closeAnimation = null;
        }
        if(m_view != null)
        {
            GetContentView().removeView(m_view);
            m_view = null;
        }
        m_viewHolder = null;
    }

    protected FrameLayout GetContentView()
    {
        return ((FrameLayout)m_mainActivity.GetFrontContentView());
    }

    public void Open(boolean anim)
    {
        if(!IsValid())
            return;
        if(anim)
            m_openAnimation.start();
        else
            m_openAnimation.end();
        Logf.e(ID_TAG, "打开主界面视图");
    }

    public void Close(boolean anim)
    {
        if(!IsValid())
            return;
        /*if(m_view.getVisibility() != View.VISIBLE)
            return;*/
        if(anim)
            m_closeAnimation.start();
        else
            m_closeAnimation.end();
        Logf.e(ID_TAG, "关闭主界面视图");
    }

    protected boolean IsValid()
    {
        return m_view != null;
    }

    protected class ViewHolder
    {
    };
}