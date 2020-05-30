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
import android.util.Log;
import android.util.Size;
import android.util.SizeF;
import android.util.TypedValue;
import android.view.View;

import androidx.annotation.Nullable;

import com.youtushuju.lingdongapp.R;
import com.youtushuju.lingdongapp.common.Logf;

import java.util.List;

public class CameraMaskView extends View
{
    private static final String ID_TAG = "CameraMashView";
    public static final int ID_STATE_READY = 0;
    public static final int ID_STATE_SCANNING = 1;
    public static final int ID_STATE_FACE_VERIFY_SUCCESS = 2;
    public static final int ID_STATE_FACE_VERIFY_FAIL = 3;
    public static final int ID_STATE_PROCESSING = 4;
    public static final int ID_STATE_PROCESS_SUCCESS = 5;
    public static final int ID_STATE_PROCESS_FAIL = 6;
    public static final int ID_STATE_UPLOADING = 7;
    public static final int ID_STATE_UPLOAD_SUCCESS = 8;
    public static final int ID_STATE_UPLOAD_FAIL = 9;
    private static final String ID_STATE_NAMES[] = {
            "",
            "扫描人脸中",
            "人脸已识别",
            "人脸无法识别",
            "开门中",
            "投递成功",
            "投递失败",
            "上报重量中",
            "上报成功",
            "上报失败",
    };

    // 背景颜色
    private int m_backgroundColor = Color.BLACK;
    // 边框颜色
    private int m_borderReadyColor = Color.WHITE;
    private int m_borderProcessingColor = Color.BLUE;
    private int m_borderScanningColor = Color.YELLOW;
    private int m_borderUploadingColor = Color.MAGENTA;
    private int m_borderSuccessColor = Color.GREEN;
    private int m_borderErrorColor = Color.RED;
    // 边线宽度
    private int m_borderNormalWidth = 6;
    private int m_borderProcessingWidth = 8;
    // 边角
    private int m_radius = 20;
    // alpha
    private float m_alpha = 0.8f;
    // 字体大小
    private int m_labelSize = 20;

    private Size m_size = null;
    private Path m_path = null;
    private Point m_labelPosition = null;
    private Point m_statePosition = null;
    private int m_state = ID_STATE_READY;
    private String m_label = "";
    private Paint m_borderPaint = null;
    private Paint m_centerPaint = null;
    private Paint m_labelPaint = null;
    private Paint m_statePaint = null;
    private boolean m_drawBox = false;
    private int m_bottomPadding = 0; // px

    public CameraMaskView(Context context)
    {
        super(context);

        Setup();
    }

    public CameraMaskView(Context context, AttributeSet attributeSet)
    {
        super(context, attributeSet);
        Setup();
    }

    public CameraMaskView(Context context, AttributeSet attributeSet, int defStyleAttr)
    {
        super(context, attributeSet, defStyleAttr);
        Setup();
    }

    public CameraMaskView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        Setup();
    }

    public int State()
    {
        return m_state;
    }

    public void SetState(int state)
    {
        if(m_state != state)
        {
            m_state = state;
            int borderColor = m_borderReadyColor;
            int borderWidth = m_borderNormalWidth;

            switch(m_state)
            {
                case ID_STATE_SCANNING:
                    borderColor = m_borderScanningColor;
                    borderWidth = m_borderProcessingWidth;
                    break;
                case ID_STATE_PROCESSING:
                    borderColor = m_borderProcessingColor;
                    borderWidth = m_borderProcessingWidth;
                    break;
                case ID_STATE_UPLOADING:
                    borderColor = m_borderUploadingColor;
                    borderWidth = m_borderProcessingWidth;
                    break;
                case ID_STATE_FACE_VERIFY_SUCCESS:
                case ID_STATE_PROCESS_SUCCESS:
                case ID_STATE_UPLOAD_SUCCESS:
                    borderColor = m_borderSuccessColor;
                    break;
                case ID_STATE_FACE_VERIFY_FAIL:
                case ID_STATE_PROCESS_FAIL:
                case ID_STATE_UPLOAD_FAIL:
                    borderColor = m_borderErrorColor;
                    break;
                case ID_STATE_READY:
                default:
                    break;
            }
            m_borderPaint.setStrokeWidth(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, borderWidth, getContext().getResources().getDisplayMetrics()));
            m_borderPaint.setColor(borderColor);
            m_statePaint.setColor(borderColor);
            m_label = ID_STATE_NAMES[m_state];
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
        m_size = new Size(Math.min(width, getWidth()), Math.min(height, getHeight() - m_bottomPadding));
        Relayout();
    }

    private void Relayout()
    {
        float tw = (float)getWidth();
        float th = (float)getHeight() - m_bottomPadding;
        float cw = m_size.getWidth() < 0 ? (tw / 100.0f) * (-m_size.getWidth()) : m_size.getWidth();
        float ch = m_size.getHeight() < 0 ? (th / 100.0f) * (-m_size.getHeight()) : m_size.getHeight();

        // 计算中间镂空的路径
        float radius = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, m_radius, getContext().getResources().getDisplayMetrics());
        float x = tw / 2.0f - cw / 2.0f;
        float y = th / 2.0f - ch / 2.0f;
        RectF rect = new RectF(x, y, x + cw, y + ch);
        m_path.reset();
        m_path.addRoundRect(rect, radius, radius, Path.Direction.CW);
        m_path.close();

        m_labelPosition.set(Math.round(tw / 2.0f), Math.round(y / 2.0f));
        m_statePosition.set(Math.round(tw / 2.0f), Math.round((th - (y + ch)) / 2.0f + (y + ch)));
        Logf.e(ID_TAG, "中框: " + rect);
        Logf.e(ID_TAG, "标题坐标中心: " + m_labelPosition);
        Logf.e(ID_TAG, "状态坐标中心: " + m_statePosition);

        invalidate();
    }

    private void Setup()
    {
        setLayerType(View.LAYER_TYPE_SOFTWARE, null); // setXfermode不支持GPU渲染, 关闭硬件加速
        m_bottomPadding = (int)getContext().getResources().getDimension(R.dimen.person_panel_height); // 150dp

        m_path = new Path();
        m_size = new Size(-80, -75);
        m_labelPosition = new Point(0, 0);
        m_statePosition = new Point(0, 0);
        m_borderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        m_centerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        m_labelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        m_statePaint = new Paint(Paint.ANTI_ALIAS_FLAG);

        m_borderPaint.setColor(m_borderReadyColor);
        m_borderPaint.setStrokeWidth(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, m_borderNormalWidth, getContext().getResources().getDisplayMetrics()));
        m_borderPaint.setStyle(Paint.Style.STROKE);
        m_borderPaint.setAntiAlias(true);

        m_centerPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        m_centerPaint.setAntiAlias(true);
        m_centerPaint.setStyle(Paint.Style.FILL);

        m_labelPaint.setColor(Color.WHITE);
        m_labelPaint.setTextAlign(Paint.Align.CENTER);
        m_labelPaint.setTextSize(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 24, getContext().getResources().getDisplayMetrics()));
        m_labelPaint.setAntiAlias(true);

        m_statePaint.setColor(m_borderReadyColor);
        m_statePaint.setTextAlign(Paint.Align.CENTER);
        m_statePaint.setTextSize(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, m_labelSize, getContext().getResources().getDisplayMetrics()));
        m_statePaint.setAntiAlias(true);

        setAlpha(m_alpha);

        Relayout();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        Relayout();
    }

    public void SetDrawBox(boolean on)
    {
        if(m_drawBox != on)
        {
            m_drawBox = on;
            invalidate();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if(m_drawBox)
        {
            canvas.drawColor(m_backgroundColor);
            canvas.drawPath(m_path, m_borderPaint);
            canvas.drawPath(m_path, m_centerPaint);
        }
        canvas.drawText("自动扫脸垃圾箱", m_labelPosition.x, m_labelPosition.y, m_labelPaint);
        canvas.drawText(m_label, m_statePosition.x, m_statePosition.y, m_statePaint);
    }
}