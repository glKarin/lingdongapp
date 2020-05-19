package com.youtushuju.lingdongapp;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.preference.ListPreference;

import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

import com.youtushuju.lingdongapp.common.Configs;
import com.youtushuju.lingdongapp.common.Constants;
import com.youtushuju.lingdongapp.common.FS;
import com.youtushuju.lingdongapp.common.Logf;
import com.youtushuju.lingdongapp.database.RecordServices;
import com.youtushuju.lingdongapp.device.DeviceUtility;
import com.youtushuju.lingdongapp.gui.App;

import java.util.List;

public class SettingsFragment extends PreferenceFragmentCompat implements Preference.OnPreferenceChangeListener, Preference.OnPreferenceClickListener
{
	private static final String ID_TAG = "SettingsFragment";

	@Override
	public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
		addPreferencesFromResource(R.xml.settings_preference);
		Preference preference;
		final Configs configs = Configs.Instance();
		final Context context = getContext();

		preference = findPreference(Constants.ID_PREFERENCE_FACE_FREQUENCY);
		//((EditTextPreference)(preference)).getEditText().setInputType(InputType.TYPE_CLASS_NUMBER);
		preference.setDefaultValue(Configs.ID_PREFERENCE_DEFAULT_FACE_FREQUENCY);
		preference.setOnPreferenceChangeListener(this);

		preference = (ListPreference) findPreference(Constants.ID_PREFERENCE_FACE_CAMERA);
		preference.setDefaultValue("" + Configs.ID_PREFERENCE_DEFAULT_FACE_CAMERA);
		preference.setOnPreferenceChangeListener(this);

		preference = findPreference(Constants.ID_PREFERENCE_SERIAL_PATH);
		preference.setDefaultValue(Configs.ID_PREFERENCE_DEFAULT_SERIAL_PATH);
		preference.setOnPreferenceChangeListener(this);

		preference = findPreference(Constants.ID_PREFERENCE_SERIAL_BAUDRATE);
		//((EditTextPreference)(preference)).getEditText().setInputType(InputType.TYPE_CLASS_NUMBER);
		preference.setDefaultValue(Configs.ID_PREFERENCE_DEFAULT_SERIAL_BAUDRATE);
		preference.setOnPreferenceChangeListener(this);

		preference = findPreference(Constants.ID_PREFERENCE_FACE_IMAGE_QUALITY);
		preference.setDefaultValue(Configs.ID_PREFERENCE_DEFAULT_FACE_IMAGE_QUALITY);
		preference.setOnPreferenceChangeListener(this);

		preference = findPreference(Constants.ID_PREFERENCE_SERIAL_PATH_SELECT);
		preference.setDefaultValue(Configs.ID_PREFERENCE_DEFAULT_SERIAL_PATH);
		List<String> devs = DeviceUtility.GetDevList("ttyS");
		if(devs != null && !devs.isEmpty())
		{
			String devArr[] = new String[devs.size()];
			for (int i = 0; i < devs.size(); i++)
				devArr[i] = devs.get(i);
			((ListPreference)preference).setEntries(devArr);
			((ListPreference)preference).setEntryValues(devArr);
		}
		else
		{
			String devArr[] = new String[]{"/dev/ttyS1", "/dev/ttyS2", "/dev/ttyS3", "/dev/ttyS5", Configs.Instance().GetFilePath("test_serial_port_device.txt")};
			((ListPreference)preference).setEntries(devArr);
			((ListPreference)preference).setEntryValues(devArr);
		}
		preference.setOnPreferenceChangeListener(this);

		preference = findPreference(Constants.ID_PREFERENCE_FACE_CAPTURE_SCHEME);
		preference.setDefaultValue(Configs.ID_PREFERENCE_DEFAULT_FACE_CAPTURE_SCHEME);
		preference.setOnPreferenceChangeListener(this);

		preference = findPreference(Constants.ID_PREFERENCE_CAMERA_RESOLUTION);
		preference.setDefaultValue(Configs.ID_PREFERENCE_DEFAULT_CAMERA_RESOLUTION);
		preference.setOnPreferenceChangeListener(this);

		preference = findPreference(Constants.ID_PREFERENCE_CLEAN_LOG);
		preference.setOnPreferenceClickListener(this);

		preference = findPreference(Constants.ID_PREFERENCE_OPEN_SCREEN_SAVER_MAX_INTERVAL);
		//((EditTextPreference)(preference)).getEditText().setInputType(InputType.TYPE_CLASS_NUMBER);
		preference.setDefaultValue(Configs.ID_PREFERENCE_DEFAULT_OPEN_SCREEN_SAVER_MAX_INTERVAL);
		preference.setOnPreferenceChangeListener(this);

		preference = findPreference(Constants.ID_PREFERENCE_OPERATE_DEVICE_TIMEOUT);
		//((EditTextPreference)(preference)).getEditText().setInputType(InputType.TYPE_CLASS_NUMBER);
		preference.setDefaultValue(Configs.ID_PREFERENCE_DEFAULT_OPERATE_DEVICE_TIMEOUT);
		preference.setOnPreferenceChangeListener(this);

		preference = findPreference(Constants.ID_PREFERENCE_CAMERA_USAGE_PLAN);
		preference.setDefaultValue(Configs.ID_PREFERENCE_DEFAULT_CAMERA_USAGE_PLAN);
		preference.setOnPreferenceChangeListener(this);

		preference = findPreference(Constants.ID_PREFERENCE_PREVIEW_CAPTURE_CROP);
		preference.setDefaultValue(Configs.ID_PREFERENCE_DEFAULT_PREVIEW_CAPTURE_CROP);
		preference.setOnPreferenceChangeListener(this);

		preference = findPreference(Constants.ID_PREFERENCE_RECORD_HISTORY);
		preference.setDefaultValue(Configs.ID_PREFERENCE_DEFAULT_RECORD_HISTORY);
		preference.setOnPreferenceChangeListener(this);

		preference = findPreference(Constants.ID_PREFERENCE_CLEAN_RECORD);
		preference.setOnPreferenceClickListener(this);

		preference = findPreference(Constants.ID_PREFERENCE_DEBUG_MODE);
		preference.setDefaultValue("" + configs.GetConfig(Configs.ID_CONFIG_DEBUG));
		preference.setOnPreferenceChangeListener(this);

		preference = findPreference(Constants.ID_PREFERENCE_PLAY_VOICE_ALERT);
		preference.setDefaultValue(Configs.ID_PREFERENCE_DEFAULT_PLAY_VOICE_ALERT);
		preference.setOnPreferenceChangeListener(this);

		preference = findPreference(Constants.ID_PREFERENCE_SERIAL_DRIVER);
		preference.setDefaultValue(Configs.ID_PREFERENCE_DEFAULT_SERIAL_DRIVER);
		preference.setOnPreferenceChangeListener(this);
	}

	@Override
	public boolean onPreferenceChange(Preference preference, Object newValue) {
		String key = preference.getKey();
		Context context = getContext();

		if(Constants.ID_PREFERENCE_SERIAL_PATH_SELECT.equals(key))
		{
			SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
			editor.putString(Constants.ID_PREFERENCE_SERIAL_PATH, newValue.toString());
			editor.commit();
			SetSummary(Constants.ID_PREFERENCE_SERIAL_PATH, null);
		}
		else if(Constants.ID_PREFERENCE_DEBUG_MODE.equals(key))
		{
			Configs configs = Configs.Instance();
			try
			{
				configs.SetConfig(Configs.ID_CONFIG_DEBUG, Integer.parseInt(newValue.toString()));
			}
			catch (Exception e)
			{
				configs.SetConfig(Configs.ID_CONFIG_DEBUG, 0);
				e.printStackTrace();
			}
		}

		SetSummary(key, newValue);
		return true;
	}

	@Override
	public boolean onPreferenceClick(final Preference preference) {
		String key = preference.getKey();

		if(Constants.ID_PREFERENCE_CLEAN_LOG.equals(key))
		{
			OpenQueryDialog("警告", "确定要清空所有日志文件?", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					boolean res = App.Instance().CleanLog();
					if(res)
					{
						SetSummary(Constants.ID_PREFERENCE_CLEAN_LOG, null);
						Toast.makeText(getContext(), "日志文件已清空", Toast.LENGTH_SHORT).show();
					}
					else
						Toast.makeText(getContext(), "清空日志文件失败", Toast.LENGTH_LONG).show();
				}
			});
		}
		else if(Constants.ID_PREFERENCE_CLEAN_RECORD.equals(key))
		{
			OpenQueryDialog("警告", "确定要清空所有历史记录?", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					RecordServices recordServices = new RecordServices(getContext());
					recordServices.DeleteAll();
					SetSummary(Constants.ID_PREFERENCE_CLEAN_RECORD, null);
					Toast.makeText(getContext(), "历史记录已清空", Toast.LENGTH_SHORT).show();
				}
			});
		}
		else
			return false;
		return true;
	}

	private void OpenQueryDialog(String title, String message, DialogInterface.OnClickListener listener)
	{
		AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
		builder.setTitle(title);
		builder.setMessage(message);
		builder.setIcon(R.drawable.icon_warning);
		builder.setPositiveButton("确定", listener);
		builder.setNegativeButton("取消", null);
		AlertDialog dialog = builder.create();
		dialog.show();
	}

	private String GetListName(String value, int keyResource, int nameResource)
	{
		String keys[] = getResources().getStringArray(keyResource);
		String names[] = getResources().getStringArray(nameResource);

		for(int i = 0; i < keys.length; i++)
		{
			if(keys[i].equals(value))
				return names[i];
		}
		return null;
	}

	private String GetCameraFaceName(String face)
	{
		return GetListName(face, R.array.camera_id, R.array.camera_name);
	}

	private String GetFaceCaptureSchemeName(String s)
	{
		return GetListName(s, R.array.face_capture_scheme_key, R.array.face_capture_scheme_name);
	}

	private String GetCameraResolutionName(String r)
	{
		return GetListName(r, R.array.camera_resolution_key, R.array.camera_resolution_name);
	}

	private String GetCameraUsagePlanName(String p)
	{
		return GetListName(p, R.array.camera_usage_plan_key, R.array.camera_usage_plan_name);
	}

	private String GetSerialDriverName(String d)
	{
		return GetListName(d, R.array.serial_driver_key, R.array.serial_driver_name);
	}

	@Override
	public void onResume() {
		super.onResume();
		SetSummary(null, null);
	}

	public void SetSummary(String key, Object newValue)
	{
		Context context = getContext();
		final Configs configs = Configs.Instance();
		Preference preference;
		String summary = null;
		String value = null;
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
		//Logf.e(ID_TAG, key + " " + newValue);

		if(key == null || Constants.ID_PREFERENCE_FACE_FREQUENCY.equals(key))
		{
			preference = findPreference(Constants.ID_PREFERENCE_FACE_FREQUENCY);
			value = newValue != null ? newValue.toString() : sharedPreferences.getString(Constants.ID_PREFERENCE_FACE_FREQUENCY, "" + Configs.ID_PREFERENCE_DEFAULT_FACE_FREQUENCY);
			summary = value + "毫秒";
			preference.setSummary(summary);
		}

		if(key == null || Constants.ID_PREFERENCE_FACE_CAMERA.equals(key))
		{
			preference = (ListPreference) findPreference(Constants.ID_PREFERENCE_FACE_CAMERA);
			value = newValue != null ? newValue.toString() : sharedPreferences.getString(Constants.ID_PREFERENCE_FACE_CAMERA, "" + Configs.ID_PREFERENCE_DEFAULT_FACE_CAMERA);
			summary = GetCameraFaceName(value);
			if (summary != null)
				preference.setSummary(summary);
		}

		if(key == null || Constants.ID_PREFERENCE_SERIAL_PATH.equals(key))
		{
			preference = findPreference(Constants.ID_PREFERENCE_SERIAL_PATH);
			value = newValue != null ? newValue.toString() : sharedPreferences.getString(Constants.ID_PREFERENCE_SERIAL_PATH, Configs.ID_PREFERENCE_DEFAULT_SERIAL_PATH);
			summary = value;
			preference.setSummary(summary);
		}

		if(key == null || Constants.ID_PREFERENCE_SERIAL_BAUDRATE.equals(key))
		{
			preference = findPreference(Constants.ID_PREFERENCE_SERIAL_BAUDRATE);
			value = newValue != null ? newValue.toString() : sharedPreferences.getString(Constants.ID_PREFERENCE_SERIAL_BAUDRATE, "" + Configs.ID_PREFERENCE_DEFAULT_SERIAL_BAUDRATE);
			summary = value;
			preference.setSummary(summary);
		}

		if(key == null || Constants.ID_PREFERENCE_FACE_IMAGE_QUALITY.equals(key))
		{
			preference = findPreference(Constants.ID_PREFERENCE_FACE_IMAGE_QUALITY);
			value = newValue != null ? newValue.toString() : ("" + sharedPreferences.getInt(Constants.ID_PREFERENCE_FACE_IMAGE_QUALITY, Configs.ID_PREFERENCE_DEFAULT_FACE_IMAGE_QUALITY));
			summary = value + "%";
			preference.setSummary(summary);
		}

		if(key == null || Constants.ID_PREFERENCE_SERIAL_PATH_SELECT.equals(key))
		{
			preference = findPreference(Constants.ID_PREFERENCE_SERIAL_PATH_SELECT);
			value = newValue != null ? newValue.toString() : sharedPreferences.getString(Constants.ID_PREFERENCE_SERIAL_PATH_SELECT, Configs.ID_PREFERENCE_DEFAULT_SERIAL_PATH);
			summary = value;
			preference.setSummary(summary);
		}

		if(key == null || Constants.ID_PREFERENCE_FACE_CAPTURE_SCHEME.equals(key))
		{
			preference = (ListPreference) findPreference(Constants.ID_PREFERENCE_FACE_CAPTURE_SCHEME);
			value = newValue != null ? newValue.toString() : sharedPreferences.getString(Constants.ID_PREFERENCE_FACE_CAPTURE_SCHEME, Configs.ID_PREFERENCE_DEFAULT_FACE_CAPTURE_SCHEME);
			summary = GetFaceCaptureSchemeName(value);
			if (summary != null)
				preference.setSummary(summary);
		}

		if(key == null || Constants.ID_PREFERENCE_CAMERA_RESOLUTION.equals(key))
		{
			preference = (ListPreference) findPreference(Constants.ID_PREFERENCE_CAMERA_RESOLUTION);
			value = newValue != null ? newValue.toString() : sharedPreferences.getString(Constants.ID_PREFERENCE_CAMERA_RESOLUTION, Configs.ID_PREFERENCE_DEFAULT_CAMERA_RESOLUTION);
			summary = GetCameraResolutionName(value);
			if (summary != null)
				preference.setSummary(summary);
		}

		if(key == null || Constants.ID_PREFERENCE_OPEN_SCREEN_SAVER_MAX_INTERVAL.equals(key))
		{
			preference = findPreference(Constants.ID_PREFERENCE_OPEN_SCREEN_SAVER_MAX_INTERVAL);
			value = newValue != null ? newValue.toString() : sharedPreferences.getString(Constants.ID_PREFERENCE_OPEN_SCREEN_SAVER_MAX_INTERVAL, "" + Configs.ID_PREFERENCE_DEFAULT_OPEN_SCREEN_SAVER_MAX_INTERVAL);
			summary = value + "毫秒";
			preference.setSummary(summary);
		}

		if(key == null || Constants.ID_PREFERENCE_OPERATE_DEVICE_TIMEOUT.equals(key))
		{
			preference = findPreference(Constants.ID_PREFERENCE_OPERATE_DEVICE_TIMEOUT);
			value = newValue != null ? newValue.toString() : sharedPreferences.getString(Constants.ID_PREFERENCE_OPERATE_DEVICE_TIMEOUT, "" + Configs.ID_PREFERENCE_DEFAULT_OPERATE_DEVICE_TIMEOUT);
			summary = value + "毫秒";
			preference.setSummary(summary);
		}

		if(key == null || Constants.ID_PREFERENCE_CAMERA_USAGE_PLAN.equals(key))
		{
			preference = (ListPreference) findPreference(Constants.ID_PREFERENCE_CAMERA_USAGE_PLAN);
			value = newValue != null ? newValue.toString() : sharedPreferences.getString(Constants.ID_PREFERENCE_CAMERA_USAGE_PLAN, "" + Configs.ID_PREFERENCE_DEFAULT_CAMERA_USAGE_PLAN);
			summary = GetCameraUsagePlanName(value);
			if (summary != null)
				preference.setSummary(summary);
		}

		if(key == null || Constants.ID_PREFERENCE_PREVIEW_CAPTURE_CROP.equals(key))
		{
			preference = findPreference(Constants.ID_PREFERENCE_PREVIEW_CAPTURE_CROP);
			boolean b = newValue != null ? (boolean)newValue : sharedPreferences.getBoolean(Constants.ID_PREFERENCE_PREVIEW_CAPTURE_CROP, Configs.ID_PREFERENCE_DEFAULT_PREVIEW_CAPTURE_CROP);
			value = b ? "启用裁剪" : "不裁剪";
			summary = value;
			preference.setSummary(summary);
		}

		if(key == null || Constants.ID_PREFERENCE_RECORD_HISTORY.equals(key))
		{
			preference = findPreference(Constants.ID_PREFERENCE_RECORD_HISTORY);
			boolean b = newValue != null ? (boolean)newValue : sharedPreferences.getBoolean(Constants.ID_PREFERENCE_RECORD_HISTORY, Configs.ID_PREFERENCE_DEFAULT_RECORD_HISTORY);
			value = b ? "记录到本地数据库" : "不做记录";
			summary = value;
			preference.setSummary(summary);
		}

		if(key == null || Constants.ID_PREFERENCE_PLAY_VOICE_ALERT.equals(key))
		{
			preference = findPreference(Constants.ID_PREFERENCE_PLAY_VOICE_ALERT);
			boolean b = newValue != null ? (boolean)newValue : sharedPreferences.getBoolean(Constants.ID_PREFERENCE_PLAY_VOICE_ALERT, Configs.ID_PREFERENCE_DEFAULT_PLAY_VOICE_ALERT);
			value = b ? "播放语音提示" : "静音";
			summary = value;
			preference.setSummary(summary);
		}

		if(key == null || Constants.ID_PREFERENCE_DEBUG_MODE.equals(key))
		{
			preference = findPreference(Constants.ID_PREFERENCE_DEBUG_MODE);
			value = newValue != null ? newValue.toString() : ("" + configs.GetConfig(Configs.ID_CONFIG_DEBUG));
			summary = value + "(" + ("0".equals(value) ? "禁用" : "启用") + ")";
			preference.setSummary(summary);
		}

		if(key == null || Constants.ID_PREFERENCE_CLEAN_LOG.equals(key))
		{
			preference = findPreference(Constants.ID_PREFERENCE_CLEAN_LOG);
			value = FS.FormatSize(App.Instance().GetLogSize());
			summary = "总大小: " + value;
			preference.setSummary(summary);
		}

		if(key == null || Constants.ID_PREFERENCE_CLEAN_RECORD.equals(key))
		{
			RecordServices recordServices = new RecordServices(context);
			preference = findPreference(Constants.ID_PREFERENCE_CLEAN_RECORD);
			value = "" + recordServices.Count(null, null);
			summary = value + "条";
			preference.setSummary(summary);
		}

		if(key == null || Constants.ID_PREFERENCE_SERIAL_DRIVER.equals(key))
		{
			preference = (ListPreference) findPreference(Constants.ID_PREFERENCE_SERIAL_DRIVER);
			value = newValue != null ? newValue.toString() : sharedPreferences.getString(Constants.ID_PREFERENCE_SERIAL_DRIVER, Configs.ID_PREFERENCE_DEFAULT_SERIAL_DRIVER);
			summary = GetSerialDriverName(value);
			if (summary != null)
				preference.setSummary(summary);
		}
	}
}
