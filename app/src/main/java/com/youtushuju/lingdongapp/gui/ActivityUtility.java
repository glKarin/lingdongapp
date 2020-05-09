package com.youtushuju.lingdongapp.gui;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
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
}
