package com.youtushuju.lingdongapp.gui;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.app.ActivityCompat;

import com.youtushuju.lingdongapp.common.Logf;

public final class ActivityUtility {
    private static final String ID_TAG = "ActivityUtility";
    public static final int ID_REQUEST_PERMISSION_RESULT = 1;
    private ActivityUtility(){}

    // 判断是否声明权限
    public static boolean IsGrantPermission(Activity activity, String permission)
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) // 棉花糖以上
        {
            return activity.checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED;
        }
        else
        {
            return true;
        }
    }

    // 请求权限
    public static boolean RequestPermission(Activity activity, String permission)
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) // 棉花糖以上
        {
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity, permission))
            {
                return false; // 用户拒绝授权, 并且希望不要再提示?
            }
            ActivityCompat.requestPermissions(activity, new String[] { permission }, ID_REQUEST_PERMISSION_RESULT);
            return true;
        }
        return false;
    }

    public static boolean CheckPermission(Activity activity, String permission)
    {
        if(IsGrantPermission(activity, permission))
            return true;
        RequestPermission(activity, permission);
        return IsGrantPermission(activity, permission);
    }
}
