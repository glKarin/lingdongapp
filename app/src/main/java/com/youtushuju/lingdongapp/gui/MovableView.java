package com.youtushuju.lingdongapp.gui;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Size;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import androidx.appcompat.widget.AppCompatImageButton;

import com.youtushuju.lingdongapp.common.Logf;

public class MovableView extends ViewGroup
{
    private static final String ID_TAG = "MovableImageButton";
    private int m_baseX;
    private int m_baseY;
    private int m_lastX;
    private int m_lastY;
    private int m_startX;
    private int m_startY;
    private boolean m_pressed = false;
    private PositionListener m_listener;

    public MovableView(Context context)
    {
        super(context);
    }

    public MovableView(Context context, AttributeSet attributeSet)
    {
        super(context, attributeSet);
    }

    public MovableView(Context context, AttributeSet attributeSet, int defStyleAttr)
    {
        super(context, attributeSet, defStyleAttr);
    }

    public interface PositionListener {
        public void OnPositionChanged(int x, int y, int relativeX, int relativeY, int lastDeltaX, int lastDeltaY, int startDeltaX, int startDeltaY);
    }

    public void SetPositionListener(PositionListener l)
    {
        m_listener = l;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        int x = (int)ev.getRawX();
        int y = (int)ev.getRawY();
        //Logf.e(ID_TAG, "%d %d|%d", x, y, ev.getAction());
        switch (ev.getAction())
        {
            case MotionEvent.ACTION_DOWN:
                if(!m_pressed)
                {
                    m_pressed = true;
                    m_lastX = m_startX = x;
                    m_lastY = m_startY = y;
                    m_baseX = (int)ev.getX();
                    m_baseY = (int)ev.getY();
                    return true;
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if(m_pressed)
                {
                    if(m_listener != null)
                    {
                        int lastDeltaX = x - m_lastX;
                        int lastDeltaY = y - m_lastY;
                        int startDeltaX = x - m_startX;
                        int startDeltaY = y - m_startY;
                        m_listener.OnPositionChanged(x, y, m_baseX, m_baseY, lastDeltaX, lastDeltaY, startDeltaX, startDeltaY);
                    }
                    m_lastX = x;
                    m_lastY = y;
                    return true;
                }
                break;
            case MotionEvent.ACTION_UP:
                if(m_pressed)
                {
                    Reset();
                    return true;
                }
                break;
            case MotionEvent.ACTION_CANCEL:
                Reset();
                return true;
            default:
                break;
        }
        return super.onTouchEvent(ev);
    }

    private void Reset()
    {
        m_lastX = m_startX = 0;
        m_lastY = m_startY = 0;
        m_baseX = m_baseY = 0;
        m_pressed = false;
    }
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        //super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        measureChildren(widthMeasureSpec, heightMeasureSpec);
        int wMode = MeasureSpec.getMode(widthMeasureSpec);
        int wSize = MeasureSpec.getSize(widthMeasureSpec);
        int hMode = MeasureSpec.getMode(heightMeasureSpec);
        int hSize = MeasureSpec.getSize(heightMeasureSpec);
        int mWidth = 0;
        int mHeight = 0;
        Size size = CaleChildrenSize();
        //Logf.e(ID_TAG, size);
        switch (wMode) {
            case MeasureSpec.EXACTLY:
                mWidth = wSize;
                break;
            case MeasureSpec.AT_MOST:
                mWidth = size.getWidth();
                break;
            case MeasureSpec.UNSPECIFIED:
                break;
        }
        switch (hMode) {
            case MeasureSpec.EXACTLY:
                mHeight = hSize;
                break;
            case MeasureSpec.AT_MOST:
                mHeight = size.getHeight();
                break;
            case MeasureSpec.UNSPECIFIED:
                break;
        }
        setMeasuredDimension(mWidth, mHeight);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            View childView = getChildAt(i);
            childView.layout(l, t, r, b);
        }
    }

    private Rect GetParentRect()
    {
        ViewGroup mViewGroup = (ViewGroup) getParent();
        if (mViewGroup != null)
        {
            int[] location = new int[2];
            mViewGroup.getLocationInWindow(location);
            //获取父布局的宽高
            int mRootMeasuredHeight = mViewGroup.getMeasuredHeight();
            int mRootMeasuredWidth = mViewGroup.getMeasuredWidth();
            //获取父布局顶点的坐标
            int mRootTopX = location[0];
            int mRootTopY = location[1];
            return new Rect(mRootTopX, mRootTopY, mRootTopX + mRootMeasuredWidth, mRootTopY + mRootMeasuredHeight);
        }
        return null;
    }

    private Size CaleChildrenSize()
    {
        int childCount = getChildCount();
        int maxWidth = 0;
        int maxHeight = 0;
        for (int i = 0; i < childCount; i++) {
            View childView = getChildAt(i);
            if(childView.getMeasuredWidth() > maxWidth)
                maxWidth = childView.getMeasuredWidth();
            if(childView.getMeasuredHeight() > maxHeight)
                maxHeight = childView.getMeasuredHeight();
        }
        return new Size(maxWidth, maxHeight);
    }
}