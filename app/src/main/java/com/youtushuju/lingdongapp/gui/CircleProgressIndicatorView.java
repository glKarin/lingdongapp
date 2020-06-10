package com.youtushuju.lingdongapp.gui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Size;
import android.util.TypedValue;
import android.view.View;

import androidx.annotation.Nullable;

import com.youtushuju.lingdongapp.R;
import com.youtushuju.lingdongapp.common.Logf;

public class CircleProgressIndicatorView extends View
{
    private static final String ID_TAG = "CircleProgressIndicatorView";

    // 背景颜色
    private int m_backgroundColor = Color.BLACK;
    // 边框颜色
    private int m_borderReadyColor = Color.BLUE;
    private int m_borderProcessingColor = Color.BLUE;
    private int m_borderScanningColor = Color.YELLOW;
    private int m_borderUploadingColor = Color.MAGENTA;
    private int m_borderSuccessColor = Color.GREEN;
    private int m_borderErrorColor = Color.RED;
    // 边线宽度
    private int m_borderWidth = 8;
    // alpha
    private float m_alpha = 0.8f;
    // 字体大小
    private int m_labelSize = 20;

    private Size m_size = null;
    private Path m_path = null;
    private Point m_labelPosition = null;
    private String m_label = "100%";
    private Paint m_borderPaint = null;
    private Paint m_labelPaint = null;
    private boolean m_drawBackground = false;
    private int m_minValue = 0;
    private int m_maxValue = 100;
    private int m_progress = 35;

    public CircleProgressIndicatorView(Context context)
    {
        super(context);

        Setup();
    }

    public CircleProgressIndicatorView(Context context, AttributeSet attributeSet)
    {
        super(context, attributeSet);
        Setup();
    }

    public CircleProgressIndicatorView(Context context, AttributeSet attributeSet, int defStyleAttr)
    {
        super(context, attributeSet, defStyleAttr);
        Setup();
    }

    public CircleProgressIndicatorView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        Setup();
    }

    public float Percent()
    {
        // max must always greater than min
        return (float)m_progress / (float)(m_maxValue - m_minValue);
    }

    public int Progress()
    {
        return m_progress;
    }

    public void SetProgress(int p)
    {
        if(m_progress != p)
        {
            m_progress = p;
            invalidate();
        }
    }

    public void SetSize(float width, float height)
    {
        m_size = new Size((int)-Math.floor(Math.min(width * 100.0f, 100.f)), (int)-Math.floor(Math.min(height * 100.0f, 100.f)));
        Relayout();
    }

    public void SetSize(int width, int height)
    {
        m_size = new Size(Math.min(width, getWidth()), Math.min(height, getHeight()));
        Relayout();
    }

    private void Relayout()
    {
        float tw = (float)getWidth();
        float th = (float)getHeight();
        float cw = m_size.getWidth() < 0 ? (tw / 100.0f) * (-m_size.getWidth()) : m_size.getWidth();
        float ch = m_size.getHeight() < 0 ? (th / 100.0f) * (-m_size.getHeight()) : m_size.getHeight();
        float w = Math.min(Math.min(tw, th), Math.min(cw, ch)) - m_borderWidth * 2;

        // 计算中间镂空的路径
        float radius = w / 2.0f;
        float x = tw / 2.0f;
        float y = th / 2.0f;
        float angle = CaleCurrentAngle();
        RectF rect = new RectF(x - radius, y - radius, x + radius, y + radius);
        m_path.reset();
        m_path.arcTo(rect, 0, angle);
        if(angle >= 360.0)
            m_path.close();

        m_labelPosition.set(Math.round(x), Math.round(y));
        Logf.e(ID_TAG, "中框: " + rect);
        Logf.e(ID_TAG, "标题坐标中心: " + m_labelPosition);

        invalidate();
    }

    private void Setup()
    {
        m_path = new Path();
        m_size = new Size(-100, -100);
        m_labelPosition = new Point(0, 0);
        m_borderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        m_labelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

        m_borderPaint.setColor(m_borderReadyColor);
        m_borderPaint.setStrokeWidth(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, m_borderWidth, getContext().getResources().getDisplayMetrics()));
        m_borderPaint.setStyle(Paint.Style.STROKE);
        m_borderPaint.setAntiAlias(true);

        m_labelPaint.setColor(Color.WHITE);
        m_labelPaint.setTextAlign(Paint.Align.CENTER);
        m_labelPaint.setTextSize(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 24, getContext().getResources().getDisplayMetrics()));
        m_labelPaint.setAntiAlias(true);

        setAlpha(m_alpha);

        Relayout();
    }

    private float CaleCurrentAngle()
    {
        return Percent() * 360.0f;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        Relayout();
    }

    public void SetDrawBackground(boolean on)
    {
        if(m_drawBackground != on)
        {
            m_drawBackground = on;
            invalidate();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.drawPath(m_path, m_borderPaint);
        if(m_drawBackground)
        {
            canvas.drawColor(m_backgroundColor);
        }
        canvas.drawText(m_label, m_labelPosition.x, m_labelPosition.y, m_labelPaint);
    }
}