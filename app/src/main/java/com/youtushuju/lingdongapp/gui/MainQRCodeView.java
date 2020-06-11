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

import java.util.ArrayList;
import java.util.List;

public class MainQRCodeView extends MainActivityView_base {
    private static final String ID_TAG = "MainQRCodeView";

    public MainQRCodeView(MainActivity activity, Handler handler)
    {
        super(activity, handler);
        Create();
    }

    public void Create()
    {
        super.Create();
        ViewHolder viewHolder = (ViewHolder)m_viewHolder;

        List<PipelineView.PipelineItemModel> list = new ArrayList<PipelineView.PipelineItemModel>();
        list.add(new PipelineView.PipelineItemModel("扫码二维码", 10000));
        list.add(new PipelineView.PipelineItemModel("开门投放垃圾", 10000));
        list.add(new PipelineView.PipelineItemModel("正在关门", -1));
        viewHolder.pipeline.SetList(list);
        viewHolder.pipeline.Ready();
    }

    private class ViewHolder extends MainActivityView_base.ViewHolder {
        public TextView label_view;
        public PipelineView pipeline;
    };

    protected void AddView(View view, FrameLayout layout)
    {
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        layoutParams.gravity = Gravity.CENTER;
        layout.addView(m_view, layoutParams);
    }

    protected View GenView(LayoutInflater inflater)
    {
        View view = inflater.inflate(R.layout.main_qrcode_panel, null);
        return view;
    }

    protected MainActivityView_base.ViewHolder GenViewHolder(View view)
    {
        ViewHolder viewHolder = new ViewHolder();
        viewHolder.label_view = view.findViewById(R.id.main_qrcode_lebel);
        viewHolder.pipeline = view.findViewById(R.id.main_qrcode_pipeline);
        return viewHolder;
    }

    protected AnimatorSet GetOpenAnimation()
    {
        AnimatorSet animatorSet = (AnimatorSet) AnimatorInflater.loadAnimator(m_mainActivity, R.animator.pipeline_open_anim);
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
        AnimatorSet animatorSet = (AnimatorSet) AnimatorInflater.loadAnimator(m_mainActivity, R.animator.pipeline_open_anim);
        animatorSet.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                m_mainActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        m_view.setVisibility(View.INVISIBLE);
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
}