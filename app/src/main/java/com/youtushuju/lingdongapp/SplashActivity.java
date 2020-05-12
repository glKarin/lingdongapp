package com.youtushuju.lingdongapp;
import androidx.appcompat.app.AppCompatActivity;
import android.os.*;
import android.content.pm.*;
import android.widget.*;
import android.content.*;
import android.view.*;

import com.youtushuju.lingdongapp.common.Logf;
import com.youtushuju.lingdongapp.device.LingDongApi;
import com.youtushuju.lingdongapp.common.Configs;
import com.youtushuju.lingdongapp.common.Constants;
import com.youtushuju.lingdongapp.gui.App;

import java.io.IOException;
import java.util.*;

public class SplashActivity extends AppCompatActivity
{
	private static final String ID_TAG = "SplashActivity";
	private static final int NAV_TO_MAIN_PAGE = 0;
	private static final int UPDATE_TIMER = 1;
	private static final int TIMER_INTERVAL = 500;
	private static final int SPLASH_DELAY = 3500;
	
	private boolean m_navLocked = false;
	private Timer m_timer = null;
	private int m_timerDelay = SPLASH_DELAY;
	
	private Handler m_handler = new Handler(){
		@Override
		public void handleMessage(Message msg)
		{
			switch(msg.what)
			{
				case UPDATE_TIMER:
				{
					if(m_timerDelay > 0)
					{
						m_timerDelay -= TIMER_INTERVAL;
						if(m_timerDelay < 0)
						{
							m_timerDelay = 0;
						}
						TextView delayView = (TextView)findViewById(R.id.splash_delay);
						if(m_timerDelay > 0)
							delayView.setText(String.format("%ds", Math.round((double)m_timerDelay / 1000.0)));
						else
						{
							ToMainPage();
						}
					}
				}
					break;
				default:
					break;
			}
		}
		
	};
	private Runnable m_callback = new Runnable(){
		public void run()
		{
			ToMainPage();
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		
		setContentView(R.layout.splash_page);

		SetupUI();

		App.Instance().PushActivity(this);

		Test();
	}

	private void SetupUI()
	{
		PackageManager pm = getPackageManager();
		TextView appVersionView = (TextView)findViewById(R.id.splash_app_version);
		try
		{
			PackageInfo info = pm.getPackageInfo(getPackageName(), 0);
			appVersionView.setText("Ver " + info.versionName);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}

		findViewById(R.id.splash_indicator).setOnClickListener(new View.OnClickListener(){
			public void onClick(View view)
			{
				if(m_navLocked)
					return;
				m_handler.removeCallbacks(m_callback);
				ToMainPage();
			}
		});

		findViewById(R.id.splash_emulate_api).setOnClickListener(m_buttonClickListener);
		findViewById(R.id.splash_real_api).setOnClickListener(m_buttonClickListener);

		if(m_timerDelay > 0)
		{
			m_timer = new Timer();
			m_timer.scheduleAtFixedRate(new TimerTask(){
				public void run()
				{
					m_handler.sendEmptyMessage(UPDATE_TIMER);
				}
			}, TIMER_INTERVAL, TIMER_INTERVAL);
		}
		else
		{
			Skip();
		}
	}

	private View.OnClickListener m_buttonClickListener = new View.OnClickListener()
	{
		@Override
		public void onClick(View v) {
			switch (v.getId())
			{
				case R.id.splash_emulate_api:
					Configs.Instance().SetConfig(Configs.ID_CONFIG_LINGDONG_API, Constants.ID_CONFIG_API_EMULATE);
					break;
				case R.id.splash_real_api:
					Configs.Instance().SetConfig(Configs.ID_CONFIG_LINGDONG_API, Constants.ID_CONFIG_API_REAL);
				default:
					break;
			}
			Skip();
		}
	};

	private void Skip()
	{
		m_handler.removeCallbacks(m_callback);
		ToMainPage();
	}
	
	private void ToMainPage()
	{
		if(m_navLocked)
			return;
		m_navLocked = true;
		findViewById(R.id.splash_indicator).setVisibility(View.INVISIBLE);
		if(m_timer != null)
		{
			m_timer.cancel();
			m_timer.purge();
		}
		Intent intent = new Intent(SplashActivity.this, MainActivity.class);
		startActivity(intent);
		finish();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		App.Instance().PopActivity(this);
	}

	private Object Test(Object ...args)
	{
		Object ret = null;

		/*LingDongApi api = Configs.Instance().GetLingDongApi(this);
		api.DeviceSetLCDBlackLight(true);*/

		try
		{
			throw new IOException();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			Logf.DumpException(e);
		}
		if(true) return null;

		return ret;
	}

	private void p(byte arr[])
	{
		StringBuffer sb = new StringBuffer();
		int i = 0;
		for (byte b : arr)
		{
			sb.append(b).append(' ');
			i++;
		}
		sb.append('\n');
		Logf.e(ID_TAG, i);
		Logf.e(ID_TAG, sb.toString());
	}
}
