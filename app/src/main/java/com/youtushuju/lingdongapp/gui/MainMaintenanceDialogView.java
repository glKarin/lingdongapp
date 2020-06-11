package com.youtushuju.lingdongapp.gui;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.os.Handler;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.youtushuju.lingdongapp.MainActivity;
import com.youtushuju.lingdongapp.R;
import com.youtushuju.lingdongapp.api.UserModel;

import java.util.ArrayList;
import java.util.List;

public class MainMaintenanceDialogView extends MainActivityView_base {
    private static final String ID_TAG = "MainMaintenanceDialogView";
    private UserModel m_user;

    public MainMaintenanceDialogView(MainActivity activity, Handler handler)
    {
        super(activity, handler);
    }

    public void SetUser(UserModel user)
    {
        m_user = user;
    }

    public void Create()
    {
        super.Create();
        ViewHolder viewHolder = (ViewHolder)m_viewHolder;

        viewHolder.m_door3Button.setOnClickListener(m_clickListener);
        viewHolder.m_door4Button.setOnClickListener(m_clickListener);
        if(m_user.IsAdministrator())
        {
            viewHolder.m_maintenanceMenuButton.setOnClickListener(m_clickListener);
            viewHolder.m_maintenanceMenuButton.setVisibility(View.VISIBLE);
        }
        else
        {
            viewHolder.m_maintenanceMenuButton.setOnClickListener(null);
            viewHolder.m_maintenanceMenuButton.setVisibility(View.GONE);
        }
    }

    private View.OnClickListener m_clickListener = new View.OnClickListener()
    {
        @Override
        public void onClick(View v) {
            m_mainActivity.ChooseMaintenance(m_user, v.getId());
        }
    };

    private class ViewHolder extends MainActivityView_base.ViewHolder {
        public TextView m_door3Button;
        public TextView m_door4Button;
        public ImageView m_maintenanceMenuButton;
    };

    protected void AddView(View view, FrameLayout layout)
    {
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        layoutParams.gravity = Gravity.CENTER;
        layout.addView(m_view, layoutParams);
    }

    protected View GenView(LayoutInflater inflater)
    {
        View view = inflater.inflate(R.layout.main_maintenance_panel, null);
        return view;
    }

    protected MainActivityView_base.ViewHolder GenViewHolder(View view)
    {
        ViewHolder viewHolder = new ViewHolder();
        viewHolder.m_door3Button = (TextView)view.findViewById(R.id.main_maintenance_door3);
        viewHolder.m_door4Button = (TextView)view.findViewById(R.id.main_maintenance_door4);
        viewHolder.m_maintenanceMenuButton = (ImageView)view.findViewById(R.id.main_maintenance_menu_btn);
        return viewHolder;
    }

    protected AnimatorSet GetOpenAnimation()
    {
        AnimatorSet animatorSet = (AnimatorSet) AnimatorInflater.loadAnimator(m_mainActivity, R.animator.result_dialog_open_anim);
        animatorSet.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                m_mainActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        m_view.setVisibility(View.VISIBLE);
                    }
                });
            }

            @Override
            public void onAnimationEnd(Animator animation) {
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                animation.end();
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
                animation.end();
            }
        });
        return animatorSet;
    }

    protected AnimatorSet GetCloseAnimation()
    {
        AnimatorSet animatorSet = (AnimatorSet) AnimatorInflater.loadAnimator(m_mainActivity, R.animator.result_dialog_close_anim);
        animatorSet.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                m_mainActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Shutdown();
                    }
                });
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                animation.end();
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
                animation.end();
            }
        });

        return animatorSet;
    }

    public void Close(boolean anim) {
        super.Close(anim);
        m_user = null;
    }

    public void Open(UserModel user, boolean anim) {
        SetUser(user);
        Create();
        super.Open(anim);
    }
}