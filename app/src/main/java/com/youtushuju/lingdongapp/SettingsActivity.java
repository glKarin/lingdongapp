package com.youtushuju.lingdongapp;
import android.content.Context;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.preference.*;
import android.os.*;
import android.text.InputType;
import android.view.Window;

import com.youtushuju.lingdongapp.common.Configs;
import com.youtushuju.lingdongapp.common.Constants;
import com.youtushuju.lingdongapp.gui.App;

public class SettingsActivity extends PreferenceActivity
{
	private static final String ID_TAG = "SettingsActivity";

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		
		addPreferencesFromResource(R.xml.settings_preference);
		Preference preference;

		preference = findPreference(Constants.ID_PREFERENCE_FACE_FREQUENCY);
		((EditTextPreference)(preference)).getEditText().setInputType(InputType.TYPE_CLASS_NUMBER);
		preference.setDefaultValue(Configs.ID_PREFERENCE_DEFAULT_FACE_FREQUENCY);

		ListPreference listPreference = (ListPreference) findPreference(Constants.ID_PREFERENCE_FACE_CAMERA);
		preference.setDefaultValue("" + Configs.ID_PREFERENCE_DEFAULT_FACE_CAMERA);

		preference = findPreference(Constants.ID_PREFERENCE_SERIAL_PATH);
		preference.setDefaultValue(Configs.ID_PREFERENCE_DEFAULT_SERIAL_PATH);

		preference = findPreference(Constants.ID_PREFERENCE_SERIAL_BAUDRATE);
		((EditTextPreference)(preference)).getEditText().setInputType(InputType.TYPE_CLASS_NUMBER);
		preference.setDefaultValue(Configs.ID_PREFERENCE_DEFAULT_SERIAL_BAUDRATE);

		preference = findPreference(Constants.ID_PREFERENCE_FACE_IMAGE_QUALITY);
		preference.setDefaultValue(Configs.ID_PREFERENCE_DEFAULT_FACE_IMAGE_QUALITY);
		preference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				preference.setSummary(newValue.toString());
				return true;
			}
		});

		App.Instance().PushActivity(this);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		App.Instance().PopActivity();
	}
}
