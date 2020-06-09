package com.youtushuju.lingdongapp;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.youtushuju.lingdongapp.gui.App;

public class SettingsActivity extends AppCompatActivity
{
	private static final String ID_TAG = "SettingsActivity";

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
