package com.youtushuju.lingdongapp;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.preference.PreferenceManager;

import android.Manifest;
import android.graphics.Rect;
import android.os.*;
import android.content.pm.*;
import android.util.Log;
import android.util.Size;
import android.widget.*;
import android.content.*;
import android.view.*;

import com.youtushuju.lingdongapp.common.Logf;
import com.youtushuju.lingdongapp.device.LingDongApi;
import com.youtushuju.lingdongapp.common.Configs;
import com.youtushuju.lingdongapp.common.Constants;
import com.youtushuju.lingdongapp.gui.ActivityUtility;
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
	private boolean m_started = false;
	private AlertDialog m_permissionDialog = null;
	
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

		Configs configs = Configs.Instance();
		boolean buildOnDebug = ActivityUtility.BuildOnDebug(this);
		int debugMode = buildOnDebug ? 0xFF : 0;
		configs.SetConfig(Configs.ID_CONFIG_LINGDONG_API, buildOnDebug ? Constants.ID_CONFIG_API_EMULATE : Constants.ID_CONFIG_API_REAL);
		configs.SetConfig(Configs.ID_CONFIG_DEBUG, debugMode);
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
		SharedPreferences.Editor editor = preferences.edit();
		{
			editor.putString(Constants.ID_PREFERENCE_DEBUG_MODE, "" + debugMode);
		}
		editor.commit();

		SetupUI();

		App.Instance().PushActivity(this);
		Test();

		CheckAppNecessaryPermission(true);
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
					Configs.Instance().SetConfig(Configs.ID_CONFIG_LINGDONG_API, ActivityUtility.BuildOnDebug(SplashActivity.this) ? Constants.ID_CONFIG_API_EMULATE : Constants.ID_CONFIG_API_REAL);
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

	private void StartSplash()
	{
		if(m_started)
			return;

		m_started = true;
		if(m_permissionDialog != null)
		{
			m_permissionDialog.dismiss();
			m_permissionDialog = null;
		}
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

	private void CheckAppNecessaryPermission(boolean grant)
	{
		Map<String, String> permissions = (Map<String, String>)Configs.Instance().GetConfig(Configs.ID_CONFIG_APP_NECESSARY_PERMISSIONS);
		List<String> list = null;

		for (String p : permissions.keySet())
		{
			if(!ActivityUtility.IsGrantPermission(this, p))
			{
				if(list == null)
					list = new ArrayList<String>();
				list.add(p);
			}
		}

		if(list != null && !list.isEmpty())
		{
			String rps[] = new String[list.size()];
			for (int i = 0; i < list.size(); i++)
				rps[i] = list.get(i);
			if(grant)
				ActivityCompat.requestPermissions(this, rps, ActivityUtility.ID_REQUEST_PERMISSION_RESULT);
			else
				OpenPermissionGrantFailDialog(list);
		}
		else
		{
			StartSplash();
		}
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		if(requestCode == ActivityUtility.ID_REQUEST_PERMISSION_RESULT)
		{
			List<String> ps = null;
			for (int i = 0; i < permissions.length; i++)
			{
				if(grantResults[i] != PackageManager.PERMISSION_GRANTED)
				{
					if(ps == null)
						ps = new ArrayList<String>();
					ps.add(permissions[i]);
				}
			}
			if(ps != null)
				OpenPermissionGrantFailDialog(ps);
			else
				StartSplash();
		}
	}

	private void OpenPermissionGrantFailDialog(List<String> list) {
		if(list == null || list.isEmpty())
			return;

		if(m_permissionDialog != null)
		{
			m_permissionDialog.dismiss();
			m_permissionDialog = null;
		}

		DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				switch (which) {
					case DialogInterface.BUTTON_POSITIVE:
						ActivityUtility.OpenAppSetting(SplashActivity.this);
						break;
					case DialogInterface.BUTTON_NEGATIVE:
					default:
						App.Instance().Exit(1);
						break;
				}
			}
		};
		Map<String, String> permissions = (Map<String, String>)Configs.Instance().GetConfig(Configs.ID_CONFIG_APP_NECESSARY_PERMISSIONS);
		StringBuffer sb = new StringBuffer();
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("程序运行必须的权限");
		sb.append("请授权程序请求的权限: \n");
		for (String p : list)
			sb.append("  " + permissions.get(p) + "\n");
		builder.setMessage(sb.toString());
		builder.setIcon(R.drawable.icon_profile);
		builder.setCancelable(false);
		builder.setPositiveButton("授权", listener);
		builder.setNegativeButton("拒绝", listener);
		m_permissionDialog = builder.create();
		m_permissionDialog.show();
	}

	@Override
	protected void onResume() {
		super.onResume();
		CheckAppNecessaryPermission(false);
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

		if(true) return null;

		{
			Size src = new Size(1280, 720);
			Size dst = new Size(1080, 1856);
			Rect rect = MainActivity.CaleCropSize(dst, src);
			Logf.e(ID_TAG, rect);
		}
		{
			Size src = new Size(1280, 720);
			Size dst = new Size(1080, 1794);
			Rect rect = MainActivity.CaleCropSize(dst, src);
			Logf.e(ID_TAG, rect);
		}

		return ret;
	}
}
