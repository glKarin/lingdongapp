package com.youtushuju.lingdongapp.gui;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.AttrRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StyleRes;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.youtushuju.lingdongapp.R;

import java.util.ArrayList;
import java.util.List;

public class ScrollImage extends FrameLayout {
    private static final String ID_TAG = "ScrollImage";
    private ViewPager m_viewPager;
    private ImagePagerAdapter m_adapter;
    private int m_interval = 3000;
    private boolean m_autoStart = true;
    private boolean m_running = false;
    private Handler m_handler = new Handler(Looper.getMainLooper());
    private Runnable m_runnable = new Runnable() {
        @Override
        public void run() {
            Flip(1);
            m_handler.postDelayed(m_runnable, m_interval);
        }
    };

    public ScrollImage(@NonNull Context context) {
        super(context);

        Setup();
    }

    public ScrollImage(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);

        Setup();
    }

    public ScrollImage(@NonNull Context context, @Nullable AttributeSet attrs,
                       @AttrRes int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);

        Setup();
    }

    public ScrollImage(@NonNull Context context, @Nullable AttributeSet attrs,
                       @AttrRes int defStyleAttr, @StyleRes int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);

        Setup();
    }

    private void Setup()
    {
        ImagePageModel item;

        m_viewPager = new ViewPager(getContext());
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        addView(m_viewPager, params);
        m_adapter = new ImagePagerAdapter(new ArrayList<ImagePageModel>());
        m_viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            private int m_currentIndex = -1;
            @Override
            public void onPageScrolled(int position, float positionOffset,int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                if (position == 0)
                {
                    m_currentIndex = m_adapter.getCount() - 2;
                }
                else if (position == m_adapter.getCount() - 1)
                {
                    m_currentIndex = 1;
                }
                else
                {
                    m_currentIndex = -1;
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                if(state == ViewPager.SCROLL_STATE_IDLE)
                {
                    if(m_currentIndex >= 0)
                    {
                        m_viewPager.setCurrentItem(m_currentIndex,false);
                        m_currentIndex = -1;
                    }
                    //play();
                }
                else if(state == ViewPager.SCROLL_STATE_DRAGGING)
                {
                    //cancel();
                }
            }
        });

        item = new ImagePageModel(R.drawable.three, "");
        m_adapter.m_list.add(item);

        item = new ImagePageModel(R.drawable.one, "");
        m_adapter.m_list.add(item);
        item = new ImagePageModel(R.drawable.two, "");
        m_adapter.m_list.add(item);
        item = new ImagePageModel(R.drawable.three, "");
        m_adapter.m_list.add(item);

        item = new ImagePageModel(R.drawable.one, "");
        m_adapter.m_list.add(item);

        m_viewPager.setOffscreenPageLimit(m_adapter.m_list.size());
        m_viewPager.setAdapter(m_adapter);

        if(m_autoStart)
            Start(0);
    }

    public void Start(int interval)
    {
        m_handler.removeCallbacks(m_runnable);
        m_handler.postDelayed(m_runnable, interval);
        m_running = true;
    }

    public void Pause()
    {
        m_handler.removeCallbacks(m_runnable);
        m_running = false;
    }

    public boolean Running()
    {
        return m_running;
    }

    public ScrollImage SetAutoStart(boolean on)
    {
        m_autoStart = on;
        if(!m_running)
            Start(0);
        return this;
    }

    public ScrollImage SetInterval(int i)
    {
        if(m_interval != i)
        {
            m_interval = i;
            m_handler.removeCallbacks(m_runnable);
            m_handler.postDelayed(m_runnable, m_interval);
        }
        return this;
    }

    public void Flip(int i)
    {
        if(i == 0)
            return;
        if(m_adapter.m_list.isEmpty())
            return;
        int index = m_viewPager.getCurrentItem();
        int next = (index + i) % m_adapter.m_list.size();
        m_viewPager.setCurrentItem(next, true);
    }

    private static class ImagePageModel
    {
        public int resource_id;
        public String label;
        public View view;

        public ImagePageModel()
        {
        }

        public ImagePageModel(int rid, String l)
        {
            resource_id = rid;
            label = l;
        }
    }

    private class ImagePagerAdapter extends PagerAdapter
    {
        private List<ImagePageModel> m_list;

        public ImagePagerAdapter(List<ImagePageModel> l)
        {
            m_list = l;
        }

        @Override
        public int getCount() {
            return m_list.size();
        }

        @Override
        public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
            return view == object;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            LayoutInflater li;
            ImagePageModel item;

            li = LayoutInflater.from(container.getContext()); //getLayoutInflater();
            item = m_list.get(position);
            if(item.view == null)
            {
                View view = li.inflate(R.layout.carousel, null, false);
                item.view = view;
            }
            ImageView imageView = (ImageView)item.view.findViewById(R.id.carousel_image);
            imageView.setImageResource(item.resource_id);
            //item.view.setBackgroundResource(item.resource_id);
            TextView textView = (TextView)item.view.findViewById(R.id.carousel_label);
            textView.setText(item.label);
            container.addView(item.view);
            return item.view;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView(m_list.get(position).view);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return m_list.get(position).label;
        }
    }
}
