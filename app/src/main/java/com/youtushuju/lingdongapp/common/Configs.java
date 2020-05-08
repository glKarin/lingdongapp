package com.youtushuju.lingdongapp.common;

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
	public static final String ID_PREFERENCE_DEFAULT_SERIAL_PATH = "/dev/ttyS1";
	public static final int ID_PREFERENCE_DEFAULT_SERIAL_BAUDRATE = 0;
	public static final String ID_PREFERENCE_DEFAULT_SERIAL_DRIVER = Constants.ID_CONFIG_SERIAL_DRIVER_UART;
	public static final int ID_PREFERENCE_DEFAULT_FACE_IMAGE_QUALITY = 50;
	public static final String ID_PREFERENCE_DEFAULT_FACE_CAPTURE_SCHEME = Constants.ID_CONFIG_FACE_CAPTURE_SCHEME_WHEN_FACE;
	public static final String ID_PREFERENCE_DEFAULT_CAMERA_RESOLUTION = Constants.ID_CONFIG_CAMERA_RESOLUTION_HIGHER;

	public static final String ID_CONFIG_LINGDONG_API = "ling_dong_api";
	public static final String ID_CONFIG_LOG_FILE = "log_file";

	private static final String ID_CONFIG_LOG_FILE_PREFIX = "ling_dong_app.";
	private static final String ID_CONFIG_LOG_FILE_SUFFIX = ".log.txt";
	private static final String ID_CONFIG_LOG_DIRECTORY = "log";
	public static final String ID_CONFIG_WORK_DIRECTORY = "ling_dong_app";

	private static Configs _configs;
	private Map<String, Object> m_configs;

	private Configs()
	{
		m_configs = new HashMap<String, Object>();
		m_configs.put(ID_CONFIG_LINGDONG_API, Constants.ID_CONFIG_API_REAL); // 默认真机
		m_configs.put(ID_CONFIG_LOG_FILE, GetFile(ID_CONFIG_LOG_DIRECTORY + File.separator + ID_CONFIG_LOG_FILE_PREFIX + new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date()) + ID_CONFIG_LOG_FILE_SUFFIX, true));
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
		if(context == null)
			return null;

		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
		String driver = preferences.getString(Constants.ID_PREFERENCE_SERIAL_DRIVER, Constants.ID_CONFIG_SERIAL_DRIVER_CEPR);
		if(Constants.ID_CONFIG_SERIAL_DRIVER_CEPR.equals(driver))
			return new SerialPortFunc_cepr();
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

	public String GetFilePath(String name)
	{
		String path = Environment.getExternalStorageDirectory().getPath() + File.separator + ID_CONFIG_WORK_DIRECTORY;
		if(!Common.StringIsBlank(path))
			path += File.separator + name;
		return path;
	}

	public File GetFile(String name, boolean create)
	{
		File file = null;
		String path = GetFilePath(name);
		boolean isDir = path.endsWith(File.separator);
		if(create)
		{
			file = new File(path);
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
