package com.youtushuju.lingdongapp;

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
import com.youtushuju.lingdongapp.database.RecordServices;
import com.youtushuju.lingdongapp.device.DeviceUtility;
import com.youtushuju.lingdongapp.gui.App;

import java.util.List;

public class SettingsFragment extends PreferenceFragmentCompat
{
	private static final String ID_TAG = "SettingsFragment";

	@Override
	public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
		addPreferencesFromResource(R.xml.settings_preference);
		Preference preference;
		final Configs configs = Configs.Instance();

		preference = findPreference(Constants.ID_PREFERENCE_FACE_FREQUENCY);
		//((EditTextPreference)(preference)).getEditText().setInputType(InputType.TYPE_CLASS_NUMBER);
		preference.setDefaultValue(Configs.ID_PREFERENCE_DEFAULT_FACE_FREQUENCY);
		preference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				preference.setSummary(newValue.toString());
				return true;
			}
		});

		ListPreference listPreference = (ListPreference) findPreference(Constants.ID_PREFERENCE_FACE_CAMERA);
		preference.setDefaultValue("" + Configs.ID_PREFERENCE_DEFAULT_FACE_CAMERA);
		preference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				preference.setSummary(newValue.toString());
				return true;
			}
		});

		preference = findPreference(Constants.ID_PREFERENCE_SERIAL_PATH);
		preference.setDefaultValue(Configs.ID_PREFERENCE_DEFAULT_SERIAL_PATH);
		preference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				preference.setSummary(newValue.toString());
				return true;
			}
		});

		preference = findPreference(Constants.ID_PREFERENCE_SERIAL_BAUDRATE);
		//((EditTextPreference)(preference)).getEditText().setInputType(InputType.TYPE_CLASS_NUMBER);
		preference.setDefaultValue(Configs.ID_PREFERENCE_DEFAULT_SERIAL_BAUDRATE);
		preference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				preference.setSummary(newValue.toString());
				return true;
			}
		});

		preference = findPreference(Constants.ID_PREFERENCE_FACE_IMAGE_QUALITY);
		preference.setDefaultValue(Configs.ID_PREFERENCE_DEFAULT_FACE_IMAGE_QUALITY);
		preference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				preference.setSummary(newValue.toString());
				return true;
			}
		});

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
		preference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				preference.setSummary(newValue.toString());
				SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(getContext()).edit();
				editor.putString(Constants.ID_PREFERENCE_SERIAL_PATH, newValue.toString());
				editor.commit();
				return true;
			}
		});

		preference = findPreference(Constants.ID_PREFERENCE_FACE_CAPTURE_SCHEME);
		preference.setDefaultValue(Configs.ID_PREFERENCE_DEFAULT_FACE_CAPTURE_SCHEME);
		preference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				preference.setSummary(newValue.toString());
				return true;
			}
		});

		preference = findPreference(Constants.ID_PREFERENCE_CAMERA_RESOLUTION);
		preference.setDefaultValue(Configs.ID_PREFERENCE_DEFAULT_CAMERA_RESOLUTION);
		preference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				preference.setSummary(newValue.toString());
				return true;
			}
		});

		preference = findPreference(Constants.ID_PREFERENCE_CLEAN_LOG);
		preference.setSummary(FS.FormatSize(App.Instance().GetLogSize()));
		preference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(final Preference preference) {
				OpenQueryDialog("警告", "确定要清空所有日志文件?", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						boolean res = App.Instance().CleanLog();
						if(res)
						{
							preference.setSummary("无");
							Toast.makeText(getContext(), "日志文件已清空", Toast.LENGTH_SHORT).show();
						}
						else
							Toast.makeText(getContext(), "清空日志文件失败", Toast.LENGTH_LONG).show();
					}
				});
				return true;
			}
		});

		preference = findPreference(Constants.ID_PREFERENCE_OPEN_SCREEN_SAVER_MAX_INTERVAL);
		//((EditTextPreference)(preference)).getEditText().setInputType(InputType.TYPE_CLASS_NUMBER);
		preference.setDefaultValue(Configs.ID_PREFERENCE_DEFAULT_OPEN_SCREEN_SAVER_MAX_INTERVAL);
		preference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				preference.setSummary(newValue.toString());
				return true;
			}
		});

		preference = findPreference(Constants.ID_PREFERENCE_OPERATE_DEVICE_TIMEOUT);
		//((EditTextPreference)(preference)).getEditText().setInputType(InputType.TYPE_CLASS_NUMBER);
		preference.setDefaultValue(Configs.ID_PREFERENCE_DEFAULT_OPERATE_DEVICE_TIMEOUT);
		preference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				preference.setSummary(newValue.toString());
				return true;
			}
		});

		preference = findPreference(Constants.ID_PREFERENCE_CAMERA_USAGE_PLAN);
		preference.setDefaultValue(Configs.ID_PREFERENCE_DEFAULT_CAMERA_USAGE_PLAN);
		preference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				preference.setSummary(newValue.toString());
				return true;
			}
		});

		preference = findPreference(Constants.ID_PREFERENCE_PREVIEW_CAPTURE_CROP);
		preference.setDefaultValue(Configs.ID_PREFERENCE_DEFAULT_PREVIEW_CAPTURE_CROP);
		preference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				preference.setSummary(newValue.toString());
				return true;
			}
		});

		preference = findPreference(Constants.ID_PREFERENCE_RECORD_HISTORY);
		preference.setDefaultValue(Configs.ID_PREFERENCE_DEFAULT_RECORD_HISTORY);
		preference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				preference.setSummary(newValue.toString());
				return true;
			}
		});

		final RecordServices recordServices = new RecordServices(getContext());
		preference = findPreference(Constants.ID_PREFERENCE_CLEAN_RECORD);
		preference.setSummary(recordServices.Count(null, null) + "条");
		preference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(final Preference preference) {
				OpenQueryDialog("警告", "确定要清空所有日志文件?", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						recordServices.DeleteAll();
						preference.setSummary(recordServices.Count(null, null) + "条");
						Toast.makeText(getContext(), "历史记录已清空", Toast.LENGTH_SHORT).show();
					}
				});
				return true;
			}
		});

		preference = findPreference(Constants.ID_PREFERENCE_DEBUG_MODE);
		preference.setDefaultValue("" + configs.GetConfig(Configs.ID_CONFIG_DEBUG));
		preference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				preference.setSummary(newValue.toString());
				try
				{
					configs.SetConfig(Configs.ID_CONFIG_DEBUG, Integer.parseInt(newValue.toString()));
				}
				catch (Exception e)
				{
					configs.SetConfig(Configs.ID_CONFIG_DEBUG, 0);
					e.printStackTrace();
				}
				return true;
			}
		});

		preference = findPreference(Constants.ID_PREFERENCE_PLAY_VOICE_ALERT);
		preference.setDefaultValue(Configs.ID_PREFERENCE_DEFAULT_PLAY_VOICE_ALERT);
		preference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				preference.setSummary(newValue.toString());
				return true;
			}
		});
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
}
