package com.youtushuju.lingdongapp.gui;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.graphics.Color;
import android.os.Handler;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.youtushuju.lingdongapp.MainActivity;
import com.youtushuju.lingdongapp.R;
import com.youtushuju.lingdongapp.api.UserModel;

public class MainResultDialogView extends MainActivityView_base {
    private static final String ID_TAG = "MainResultDialogView";
    private boolean m_success = true;
    private String m_message;

    public MainResultDialogView(MainActivity activity, Handler handler)
    {
        super(activity, handler);
    }

    public void Create()
    {
        super.Create();
        ViewHolder viewHolder = (ViewHolder)m_viewHolder;
        viewHolder.message_view.setText(m_message);
        viewHolder.message_view.setTextColor(m_success ? Color.GREEN : Color.RED);
        viewHolder.icon_view.setImageResource(m_success ? R.drawable.icon_success : R.drawable.icon_error);
    }

    private View.OnClickListener m_clickListener = new View.OnClickListener()
    {
        @Override
        public void onClick(View v) {
        }
    };

    private class ViewHolder extends MainActivityView_base.ViewHolder {
        public TextView message_view;
        public ImageView icon_view;
    };

    protected void AddView(View view, FrameLayout layout)
    {
        int px = ActivityUtility.dp2px(m_mainActivity, 320);
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(px, px);
        layoutParams.gravity = Gravity.CENTER;
        layout.addView(m_view, layoutParams);
    }

    protected View GenView(LayoutInflater inflater)
    {
        View view = inflater.inflate(R.layout.main_result_panel, null);
        return view;
    }

    protected MainActivityView_base.ViewHolder GenViewHolder(View view)
    {
        ViewHolder viewHolder = new ViewHolder();
        viewHolder.message_view = (TextView)view.findViewById(R.id.main_result_message);
        viewHolder.icon_view = (ImageView)view.findViewById(R.id.main_result_icon);
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
                        if(m_view != null)
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
        m_message = null;
        m_success = true;
    }

    public void Open(boolean suc, String message, boolean anim) {
        m_message = message;
        m_success = suc;
        Create();
        super.Open(anim);
    }
}