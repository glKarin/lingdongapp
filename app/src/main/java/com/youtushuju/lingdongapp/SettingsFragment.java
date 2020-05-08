package com.youtushuju.lingdongapp;

import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.preference.ListPreference;

import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

import com.youtushuju.lingdongapp.common.Configs;
import com.youtushuju.lingdongapp.common.Constants;
import com.youtushuju.lingdongapp.device.DeviceUtility;

import java.util.List;

public class SettingsFragment extends PreferenceFragmentCompat
{
	private static final String ID_TAG = "SettingsActivity";

	@Override
	public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
		addPreferencesFromResource(R.xml.settings_preference);
		Preference preference;

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
			String devArr[] = new String[]{"/dev/ttyS1", "/dev/ttyS2", "/dev/ttyS3", "/dev/ttyS5"};
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
	}
}
