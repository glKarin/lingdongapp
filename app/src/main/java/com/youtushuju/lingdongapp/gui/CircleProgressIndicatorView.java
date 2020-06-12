package com.youtushuju.lingdongapp.gui;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Size;
import android.util.TypedValue;
import android.view.View;

import androidx.annotation.Nullable;

import com.youtushuju.lingdongapp.R;
import com.youtushuju.lingdongapp.common.Logf;

import java.util.Timer;
import java.util.TimerTask;

public class CircleProgressIndicatorView extends View
{
    private static final String ID_TAG = "CircleProgressIndicatorView";
    public static final int ENUM_STATE_READY = 0;
    public static final int ENUM_STATE_RUNNING = 1;
    public static final int ENUM_STATE_FINISHED = 2;

    public static final int ENUM_LABEL_FORMATTER_NONE = 0; // 不显示
    public static final int ENUM_LABEL_FORMATTER_RAW = 1; // 显示progress值
    public static final int ENUM_LABEL_FORMATTER_PERCENT = 2; // 百分比显示
    public static final int ENUM_LABEL_FORMATTER_SECOND = 3; // 秒

    public static final int ENUM_BACKGROUND_INSIDE_CIRCLE = 0; // 背景圆在环内部
    public static final int ENUM_BACKGROUND_OUTSIDE_CIRCLE = 1; // 背景圆在环外部

    private static final int CONST_DEFAULT_START_VALUE = 0;
    private static final int CONST_DEFAULT_END_VALUE = 100;
    private static final int CONST_DEFAULT_STEP = 1;

    // 背景颜色
    private int m_backgroundColor = Color.BLACK;
    // 边框颜色
    private int m_borderColor = Color.BLUE;
    private int m_borderStartColor = Color.BLUE;
    private int m_borderMiddleColor = Color.YELLOW;
    private int m_borderEndColor = Color.RED;
    // 边线宽度
    private int m_borderWidth = 8;
    // alpha
    private float m_alpha = 0.8f;
    // 字体大小
    private int m_labelSize = 24;
    private int m_labelColor = Color.BLACK;

    private Size m_size = null;
    private Path m_path = null;
    private Path m_closedPath = null;
    private Point m_labelPosition = null;
    private String m_label = "";
    private Paint m_borderPaint = null;
    private Paint m_labelPaint = null;
    private Paint m_backgroundPaint = null;
    private boolean m_drawBackground = false;
    private boolean m_drawBorder = true;
    private int m_labelFormatter = ENUM_LABEL_FORMATTER_RAW;
    // endValue always greater than minValue
    private int m_startValue = CONST_DEFAULT_START_VALUE;
    private int m_endValue = CONST_DEFAULT_END_VALUE;
    private int m_progress = CONST_DEFAULT_END_VALUE;
    private int m_step = CONST_DEFAULT_STEP;
    // only for auto-grow mode
    private int m_state = ENUM_STATE_READY;
    private Timer m_timer = null;
    private boolean m_autoGrow = true;
    private int m_timerInterval = 50;
    private long m_startTime = 0;
    private long m_endTime = 0;
    private ProgressListener m_progressListener = null;
    private int m_backgroundRadiusType = ENUM_BACKGROUND_INSIDE_CIRCLE;

    public interface ProgressListener
    {
        public void OnProgress(CircleProgressIndicatorView view);
        public void OnReady(CircleProgressIndicatorView view);
        public void OnStarted(CircleProgressIndicatorView view);
        public void OnFinished(CircleProgressIndicatorView view);
        public void OnStateChanged(CircleProgressIndicatorView view);
    }

    public CircleProgressIndicatorView(Context context)
    {
        super(context);
        Setup(null);
    }

    public CircleProgressIndicatorView(Context context, AttributeSet attributeSet)
    {
        super(context, attributeSet);
        Setup(attributeSet);
    }

    public CircleProgressIndicatorView(Context context, AttributeSet attributeSet, int defStyleAttr)
    {
        super(context, attributeSet, defStyleAttr);
        Setup(attributeSet);
    }

    public CircleProgressIndicatorView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        Setup(attrs);
    }

    private void SetBackgroundRadiusType(int type)
    {
        if(m_backgroundRadiusType != type)
        {
            m_backgroundRadiusType = type;
            Relayout();
        }
    }

    private void SetState(int state)
    {
        if(m_state != state)
        {
            m_state = state;
            if(m_progressListener != null)
                m_progressListener.OnStateChanged(this);
        }
    }

    public void SetLabelFormatter(int formatter)
    {
        if(m_labelFormatter != formatter)
        {
            m_labelFormatter = formatter;
            SetLabel(FormatLabel());
        }
    }

    private void SetLabel(String label)
    {
        if(m_label != label)
        {
            m_label = label;
            invalidate();
        }
    }

    public int State()
    {
        return m_state;
    }

    // 是否是增长的
    public boolean IsCW()
    {
        return m_step >= 0;
    }

    // 是否是减少的
    public boolean IsCCW()
    {
        return m_step < 0;
    }

    // 定时器是否运行
    public boolean IsRunning()
    {
        return m_state == ENUM_STATE_RUNNING;
    }

    // 进度百分比
    public float Percent()
    {
        float p = (float)m_progress / (float)(m_endValue - m_startValue);
        if(IsCW())
            return p;
        else
            return /*1.0f - */p;
    }

    public int Progress()
    {
        return m_progress;
    }

    public void SetProgress(int p)
    {
        int newP;
        if(p > m_endValue)
            newP = m_endValue;
        else if(p < m_startValue)
            newP = m_startValue;
        else
            newP = p;

        if(m_progress != newP)
        {
            m_progress = newP;
            m_label = FormatLabel();
            if(m_progressListener != null)
                m_progressListener.OnProgress(this);
            CheckFinished();
            Relayout();
        }
    }

    // 检查是否结束
    private boolean CheckFinished()
    {
        if((IsCW() && (IsOver() || IsFinish()))
                || (IsCCW() && (IsUnder() || IsReady()))
        )
        {
            StopAutoGrow();
            m_endTime = System.currentTimeMillis();
            SetState(ENUM_STATE_FINISHED);
            if(m_progressListener != null)
                m_progressListener.OnFinished(this);
            return true;
        }
        return false;
    }

    public void SetStep(int step)
    {
        // should not equals 0
        m_step = step;
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

    public void SetEndValue(int max)
    {
        if(m_endValue != max)
        {
            m_endValue = max;
            InvalidateProgress();
        }
    }

    public void SetStartValue(int min)
    {
        if(m_startValue != min)
        {
            m_startValue = min;
            InvalidateProgress();
        }
    }

    public void SetRange(int min, int max)
    {
        if(m_startValue == min && m_endValue == max)
            return;
        m_endValue = max;
        m_startValue = min;
        InvalidateProgress();
    }

    private void InvalidateProgress()
    {
        if(IsUnder())
            Ready();
        else if(IsOver())
            Finish();
        else
            Relayout();
    }

    // 转为起始状态
    public void Ready()
    {
        StopAutoGrow();
        if(IsCW())
            SetProgress(m_startValue);
        else
            SetProgress(m_endValue);
        //m_startTime = System.currentTimeMillis();
        if(m_progressListener != null)
            m_progressListener.OnReady(this);
    }

    // 转为结束状态
    public void Finish()
    {
        StopAutoGrow();
        if(IsCW())
            SetProgress(m_endValue);
        else
            SetProgress(m_startValue);
        m_endTime = System.currentTimeMillis();
        SetState(ENUM_STATE_FINISHED);
        if(m_progressListener != null)
            m_progressListener.OnFinished(this);
    }

    public void Reset()
    {
        StopAutoGrow();
        Ready();
        SetState(ENUM_STATE_READY);
        m_startTime = 0;
        m_endTime = 0;
    }

    public void Grow()
    {
        if(IsCW() && (IsOver() || IsFinish()))
            return;
        if(IsCCW() && (IsUnder() || IsReady()))
            return;

        int next = m_progress + m_step;
        SetProgress(next);
    }

    public boolean IsFinished()
    {
        return m_state == ENUM_STATE_FINISHED;
    }

    public boolean IsOver()
    {
        return m_progress > m_endValue;
    }

    public boolean IsUnder()
    {
        return m_progress < m_startValue;
    }

    public boolean IsFinish()
    {
        return m_progress == m_endValue;
    }

    public boolean IsReady()
    {
        return m_progress == m_startValue;
    }

    private void Relayout()
    {
        float tw = (float)getWidth();
        float th = (float)getHeight();
        float cw = m_size.getWidth() < 0 ? (tw / 100.0f) * (-m_size.getWidth()) : m_size.getWidth();
        float ch = m_size.getHeight() < 0 ? (th / 100.0f) * (-m_size.getHeight()) : m_size.getHeight();
        float w = Math.min(Math.min(tw, th), Math.min(cw, ch)) - m_borderWidth * 4;

        // 计算中间镂空的路径
        float radius = w / 2.0f;
        float x = tw / 2.0f;
        float y = th / 2.0f;
        float angle = CaleCurrentAngle();
        RectF rect = new RectF(x - radius, y - radius, x + radius, y + radius);

        m_path.reset();
        if(angle < 360)
        {
            m_path.arcTo(rect, 0, Math.min(angle, 359.0f));
            Matrix mat = new Matrix();
            mat.setRotate(-90, x, y);
            m_path.transform(mat);
        }
        else
        {
            m_path.addCircle(x, y, radius, Path.Direction.CW);
            m_path.close();
        }

        m_closedPath.reset();
        m_closedPath.addCircle(x, y, m_backgroundRadiusType == ENUM_BACKGROUND_OUTSIDE_CIRCLE ? radius + m_borderWidth / 2 : radius - m_borderWidth / 2, Path.Direction.CCW);
        m_closedPath.close();

        Rect textRect = new Rect();
        m_labelPaint.getTextBounds(m_label, 0, m_label.length(), textRect);
        m_labelPosition.set(Math.round(x), Math.round(y + textRect.height() / 2));
        /*Logf.e(ID_TAG, "中框: " + rect);
        Logf.e(ID_TAG, "标题坐标中心: " + m_labelPosition);*/

        invalidate();
    }

    private void Setup(AttributeSet attrs)
    {
        int circleRadius = -100;
        if(attrs != null)
        {
            TypedArray ta = getContext().obtainStyledAttributes(attrs, R.styleable.CircleProgressIndicatorView);
            m_borderWidth = (int)ta.getDimension(R.styleable.CircleProgressIndicatorView_borderWidth, m_borderWidth);
            m_borderColor = ta.getColor(R.styleable.CircleProgressIndicatorView_borderColor, m_borderColor);
            m_labelColor = ta.getColor(R.styleable.CircleProgressIndicatorView_labelColor, m_labelColor);
            m_labelSize = (int)ta.getDimension(R.styleable.CircleProgressIndicatorView_labelTextSize, m_labelSize);
            m_autoGrow = ta.getBoolean(R.styleable.CircleProgressIndicatorView_autoGrow, m_autoGrow);
            m_startValue = ta.getInt(R.styleable.CircleProgressIndicatorView_startValue, m_startValue);
            m_endValue = ta.getInt(R.styleable.CircleProgressIndicatorView_endValue, m_endValue);
            m_step = ta.getInt(R.styleable.CircleProgressIndicatorView_step, m_step);
            m_timerInterval = ta.getInt(R.styleable.CircleProgressIndicatorView_timerInterval, m_timerInterval);
            m_backgroundColor = ta.getColor(R.styleable.CircleProgressIndicatorView_backgroundColor, m_backgroundColor);
            m_labelFormatter = ta.getInt(R.styleable.CircleProgressIndicatorView_labelFormatter, m_labelFormatter);
            m_drawBackground = ta.getBoolean(R.styleable.CircleProgressIndicatorView_drawBackground, m_drawBackground);
            m_backgroundRadiusType = ta.getInt(R.styleable.CircleProgressIndicatorView_backgroundRadiusType, m_backgroundRadiusType);
            m_drawBorder = ta.getBoolean(R.styleable.CircleProgressIndicatorView_drawBorder, m_drawBorder);
            circleRadius = ta.getInt(R.styleable.CircleProgressIndicatorView_circleRadius, circleRadius);
        }

        if(IsCW())
            m_progress = m_startValue;
        else
            m_progress = m_endValue;

        m_path = new Path();
        m_closedPath = new Path();
        m_size = new Size(circleRadius, circleRadius);
        m_labelPosition = new Point(0, 0);
        m_borderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        m_backgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        m_labelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

        m_borderPaint.setColor(m_borderColor);
        m_borderPaint.setStrokeWidth(m_borderWidth);
        m_borderPaint.setStyle(Paint.Style.STROKE);
        m_borderPaint.setAntiAlias(true);

        m_backgroundPaint.setColor(m_backgroundColor);
        m_backgroundPaint.setStyle(Paint.Style.FILL);
        m_backgroundPaint.setAntiAlias(true);

        m_labelPaint.setColor(Color.WHITE);
        m_labelPaint.setTextAlign(Paint.Align.CENTER);
        m_labelPaint.setTextSize(m_labelSize);
        m_labelPaint.setAntiAlias(true);

        setAlpha(m_alpha);

        Relayout();

        if(m_autoGrow && m_timerInterval > 0)
            StartAutoGrow();
    }

    public void SetLabelColor(int color)
    {
        m_labelColor = color;
        m_labelPaint.setColor(m_labelColor);
        invalidate();
    }

    public void SetLabelTextSize(int size)
    {
        if(m_labelSize != size)
        {
            m_labelSize = size;
            m_labelPaint.setTextSize(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, m_labelSize, getContext().getResources().getDisplayMetrics()));
            invalidate();
        }
    }

    public void SetBackgroundColor(int color)
    {
        if(m_backgroundColor != color)
        {
            m_backgroundColor = color;
            m_backgroundPaint.setColor(m_backgroundColor);
            invalidate();
        }
    }

    public void SetBorderColor(int color)
    {
        if(m_borderColor != color)
        {
            m_borderColor = color;
            invalidate();
        }
    }

    public void SetBorderWidth(int width)
    {
        if(m_borderWidth != width)
        {
            m_borderWidth = width;
            Relayout();
        }
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

    public void SetDrawBorder(boolean on)
    {
        if(m_drawBorder != on)
        {
            m_drawBorder = on;
            invalidate();
        }
    }

    public void SetTimerInterval(int i)
    {
        if(m_timerInterval != i)
        {
            m_timerInterval = i;
            if(m_timerInterval > 0)
            {
                if(m_autoGrow)
                    StartAutoGrow();
            }
            else
                StopAutoGrow();
        }
    }

    private void StartAutoGrow()
    {
        StopAutoGrow();

        m_timer = new Timer();
        m_timer.scheduleAtFixedRate(new AutoGrowTask(), m_timerInterval, m_timerInterval);
        m_startTime = System.currentTimeMillis();
        Logf.e(ID_TAG, "开始自动变化");
    }

    private void StopAutoGrow()
    {
        if(m_timer != null)
        {
            Logf.e(ID_TAG, "结束自动变化");
            m_timer.purge();
            m_timer.cancel();
            m_timer = null;
        }
    }

    public void SetAutoGrow(boolean on)
    {
        if(m_autoGrow != on)
        {
            m_autoGrow = on;
            if(m_autoGrow)
                StartAutoGrow();
            else
                StopAutoGrow();
        }
    }

    public void SetProgressListener(ProgressListener l)
    {
        m_progressListener = l;
    }

    public int EndValue()
    {
        return m_endValue;
    }

    public int StartValue()
    {
        return m_startValue;
    }

    public long StartTime()
    {
        return m_startTime;
    }

    public long EndTime()
    {
        return m_endTime;
    }

    private String FormatLabel()
    {
        switch (m_labelFormatter)
        {
            case ENUM_LABEL_FORMATTER_SECOND:
                return "" + Math.round(m_progress / 1000);
            case ENUM_LABEL_FORMATTER_PERCENT:
                return Math.round(Percent() * 100) + "%";
            case ENUM_LABEL_FORMATTER_NONE:
                return "";
            case ENUM_LABEL_FORMATTER_RAW:
            default:
                return "" + m_progress;
        }
    }

    public void Shutdown()
    {
        Reset();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if(m_drawBackground)
            canvas.drawPath(m_closedPath, m_backgroundPaint);
        if(m_drawBorder)
            canvas.drawPath(m_path, m_borderPaint);
        if(m_labelFormatter != ENUM_LABEL_FORMATTER_NONE)
            canvas.drawText(m_label, m_labelPosition.x, m_labelPosition.y, m_labelPaint);
    }

    private class AutoGrowTask extends TimerTask
    {
        @Override
        public void run() {
            CircleProgressIndicatorView.this.post(new Runnable() {
                @Override
                public void run() {
                    Grow();
                }
            });
            if(m_state == ENUM_STATE_READY)
            {
                SetState(ENUM_STATE_RUNNING);
                if(m_progressListener != null)
                    m_progressListener.OnStarted(CircleProgressIndicatorView.this);
            }
        }
    }
}