package com.youtushuju.lingdongapp;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Debug;
import android.os.Process;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import com.youtushuju.lingdongapp.common.Configs;
import com.youtushuju.lingdongapp.common.Constants;
import com.youtushuju.lingdongapp.common.Logf;
import com.youtushuju.lingdongapp.device.LingDongApi;
import com.youtushuju.lingdongapp.gui.App;

import java.util.Timer;
import java.util.TimerTask;

public class AutoBootReceiver extends BroadcastReceiver {
    private static final String ID_TAG = "AutoBootReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        boolean autoboot = PreferenceManager.getDefaultSharedPreferences(context).getBoolean(Constants.ID_PREFERENCE_AUTO_BOOT, Configs.ID_PREFERENCE_DEFAULT_AUTO_BOOT);
        if(!autoboot)
        {
            Log.i(ID_TAG, "Do not auto start after system boot.");
            return;
        }
        if(Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction()))
        {
            Intent thisIntent = new Intent(context, SplashActivity.class);
            thisIntent.setAction("android.intent.action.MAIN");
            thisIntent.addCategory("android.intent.category.LAUNCHER");
            thisIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(thisIntent);
            Log.i(ID_TAG, "Auto start!");
        }
    }
}
