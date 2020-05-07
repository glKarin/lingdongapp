package com.youtushuju.lingdongapp.gui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.TextureView;

public class DynamicTextureView extends TextureView
{
    public static final int ID_FILL_SCHEME_NONE = 0;
    public static final int ID_FILL_SCHEME_WIDTH_PREFER = 1;
    public static final int ID_FILL_SCHEME_HEIGHT_PREFER = 2;

    private int m_ratioWidth = 0;
    private int m_ratioHeight = 0;
    private int m_fillScheme = ID_FILL_SCHEME_NONE;

    public DynamicTextureView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
    }

    public DynamicTextureView(Context context) {
        super(context);
    }

    public DynamicTextureView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public DynamicTextureView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public void SetAspectRatio(int width, int height)
    {
        if(width < 0 || height < 0)
            throw new IllegalArgumentException(String.format("width(%d) / height(%d) cannot be negative.", width, height)); // return

        m_ratioWidth = width;
        m_ratioHeight = height;
        requestLayout();
    }

    public void SetFileScheme(int s)
    {
        if(m_fillScheme != s)
        {
            m_fillScheme = s;
            requestLayout();
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
    {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        if (m_fillScheme == ID_FILL_SCHEME_NONE || (m_ratioWidth == 0 || m_ratioHeight == 0))
        {
            setMeasuredDimension(width, height);
        }
        else
        {
            if(m_fillScheme == ID_FILL_SCHEME_HEIGHT_PREFER)
            {
                if (width < height * m_ratioWidth / m_ratioHeight)
                {
                    setMeasuredDimension(width, width * m_ratioHeight / m_ratioWidth);
                }
                else
                {
                    setMeasuredDimension(height * m_ratioWidth / m_ratioHeight, height);
                }
            }
            else
            {
                if (width < height * m_ratioWidth / m_ratioHeight)
                {
                    setMeasuredDimension(height * m_ratioWidth / m_ratioHeight, height);
                }
                else
                {
                    setMeasuredDimension(width, width * m_ratioHeight / m_ratioWidth);
                }
            }
        }
    }
}
