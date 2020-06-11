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
import com.youtushuju.lingdongapp.gui.CircleProgressIndicatorView;
import com.youtushuju.lingdongapp.gui.PipelineView;
import com.youtushuju.lingdongapp.json.JSON;
import com.youtushuju.lingdongapp.json.JsonMap;

import java.util.*;

public class SplashActivity extends AppCompatActivity
{
	private static final String ID_TAG = "SplashActivity";
	private static final int SPLASH_DELAY = 3500;
	
	private boolean m_navLocked = false;
	private int m_timerDelay = SPLASH_DELAY;
	private boolean m_started = false;
	private AlertDialog m_permissionDialog = null;
	private boolean m_granted = false;
	private CircleProgressIndicatorView m_indicator;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		ActivityUtility.HideNavBar(this);
		setContentView(R.layout.splash_page);
		App app = App.Instance();

		//app.Init(this);
		app.PushActivity(this);

		if((int)Configs.Instance().GetConfig(Configs.ID_CONFIG_DEBUG) == 0)
			findViewById(R.id.splash_debug_panel).setVisibility(View.GONE);
		m_indicator = (CircleProgressIndicatorView)findViewById(R.id.splash_indicator);

		SetupUI();

		//CheckAppNecessaryPermission(false, true);
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
			App.HandleException(e);
		}

		m_indicator.SetProgressListener(new CircleProgressIndicatorView.ProgressListener() {
			@Override
			public void OnProgress(CircleProgressIndicatorView view) {

			}

			@Override
			public void OnReady(CircleProgressIndicatorView view) {

			}

			@Override
			public void OnStarted(CircleProgressIndicatorView view) {

			}

			@Override
			public void OnFinished(CircleProgressIndicatorView view) {
				view.Shutdown();
				ToMainPage();
			}

			@Override
			public void OnStateChanged(CircleProgressIndicatorView view) {

			}
		});

		m_indicator.setOnClickListener(new View.OnClickListener(){
			public void onClick(View view)
			{
				if(m_navLocked)
					return;
				m_indicator.Shutdown();
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
		ToMainPage();
	}
	
	private void ToMainPage()
	{
		if(Test() != null)
			return;
		if(m_navLocked)
			return;
		m_navLocked = true;
		m_indicator.setVisibility(View.INVISIBLE);
		m_indicator.Shutdown();
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
		if(m_timerDelay <= 0)
		{
			Skip();
		}
	}

	private void CheckAppNecessaryPermission(boolean grant, boolean direct)
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
				OpenPermissionGrantFailDialog(list, direct);
		}
		else
		{
			StartSplash();
		}
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		m_granted = true;
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
				OpenPermissionGrantFailDialog(ps, false);
			else
				StartSplash();
		}
	}

	// grant = true: 程序里授权, = false: 设置里手动授权
	private void OpenPermissionGrantFailDialog(final List<String> list, final boolean grant) {
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
						if(grant)
						{
							String rps[] = new String[list.size()];
							for (int i = 0; i < list.size(); i++)
								rps[i] = list.get(i);
							ActivityCompat.requestPermissions(SplashActivity.this, rps, ActivityUtility.ID_REQUEST_PERMISSION_RESULT);
						}
						else
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
		builder.setIcon(R.drawable.icon_warning);
		builder.setCancelable(false);
		builder.setPositiveButton("授权", listener);
		builder.setNegativeButton("拒绝", listener);
		m_permissionDialog = builder.create();
		m_permissionDialog.show();
	}

	@Override
	protected void onResume() {
		super.onResume();
		CheckAppNecessaryPermission(false, !m_granted);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		App.Instance().PopActivity(this);
	}

	private Object Test(Object ...args)
	{
		Object ret = null;

		if(true) return null;

		Intent intent = new Intent(this, LayoutActivity.class);
		startActivity(intent);
		ret = intent;

		return ret;
	}
}
