package com.youtushuju.lingdongapp.gui;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.youtushuju.lingdongapp.R;
import com.youtushuju.lingdongapp.common.Logf;

import java.util.List;

public class PipelineView extends ScrollView
{
    private static final String ID_TAG = "PipelineView";
    public static final int ENUM_STATE_READY = 0;
    public static final int ENUM_STATE_PROCESSING = 1;
    public static final int ENUM_STATE_FINISHED = 2;
    public static final int ENUM_STATE_WARNING = 3;
    public static final int ENUM_STATE_ERROR = 4;

    // 颜色
    private int m_readyColor = Color.BLACK;
    private int m_processingColor = Color.BLUE;
    private int m_finishColor = Color.GREEN;
    private int m_warningColor = Color.YELLOW;
    private int m_errorColor = Color.RED;

    private LinearLayout m_layout;
    private List<PipelineItemModel> m_list;
    private int m_current = 0;
    private PipelineListener m_pipelineListener = null;

    public interface PipelineListener
    {
        public void OnStarted(PipelineView view);
        public void OnFinished(PipelineView view);
        public void OnStateChanged(PipelineView view, int index);
        public void OnNextStart(PipelineView view, int index);
        public void OnTimeout(PipelineView view, int index);
    }

    public PipelineView(Context context)
    {
        super(context);
        Setup(null);
    }

    public PipelineView(Context context, AttributeSet attributeSet)
    {
        super(context, attributeSet);
        Setup(attributeSet);
    }

    public PipelineView(Context context, AttributeSet attributeSet, int defStyleAttr)
    {
        super(context, attributeSet, defStyleAttr);
        Setup(attributeSet);
    }

    public PipelineView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        Setup(attrs);
    }

    private void Setup(AttributeSet attrs)
    {
        if(attrs != null)
        {
        }

        m_layout = new LinearLayout(getContext());
        m_layout.setOrientation(LinearLayout.VERTICAL);
        ScrollView.LayoutParams layoutParams = new ScrollView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        addView(m_layout, layoutParams);
    }

    public void Ready()
    {
        if(m_list != null && !m_list.isEmpty())
        {
            for (PipelineItemModel item : m_list)
                item.Ready();
            m_current = 0;
        }
        else
            m_current = -1;
    }

    // 转为结束状态
    public void Finish()
    {
    }

    public void Reset()
    {
        SetList(null);
    }

    public void Shutdown()
    {
        Reset();
    }

    public void SetPipelineListener(PipelineListener l)
    {
        m_pipelineListener = l;
    }

    public void SetList(List<PipelineItemModel> list)
    {
        m_list = list;
        Relayout();
    }

    public void Start()
    {
        m_current = 0;
        PipelineItemModel currentItem = m_list.get(m_current);
        currentItem.Processing();
        if(m_pipelineListener != null)
            m_pipelineListener.OnStarted(this);
    }

    public void Next()
    {
        PipelineItemModel currentItem = m_list.get(m_current);
        PipelineItemModel nextItem = m_current < m_list.size() - 1 ? m_list.get(m_current + 1) : null;
        currentItem.current = false;
        currentItem.Finish();
        if(nextItem == null)
        {
            if(m_pipelineListener != null)
                m_pipelineListener.OnFinished(this);
        }
        else
        {
            nextItem.current = true;
            m_current++;
            nextItem.Processing();
            if(m_pipelineListener != null)
                m_pipelineListener.OnNextStart(this, m_current);
        }
    }

    public void Stop()
    {
        PipelineItemModel currentItem = m_list.get(m_current);
        currentItem.Error();
    }

    public void Timeout()
    {
        if(m_pipelineListener != null)
            m_pipelineListener.OnTimeout(this, m_current);
        Logf.e(ID_TAG, "流程步骤到时: " + m_current);
    }

    private void Relayout()
    {
        m_layout.removeAllViews();
        if(m_list == null)
            return;

        for (int i = 0; i < m_list.size(); i++)
        {
            PipelineItemModel item = m_list.get(i);
            item.SetView(i, this);
        }
    }

    public static class PipelineItemModel
    {
        public boolean current = false;
        public int state = ENUM_STATE_READY;
        public String content;
        public int time_limit = 0;

        private int m_index = -1;
        private PipelineView m_pipelineView;
        private View m_view;
        private ViewHolder m_viewHolder;

        public PipelineItemModel()
        {
        }

        public PipelineItemModel(String content, int timeLimit)
        {
            this.content = content;
            time_limit = timeLimit;
        }

        private void SetView(int index, PipelineView view)
        {
            m_index = index;
            m_pipelineView = view;
            m_view = LayoutInflater.from(m_pipelineView.getContext()).inflate(R.layout.pipeline_deledate, null);
            m_viewHolder = new ViewHolder();
            m_viewHolder.indicator = (CircleProgressIndicatorView)m_view.findViewById(R.id.pipeline_delegate_indicator);
            m_viewHolder.content = (TextView) m_view.findViewById(R.id.pipeline_delegate_content);

            m_viewHolder.content.setText(content);
            Ready();

            m_pipelineView.m_layout.addView(m_view);
        }

        private static class ViewHolder {
            CircleProgressIndicatorView indicator;
            TextView content;
        };

        private void Ready()
        {
            m_viewHolder.indicator.SetBackgroundColor(m_pipelineView.m_readyColor);
            m_viewHolder.content.setTextColor(m_pipelineView.m_readyColor);
            if(m_pipelineView.m_current == m_index)
            {
                current = true;
            }
            m_viewHolder.content.getPaint().setFakeBoldText(current); // 当前加粗
            SetState(ENUM_STATE_READY);
        }

        private void Processing()
        {
            m_viewHolder.indicator.SetBackgroundColor(m_pipelineView.m_processingColor);
            m_viewHolder.content.setTextColor(m_pipelineView.m_processingColor);
            if(m_pipelineView.m_current == m_index)
            {
                current = true;
            }
            m_viewHolder.content.getPaint().setFakeBoldText(current); // 当前加粗
            if(time_limit >= 0)
            {
                m_viewHolder.indicator.SetProgressListener(new CircleProgressIndicatorView.ProgressListener() {
                    @Override
                    public void OnProgress(CircleProgressIndicatorView view) {

                    }

                    @Override
                    public void OnReady(CircleProgressIndicatorView view) {

                    }

                    @Override
                    public void OnStarted(CircleProgressIndicatorView view) {

                    }

                    @Override
                    public void OnFinished(CircleProgressIndicatorView view) {
                        if(time_limit > 0)
                        {
                            m_pipelineView.Timeout();
                        }
                    }

                    @Override
                    public void OnStateChanged(CircleProgressIndicatorView view) {

                    }
                });
            }
            m_viewHolder.indicator.SetDrawBorder(true);
            m_viewHolder.indicator.SetEndValue(time_limit);
            m_viewHolder.indicator.SetProgress(time_limit);
            m_viewHolder.indicator.SetLabelFormatter(CircleProgressIndicatorView.ENUM_LABEL_FORMATTER_SECOND);
            m_viewHolder.indicator.SetAutoGrow(true);
            SetState(ENUM_STATE_PROCESSING);
        }

        private void Finish()
        {
            m_viewHolder.indicator.SetBackgroundColor(m_pipelineView.m_finishColor);
            m_viewHolder.content.setTextColor(m_pipelineView.m_finishColor);
            if(m_pipelineView.m_current == m_index)
            {
                current = true;
            }
            m_viewHolder.content.getPaint().setFakeBoldText(current); // 当前加粗
            m_viewHolder.indicator.SetDrawBorder(false);
            if(m_viewHolder.indicator.IsFinished())
                m_viewHolder.indicator.Finish();
            m_viewHolder.indicator.SetLabelFormatter(CircleProgressIndicatorView.ENUM_LABEL_FORMATTER_NONE);
            SetState(ENUM_STATE_FINISHED);
        }

        private void Warning()
        {
            m_viewHolder.indicator.SetBackgroundColor(m_pipelineView.m_warningColor);
            m_viewHolder.content.setTextColor(m_pipelineView.m_warningColor);
            if(m_pipelineView.m_current == m_index)
            {
                current = true;
            }
            m_viewHolder.content.getPaint().setFakeBoldText(current); // 当前加粗
            m_viewHolder.indicator.SetDrawBorder(false);
            if(m_viewHolder.indicator.IsFinished())
                m_viewHolder.indicator.Finish();
            m_viewHolder.indicator.SetLabelFormatter(CircleProgressIndicatorView.ENUM_LABEL_FORMATTER_NONE);
            SetState(ENUM_STATE_WARNING);
        }

        private void Error()
        {
            m_viewHolder.indicator.SetBackgroundColor(m_pipelineView.m_errorColor);
            m_viewHolder.content.setTextColor(m_pipelineView.m_errorColor);
            if(m_pipelineView.m_current == m_index)
            {
                current = true;
            }
            m_viewHolder.content.getPaint().setFakeBoldText(current); // 当前加粗
            m_viewHolder.indicator.SetDrawBorder(false);
            if(m_viewHolder.indicator.IsFinished())
                m_viewHolder.indicator.Finish();
            m_viewHolder.indicator.SetLabelFormatter(CircleProgressIndicatorView.ENUM_LABEL_FORMATTER_NONE);
            SetState(ENUM_STATE_ERROR);
        }

        private void SetState(int state)
        {
            if(this.state != state)
            {
                this.state = state;
                if(m_pipelineView.m_pipelineListener != null)
                    m_pipelineView.m_pipelineListener.OnStateChanged(m_pipelineView, m_index);
            }
        }
    }
}