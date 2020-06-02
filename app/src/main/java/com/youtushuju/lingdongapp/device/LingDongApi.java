package com.youtushuju.lingdongapp.device;
import android.content.Context;

public abstract class LingDongApi
{
    protected Context m_context;
    
    public LingDongApi(Context context)
    {
        m_context = context;
    }

    public abstract boolean IsValid();
    public abstract String GetSystemApiVersion();
    public abstract String GetSystemAndroidOsVersion();
    public abstract String GetSystemKernelVersion();
    public abstract String GetSystemDeviceModel();
    public abstract String GetSystemBuilderNumberDisplay();
    public abstract String GetSystemInternalStorageMemory();
    public abstract String GetSystemInternalFreeStorageMemory();
    public abstract String GetSystemRunningMemory();

    public abstract String GetSystemPrimaryStoragePath();
    public abstract String GetSystemSdcardPath();
    public abstract String GetSystemUSBPath();

    public abstract int GetSystemScreenWidth();
    public abstract int GetSystemScreenHeight();

    public abstract void DeviceSleep();
    public abstract void DeviceWakeUp();
    public abstract void DeviceReset();
    public abstract void DeviceShutdown();
    public abstract void DeviceReboot();
    public abstract void DeviceSetOrientation(int angle);
    public abstract void DeviceSetBrightness(int brightness); // 0 - 255
    public abstract void DeviceSetLCDBlackLight(boolean on);
    public abstract void DeviceControlNavGesture(boolean enable);
    
    public LingDongSystemInfo GetSystemInfo()
    {
        LingDongSystemInfo systemInfo;

        systemInfo = new LingDongSystemInfo();
        systemInfo.api_version = GetSystemApiVersion();
        systemInfo.android_os_version = GetSystemAndroidOsVersion();
        systemInfo.device_model = GetSystemDeviceModel();
        systemInfo.kernel_version = GetSystemKernelVersion();
        systemInfo.builder_number_display = GetSystemBuilderNumberDisplay();
        systemInfo.running_memory = GetSystemRunningMemory();
        systemInfo.internal_free_storage_memory = GetSystemInternalFreeStorageMemory();
        systemInfo.internal_storage_memory = GetSystemInternalStorageMemory();

        return systemInfo;
    }
}
