package com.youtushuju.lingdongapp.gui;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.view.View;
import android.webkit.MimeTypeMap;

import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;

import com.youtushuju.lingdongapp.FileBrowserActivity;
import com.youtushuju.lingdongapp.common.Logf;

import java.io.File;

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

    public static void OpenExternally(Activity activity, String path)
    {
        Uri uri = null;
        Intent intent = new Intent();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) // 牛轧糖以上
        {
            uri = FileProvider.getUriForFile(activity,activity.getApplicationContext().getPackageName() + ".file_provider", new File(path));
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        }
        else
        {
            uri = Uri.parse("file://" + path);
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setAction(Intent.ACTION_VIEW);

        String mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(MimeTypeMap.getFileExtensionFromUrl("file://" + path));
        intent.setDataAndType(uri, mimeType);
        activity.startActivity(intent);
    }

    public static void OpenAppSetting(Context context)
    {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", context.getPackageName(), null);
        intent.setData(uri);
        context.startActivity(intent);
    }

    public static boolean BuildOnDebug(Context context)
    {
        try
        {
            ApplicationInfo info = context.getApplicationInfo();
            return (info.flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return false; // default is release
        }
    }

    public static void HideNavBar(Activity activity)
    {
        View decorView = activity.getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_IMMERSIVE
                | View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);
    }

    public static int dp2px(Context context, float dp){
        float scale=context.getResources().getDisplayMetrics().density;
        return (int) (dp * scale + 0.5f);
    }

    public static int px2dp(Context context, float px){
        float scale = context.getResources().getDisplayMetrics().density;
        return (int) (px / scale + 0.5f);
    }

    public static int dp2px(Context context, int dp){
        float scale=context.getResources().getDisplayMetrics().density;
        return (int)((float)dp * scale + 0.5f);
    }

    public static int px2dp(Context context, int px){
        float scale = context.getResources().getDisplayMetrics().density;
        return (int)((float)px / scale + 0.5f);
    }
}
