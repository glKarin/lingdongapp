package com.youtushuju.lingdongapp.device;
import android.app.SmatekManager;
import android.content.Context;

public class LingDongApi_real extends LingDongApi
{
    private static final String ID_TAG = "LingDongApi_real";
    private static final String ID_SMATEK_MANAGER_SERVICE_NAME = "smatek";
    private SmatekManager m_smatekManager;

    public LingDongApi_real(Context context)
    {
        super(context);
    }

    public boolean Setup()
    {
        if(m_context == null)
            return false;

        m_smatekManager = (SmatekManager) m_context.getSystemService(ID_SMATEK_MANAGER_SERVICE_NAME);

        return IsValid();
    }

    public boolean IsValid()
    {
        return m_smatekManager != null;
    }

    public String GetSystemApiVersion()
    {
        if(!IsValid())
            return null;
        return m_smatekManager.getAPIVersion();
    }

    public String GetSystemAndroidOsVersion()
    {
        if(!IsValid())
            return null;
        return m_smatekManager.getAndroidOSVersion();
    }

    public String GetSystemKernelVersion()
    {
        if(!IsValid())
            return null;
        return m_smatekManager.getKernelVersion();
    }

    public String GetSystemDeviceModel()
    {
        if(!IsValid())
            return null;
        return m_smatekManager.getDeviceModel();
    }

    public String GetSystemBuilderNumberDisplay()
    {
        if(!IsValid())
            return null;
        return m_smatekManager.getBuilderNumberDisplay();
    }

    public String GetSystemInternalStorageMemory()
    {
        if(!IsValid())
            return null;
        return m_smatekManager.getInternalStorageMemory();
    }

    public String GetSystemInternalFreeStorageMemory()
    {
        if(!IsValid())
            return null;
        return m_smatekManager.getInternalFreeStorageMemory();
    }

    public String GetSystemRunningMemory()
    {
        if(!IsValid())
            return null;
        return m_smatekManager.getRunningMemory();
    }

    public String GetSystemPrimaryStoragePath()
    {
        if(!IsValid())
            return null;
        return m_smatekManager.getPrimaryStoragePath();
    }

    public String GetSystemSdcardPath()
    {
        if(!IsValid())
            return null;
        return m_smatekManager.getSdcardPath();
    }

    public String GetSystemUSBPath()
    {
        if(!IsValid())
            return null;
        return m_smatekManager.getUSBPath();
    }

    public void DeviceSleep()
    {
        if(!IsValid())
            return;
        m_smatekManager.goToSleep();
    }

    public void DeviceWakeUp()
    {
        if(!IsValid())
            return;
        m_smatekManager.wakeUp();
    }

    public void DeviceReset()
    {
        if(!IsValid())
            return;
        m_smatekManager.eraseAllData();
    }

    public void DeviceShutdown()
    {
        if(!IsValid())
            return;
        m_smatekManager.shutdown();
    }

    public void DeviceReboot()
    {
        if(!IsValid())
            return;
        m_smatekManager.reboot();
    }

    public void DeviceSetOrientation(int angle)
    {
        if(!IsValid())
            return;
        m_smatekManager.setRotation("" + angle);
    }

    public void DeviceSetBrightness(int brightness)
    {
        if(!IsValid())
            return;
        m_smatekManager.setBrightness(brightness);
    }

    public void DeviceSetLCDBlackLight(boolean on)
    {
        if(!IsValid())
            return;
        m_smatekManager.setLcdBlackLight(on);
    }
}
