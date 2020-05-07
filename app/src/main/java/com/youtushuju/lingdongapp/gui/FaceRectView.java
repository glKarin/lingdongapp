package com.youtushuju.lingdongapp.gui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

import androidx.annotation.Nullable;

import com.youtushuju.lingdongapp.common.Logf;

import java.util.List;

public class FaceRectView extends View
{
    private static final String ID_TAG = "FaceRectView";
    private String mCorlor = "#42ed45";
    private Paint mPaint = null;
    private List<RectF> m_rects = null;

    public FaceRectView(Context context)
    {
        super(context);
        Setup();
    }

    public FaceRectView(Context context, AttributeSet attributeSet)
    {
        super(context, attributeSet);
        Setup();
    }

    public FaceRectView(Context context, AttributeSet attributeSet, int defStyleAttr)
    {
        super(context, attributeSet, defStyleAttr);
        Setup();
    }

    public FaceRectView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        Setup();
    }

    private void Setup()
    {
        mPaint = new Paint();
        mPaint.setColor(Color.GREEN);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1f, getContext().getResources().getDisplayMetrics()));
        mPaint.setAntiAlias(true);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if(m_rects != null)
        {
            for(RectF r : m_rects)
            {
                canvas.drawRect(r, mPaint);
            }
        }
    }

    public void SetFaces(List<RectF> l)
    {
        m_rects = l;
        invalidate();
    }
}