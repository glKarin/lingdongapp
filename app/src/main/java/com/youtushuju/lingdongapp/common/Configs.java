package com.youtushuju.lingdongapp.common;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.hardware.camera2.CameraCharacteristics;
import android.os.Environment;
import android.util.Log;

import androidx.preference.PreferenceManager;

import com.youtushuju.lingdongapp.device.LingDongApi_real;
import com.youtushuju.lingdongapp.device.LingDongApi;
import com.youtushuju.lingdongapp.device.LingDongApi_emulate;
import com.youtushuju.lingdongapp.device.SerialPortFunc;
import com.youtushuju.lingdongapp.device.SerialPortFunc_cepr;
import com.youtushuju.lingdongapp.device.SerialPortFunc_test;
import com.youtushuju.lingdongapp.device.SerialPortFunc_uc;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public final class Configs
{
	private static final String ID_TAG = "Configs";
	public static final int ID_PREFERENCE_DEFAULT_FACE_FREQUENCY = 2000; //ms
	public static final int ID_PREFERENCE_DEFAULT_FACE_CAMERA = CameraCharacteristics.LENS_FACING_FRONT; //ms
	public static final String ID_PREFERENCE_DEFAULT_SERIAL_PATH = "/dev/ttyS4";
	public static final int ID_PREFERENCE_DEFAULT_SERIAL_BAUDRATE = 9600;
	public static final String ID_PREFERENCE_DEFAULT_SERIAL_DRIVER = Constants.ID_CONFIG_SERIAL_DRIVER_CEPR;
	public static final int ID_PREFERENCE_DEFAULT_FACE_IMAGE_QUALITY = 50;
	public static final String ID_PREFERENCE_DEFAULT_FACE_CAPTURE_SCHEME = Constants.ID_CONFIG_FACE_CAPTURE_SCHEME_ALWAYS; // Constants.ID_CONFIG_FACE_CAPTURE_SCHEME_WHEN_FACE;
	public static final String ID_PREFERENCE_DEFAULT_CAMERA_RESOLUTION = Constants.ID_CONFIG_CAMERA_RESOLUTION_HIGHEST; // Constants.ID_CONFIG_CAMERA_RESOLUTION_HIGHER;
	public static final int ID_PREFERENCE_DEFAULT_OPEN_SCREEN_SAVER_MAX_INTERVAL = 10000;
	public static final int ID_PREFERENCE_DEFAULT_OPERATE_DEVICE_TIMEOUT = 10000;
	public static final int ID_PREFERENCE_DEFAULT_CAMERA_USAGE_PLAN = Constants.ID_CONFIG_CAMERA_USAGE_PLAN_CLOSE_WHEN_UNUSED_AND_REOPEN_WHEN_NEED;
	public static final boolean ID_PREFERENCE_DEFAULT_PREVIEW_CAPTURE_CROP = true;
	public static final boolean ID_PREFERENCE_DEFAULT_RECORD_HISTORY = true;
	public static final boolean ID_PREFERENCE_DEFAULT_PLAY_VOICE_ALERT = true;
	public static final String ID_PREFERENCE_DEFAULT_MAIN_MENU_GEOMETRY = "-10,20,64,64";
	public static final boolean ID_PREFERENCE_DEFAULT_CAMERA_DRAW_BOX = false;
	public static final boolean ID_PREFERENCE_DEFAULT_AUTO_BOOT = true;
	public static final boolean ID_PREFERENCE_DEFAULT_ALLOW_EXIT = false;
	public static final int ID_PREFERENCE_DEFAULT_BGM_INTERVAL = 60000;

	public static final int CONST_DEFAULT_HEARTBEAT_INTERVAL = 2000;
	public static final int CONST_DEFAULT_BGM_PLAY_END_INTERVAL = 0;

	public static final String ID_CONFIG_LINGDONG_API = "ling_dong_api";
	public static final String ID_CONFIG_LOG_FILE = "log_file";
	public static final String ID_CONFIG_DEBUG = "debug";
	public static final String ID_CONFIG_APP_NECESSARY_PERMISSIONS = "app_necessary_permissions";
	public static final String ID_CONFIG_SERIAL_PORT_DEVICE_DRIVER = "serial_port_device_driver";
	public static final String ID_CONFIG_SERIAL_PORT_DEVICE_BAUDRATE = "serial_port_device_baudrate";
	public static final String ID_CONFIG_SERIAL_PORT_DEVICE_PATH = "serial_port_device_path";

	private static final String ID_CONFIG_LOG_FILE_PREFIX = "ling_dong_app.";
	private static final String ID_CONFIG_LOG_FILE_SUFFIX = ".log.txt";
	public static final String ID_CONFIG_LOG_DIRECTORY = "log";
	public static final String ID_CONFIG_WORK_DIRECTORY = "ling_dong_app";
	public static final String ID_CONFIG_CORE_DUMP_FILE = "app_crash.dump.txt";
	public static final String ID_CONFIG_APP_DOWNLOAD = "app_download";

	private static Configs _configs;
	private Map<String, Object> m_configs;

	private Configs()
	{
		InitConfigs();
	}

	private void InitConfigs()
	{
		if(m_configs == null)
			m_configs = new HashMap<String, Object>();

		m_configs.put(ID_CONFIG_LINGDONG_API, Constants.ID_CONFIG_API_REAL); // 默认真机
		m_configs.put(ID_CONFIG_LOG_FILE, GetFile(ID_CONFIG_LOG_DIRECTORY + File.separator + ID_CONFIG_LOG_FILE_PREFIX + new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date()) + ID_CONFIG_LOG_FILE_SUFFIX, true));
		m_configs.put(ID_CONFIG_DEBUG, 0);
		m_configs.put(ID_CONFIG_SERIAL_PORT_DEVICE_DRIVER, ID_PREFERENCE_DEFAULT_SERIAL_DRIVER);
		m_configs.put(ID_CONFIG_SERIAL_PORT_DEVICE_PATH, ID_PREFERENCE_DEFAULT_SERIAL_PATH);
		m_configs.put(ID_CONFIG_SERIAL_PORT_DEVICE_BAUDRATE, ID_PREFERENCE_DEFAULT_SERIAL_BAUDRATE);

		Map<String, String> permissions = new HashMap<String, String>();
		permissions.put(Manifest.permission.CAMERA, "使用摄像头");
		permissions.put(Manifest.permission.READ_PHONE_STATE, "获取设备信息");
		permissions.put(Manifest.permission.READ_EXTERNAL_STORAGE, "访问设备外部存储");
		permissions.put(Manifest.permission.WRITE_EXTERNAL_STORAGE, "写入设备外部存储");
		permissions.put(Manifest.permission.RECEIVE_BOOT_COMPLETED, "开机自启");
		m_configs.put(ID_CONFIG_APP_NECESSARY_PERMISSIONS, permissions);
	}

	public static Configs Instance()
	{
		if(_configs == null)
		{
			_configs = new Configs();
		}
		return _configs;
	}

	public LingDongApi GetLingDongApi(Context context)
	{
		if(context == null)
			return null;

		if(Constants.ID_CONFIG_API_EMULATE.equals(m_configs.get(ID_CONFIG_LINGDONG_API)))
			return new LingDongApi_emulate(context);
		else
			return new LingDongApi_real(context);
	}

	public SerialPortFunc GetLingDongSerialDriver(Context context)
	{
		String driver = null;
		if(context == null)
			driver = (String)GetConfig(ID_CONFIG_SERIAL_PORT_DEVICE_DRIVER);
		else
		{
			SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
			driver = preferences.getString(Constants.ID_PREFERENCE_SERIAL_DRIVER, Constants.ID_CONFIG_SERIAL_DRIVER_CEPR);
		}

		if(Constants.ID_CONFIG_SERIAL_DRIVER_CEPR.equals(driver))
			return new SerialPortFunc_cepr();
		else if(Constants.ID_CONFIG_SERIAL_DRIVER_TEST.equals(driver))
			return new SerialPortFunc_test();
		else
			return new SerialPortFunc_uc();
	}

	public Object GetConfig(String key)
	{
		if(key == null)
			return m_configs;
		else
			return m_configs.get(key);
	}

	public Configs SetConfig(String key, Object value)
	{
		m_configs.put(key, value);
		return this;
	}

	public Object GetConfig(String key, Object value)
	{
		if(m_configs.containsKey(key))
			return m_configs.get(key);
		else
		{
			SetConfig(key, value);
			return value;
		}
	}

	public String GetFilePath(String name)
	{
		String path = Environment.getExternalStorageDirectory().getPath() + File.separator + ID_CONFIG_WORK_DIRECTORY;
		if(!Common.StringIsBlank(path))
			path += File.separator + name;
		Log.e(ID_TAG, path);
		return path;
	}

	public File GetFile(String name, boolean create)
	{
		File file = null;
		String path = GetFilePath(name);
		file = new File(path);
		boolean isDir = path.endsWith(File.separator);
		if(create)
		{
			if(file.exists())
			{
				if(isDir && !file.isDirectory())
					return null;
				if(!isDir && file.isDirectory())
					return null;
				return file;
			}
			else
			{
				if(isDir)
				{
					if(!file.mkdirs())
						return null;
					return file;
				}
				else
				{
					File parent = file.getParentFile();
					if(!parent.exists())
					{
						if(!parent.mkdirs())
							return null;
					}
					try
					{
						if(file.createNewFile())
							return file;
						else
							return null;
					}
					catch (IOException e)
					{
						e.printStackTrace();
						return null;
					}
				}
			}
		}

		return file;
	}

	public File GetFile(String name)
	{
		return GetFile(name, false);
	}
}
