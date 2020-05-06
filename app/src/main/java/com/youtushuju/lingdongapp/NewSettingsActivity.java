package com.youtushuju.lingdongapp;

import android.os.Bundle;
import android.text.InputType;

import androidx.appcompat.app.AppCompatActivity;

import com.youtushuju.lingdongapp.common.Configs;
import com.youtushuju.lingdongapp.common.Constants;
import com.youtushuju.lingdongapp.gui.App;

public class NewSettingsActivity extends AppCompatActivity
{
	private static final String ID_TAG = "NewSettingsActivity";

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		//setContentView(R.layout.settings_page);
		getSupportFragmentManager().beginTransaction().replace(android.R.id.content, new SettingsFragment()).commit();
		App.Instance().PushActivity(this);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		App.Instance().PopActivity();
	}
}
