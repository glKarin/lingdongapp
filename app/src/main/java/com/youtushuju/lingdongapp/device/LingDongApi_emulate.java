package com.youtushuju.lingdongapp.device;
import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.util.DisplayMetrics;
import android.view.WindowManager;
import android.widget.Toast;

import com.youtushuju.lingdongapp.common.FS;
import com.youtushuju.lingdongapp.common.Logf;
import com.youtushuju.lingdongapp.common.Sys;

public class LingDongApi_emulate extends LingDongApi
{
    private static final String ID_TAG = "灵动模拟测试";

    public LingDongApi_emulate(Context context)
    {
        super(context);
    }

    public boolean IsValid()
    {
        return true;
    }

    public String GetSystemApiVersion()
    {
        return "" + Build.VERSION.SDK_INT;
    }

    public String GetSystemAndroidOsVersion()
    {
        return android.os.Build.VERSION.RELEASE;
    }

    public String GetSystemKernelVersion()
    {
        return Sys.Instance().KernelVersion();
    }

    public String GetSystemDeviceModel()
    {
        return Build.MODEL;
    }

    public String GetSystemBuilderNumberDisplay()
    {
        return Sys.Instance().KernelBuildInfo();
    }

    public String GetSystemInternalStorageMemory()
    {
        StatFs stat;

        stat = new StatFs(Environment.getDataDirectory().getPath());
        return FS.FormatSize(stat.getTotalBytes());
    }

    public String GetSystemInternalFreeStorageMemory()
    {
        StatFs stat;

        stat = new StatFs(Environment.getDataDirectory().getPath());
        return FS.FormatSize(stat.getAvailableBytes());
    }

    public String GetSystemRunningMemory()
    {
        return FS.FormatSize(Runtime.getRuntime().totalMemory());
    }

    public String GetSystemPrimaryStoragePath()
    {
        return Environment.getDataDirectory().getPath();
    }

    public String GetSystemSdcardPath()
    {
        return Environment.getExternalStorageDirectory().getPath();
    }

    public String GetSystemUSBPath()
    {
        return Environment.getExternalStorageDirectory().getPath();
    }

    public void DeviceSleep()
    {
        EmulateControl("设备休眠");
    }

    public void DeviceWakeUp()
    {
        EmulateControl("设备休眠唤醒");
    }

    public void DeviceReset()
    {
        EmulateControl("设备恢复出厂设置");
    }

    public void DeviceShutdown()
    {
        EmulateControl("设备关机");
    }

    public void DeviceReboot()
    {
        EmulateControl("设备重启");
    }

    public void DeviceSetOrientation(int angle)
    {
        EmulateControl("设备设置屏幕方向: %d", angle);
    }

    public void DeviceSetBrightness(int brightness)
    {
        EmulateControl("设备设置屏幕亮度: %d", brightness);
    }

    public void DeviceSetLCDBlackLight(boolean on)
    {
        EmulateControl("设备设置开关屏幕背光: %b", on);
    }

    public void DeviceControlNavGesture(boolean enable)
    {
        EmulateControl("设备设置控制向上的手势显示导航栏: %b", enable);
    }

    private void EmulateControl(String type, Object... args)
    {
        Toast.makeText(m_context, String.format(type, args), Toast.LENGTH_LONG).show();
        Logf.d(ID_TAG, type, args);
    }

    public int GetSystemScreenWidth()
    {
        return GetMetrics().widthPixels;
    }

    public int GetSystemScreenHeight()
    {
        return GetMetrics().heightPixels;
    }

    private DisplayMetrics GetMetrics()
    {
        DisplayMetrics dm = null;
        if(m_context instanceof Activity)
        {
            WindowManager manager = ((Activity)m_context).getWindowManager();
            dm = new DisplayMetrics();
            manager.getDefaultDisplay().getMetrics(dm);
        }
        else
        {
            Resources resources = m_context.getResources();
            dm = resources.getDisplayMetrics();
        }
        return dm;
    }
}
