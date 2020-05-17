package com.youtushuju.lingdongapp.gui;
import android.widget.*;
import android.content.*;
import android.view.*;
import java.util.*;
import android.util.*;

public class PagedListView extends ListView
{
	public static final int PAGED_LIST_VIEW_MODE_REFRESH = 1;
	public static final int PAGED_LIST_VIEW_MODE_MORE = 2;
	private static final int TOUCH_ACTION_NONE = 0;
	private static final int TOUCH_ACTION_REFRESH = 1;
	private static final int TOUCH_ACTION_REFRESH_CANCEL = 2;
	private static final int TOUCH_ACTION_MORE = 3;
	private static final int REFRESH_DOWN_LIMIT = 320;
	
	private int m_touchAction = TOUCH_ACTION_NONE;
	private int m_lastTouchY = 0;
	private int m_lastY = 0;
	private int m_lastTouchId = -1;
	private DataProvider m_dataProvider = null;
	private HeaderView m_header = null;
	private FooterView m_footer = null;
	private int m_loadMode = PAGED_LIST_VIEW_MODE_REFRESH;
	
	public PagedListView(Context context)
	{
		this(context, null);
	}
	
	public PagedListView(Context context, AttributeSet attr)
	{
		super(context, attr);
		
		setOverScrollMode(ListView.OVER_SCROLL_ALWAYS);

		m_header = new HeaderView(this);
		addHeaderView(m_header.m_view);
		m_footer = new FooterView(this);
		addFooterView(m_footer.m_view);

		setOnScrollListener(new OnScrollListener() {
			private int m_last = 0;
			public void onScrollStateChanged(AbsListView list, int state)
			{
				if(state == SCROLL_STATE_IDLE && m_touchAction == TOUCH_ACTION_MORE && getLastVisiblePosition() == m_last)
				{
					m_touchAction = TOUCH_ACTION_NONE;
					SetLoadMode(PAGED_LIST_VIEW_MODE_MORE);
					if(m_dataProvider != null)
						m_dataProvider.MoreData();
				}
				else if(state == SCROLL_STATE_IDLE && m_touchAction == TOUCH_ACTION_REFRESH && getFirstVisiblePosition() == 0)
				{
					m_touchAction = TOUCH_ACTION_NONE;
					SetLoadMode(PAGED_LIST_VIEW_MODE_REFRESH);
					if(m_dataProvider != null)
						m_dataProvider.RefreshData();
				}
			}
			
			public void onScroll(AbsListView list, int first, int total, int last)
			{
				m_last = last - 1;
			}
		});
	}

	@Override
	public boolean onTouchEvent(MotionEvent ev)
	{
		int id = ev.getPointerId(ev.getActionIndex());
		switch(ev.getAction())
		{
			case MotionEvent.ACTION_DOWN:
				if(m_lastTouchId == -1)
				{
					m_lastTouchId = id;
					m_lastY = m_lastTouchY = (int)ev.getY();

					m_touchAction = TOUCH_ACTION_NONE;
				}
				break;
			case MotionEvent.ACTION_UP:
				if(m_lastTouchId == id)
				{
					m_lastTouchY = 0;
					m_lastY = 0;
					m_lastTouchId = -1;
					m_header.SetState(HeaderView.HEADER_VIEW_STATE_IDLE, 0);
					
					//Toast.makeText(getContext(), "" + m_touchAction, Toast.LENGTH_SHORT).show();
				}
				break;
			case MotionEvent.ACTION_MOVE:
				if(m_lastTouchId == id)
				{
					int y = (int)ev.getY();
					int delta = y - m_lastTouchY;
					int lastDelta = y - m_lastY;
					boolean isFirst = getFirstVisiblePosition() == 0;
					boolean isLast = getLastVisiblePosition() == getCount() - 1;
					if(lastDelta > 0)
					{
						if(isFirst)
						{
							if(delta > 0)
								m_header.SetState(HeaderView.HEADER_VIEW_STATE_DOWN, delta);
							if(delta >= REFRESH_DOWN_LIMIT)
							{
								m_touchAction = TOUCH_ACTION_REFRESH;
								m_header.SetState(HeaderView.HEADER_VIEW_STATE_UP, delta);
							}
						}
					}
					else if(lastDelta < 0)
					{
						//Logf.e(null,  "%d %d, %d/%d", isFirst?1:0, isLast?1:0, lastDelta, delta);
						if(isFirst)
						{
							if(delta >= REFRESH_DOWN_LIMIT)
							{
								// m_touchAction = TOUCH_ACTION_REFRESH;
								m_header.SetState(HeaderView.HEADER_VIEW_STATE_UP, delta);
							}
							else if(delta > 0)
							{
								m_header.SetState(HeaderView.HEADER_VIEW_STATE_CANCEL, delta);
								if(m_touchAction == TOUCH_ACTION_REFRESH)
								{
									m_touchAction = TOUCH_ACTION_REFRESH_CANCEL;
								}
							}
						}
						if(isLast)
						{
							if(delta < 0)
							{
								m_header.SetState(HeaderView.HEADER_VIEW_STATE_IDLE, 0);
								m_touchAction = TOUCH_ACTION_MORE;
							}
						}
					}
					m_lastY = y;
				}
				break;
			default:
				break;
		}
		return super.onTouchEvent(ev);
	}
	
	public void SetDataProvider(DataProvider p)
	{
		m_dataProvider = p;
	}
	
	public int LoadMode()
	{
		return m_loadMode;
	}
	
	protected void SetLoadMode(int mode)
	{
		if(m_loadMode != mode)
			m_loadMode = mode;
	}
	
	public void BeginMoreLoading()
	{
		m_footer.SetState(FooterView.FOOTER_VIEW_STATE_LOADING);
	}

	public void EndMoreLoading(boolean success)
	{
		m_footer.SetState(success ? FooterView.FOOTER_VIEW_STATE_IDLE : FooterView.FOOTER_VIEW_STATE_FAIL);
	}
	
	public interface DataProvider
	{
		public void RefreshData();
		public void MoreData();
	}
	
	private class FooterView
	{
		public static final int FOOTER_VIEW_STATE_IDLE = 0;
		public static final int FOOTER_VIEW_STATE_LOADING = 1;
		public static final int FOOTER_VIEW_STATE_FAIL = 2;
		
		private View m_view = null;
		private TextView m_label = null;
		private ProgressBar m_progressBar = null;
		private int m_state = FOOTER_VIEW_STATE_IDLE;
		private int m_height = 0;
		
		public FooterView(ViewGroup parent)
		{
			Context context = parent.getContext();
			LinearLayout layout = new LinearLayout(context);
			layout.setOrientation(LinearLayout.HORIZONTAL);
			layout.setGravity(Gravity.CENTER);
			layout.setLayoutParams(new AbsListView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

			ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
			m_progressBar = new ProgressBar(context);
			m_progressBar.setIndeterminate(true);
			layout.addView(m_progressBar, params);
			
			m_label = new TextView(context);
			m_label.setText("加载中");
			m_label.setTextSize(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 6, context.getResources().getDisplayMetrics()));
			layout.addView(m_label, params);

			m_view = layout;
			m_view.measure(0, 0);
			m_height = m_view.getMeasuredHeight();
			
			SetState(FOOTER_VIEW_STATE_IDLE);
		}
		
		public void SetState(int state)
		{
			m_state = state;
			switch(m_state)
			{
				case FOOTER_VIEW_STATE_LOADING:
					m_progressBar.setVisibility(View.VISIBLE);
					m_label.setText("加载中");
					m_view.setVisibility(View.VISIBLE);
					m_view.setPadding(0, 0, 0, 0);
					break;
				case FOOTER_VIEW_STATE_FAIL:
					m_progressBar.setVisibility(View.GONE);
					m_label.setText("加载错误");
					m_view.setVisibility(View.GONE);
					m_view.setPadding(0, -m_height, 0, 0);
					break;
				case FOOTER_VIEW_STATE_IDLE:
				default:
					m_progressBar.setVisibility(View.GONE);
					m_label.setText("加载完成");
					m_view.setVisibility(View.GONE);
					m_view.setPadding(0, -m_height, 0, 0);
					break;
			}
		}
	}

	private class HeaderView
	{
		public static final int HEADER_VIEW_STATE_IDLE = 0;
		public static final int HEADER_VIEW_STATE_DOWN = 1;
		public static final int HEADER_VIEW_STATE_UP = 2;
		public static final int HEADER_VIEW_STATE_CANCEL = 3;
		
		private View m_view = null;
		private TextView m_label = null;
		private int m_height = 0;
		private int m_state = HEADER_VIEW_STATE_IDLE;

		public HeaderView(ViewGroup parent)
		{
			Context context = parent.getContext();

			LinearLayout layout = new LinearLayout(context);
			layout.setOrientation(LinearLayout.VERTICAL);
			layout.setGravity(Gravity.CENTER);
			layout.setLayoutParams(new AbsListView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

			ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
			m_label = new TextView(context);
			m_label.setTextSize(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, context.getResources().getDisplayMetrics()));
			layout.addView(m_label, params);

			m_view = layout;

			m_view.measure(0, 0);
			m_height = m_view.getMeasuredHeight();

			SetState(HEADER_VIEW_STATE_IDLE, 0);
		}

		public void SetState(int state, int delta)
		{
			m_state = state;
			switch(m_state)
			{
				case HEADER_VIEW_STATE_DOWN:
					m_label.setText("下拉刷新...");
					m_view.setVisibility(View.VISIBLE);
					m_view.setPadding(0, (delta - m_height) / 2, 0, (delta - m_height) / 2);
					break;
				case HEADER_VIEW_STATE_UP:
					m_label.setText("松开刷新...");
					m_view.setVisibility(View.VISIBLE);
					m_view.setPadding(0, (delta - m_height) / 2, 0, (delta - m_height) / 2);
					break;
				case HEADER_VIEW_STATE_CANCEL:
					m_label.setText("松开取消...");
					m_view.setVisibility(View.VISIBLE);
					m_view.setPadding(0, (delta - m_height) / 2, 0, (delta - m_height) / 2);
					break;
				case HEADER_VIEW_STATE_IDLE:
				default:
					m_label.setText("下拉刷新...");
					m_view.setVisibility(View.GONE);
					m_view.setPadding(0, -m_height / 2, 0, -m_height / 2);
					break;
			}
		}
	}
}
