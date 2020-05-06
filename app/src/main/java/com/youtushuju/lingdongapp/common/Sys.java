package com.youtushuju.lingdongapp.common;

import android.content.Context;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.InputStream;

public final class Sys
{
    private static final String ID_TAG = "Sys";
    private String m_kernelVersion;
    private String m_kernelBuildeInfo;
    private static Sys _sys;
    
    private Sys()
    {
        ParseKernelInfo();
    }
    
    public static Sys Instance()
    {
        if(_sys == null)
        {
            _sys = new Sys();
        }
        return _sys;
    }
  
    /*
     Linux version 4.9.112-perf+ (cm@cm-build-c99) (gcc version 4.9.x 20150123 (prerelease) (GCC) ) #1 SMP PREEMPT Thu Jun 20 17:57:25 CST 2019
    */
    private boolean ParseKernelInfo()
    {
        String Path = "/proc/version";
        String text;
        VarRef<byte []> data;
        boolean ok;

        data = new VarRef<byte []>();
        ok = ExecByRoot("cat " + Path, data);
        if(!ok)
            return false;
        text = new String(data.ref);

        if(TextUtils.isEmpty(text))
            return false;

        int index = text.indexOf("Linux version");
        int i = "Linux version".length();
        while(text.charAt(i + index) == ' ')
            i++;
        index = text.indexOf(" ", i);
        m_kernelVersion = text.substring(i, index);

        i = 0;
        while(text.charAt(i + index) == ' ')
            i++;
        m_kernelBuildeInfo = text.substring(i + index);

        return true;
    }
    
    public String KernelVersion()
    {
        return m_kernelVersion;
    }

    public String KernelBuildInfo()
    {
        return m_kernelBuildeInfo;
    }

    public static boolean ExecByRoot(String command, VarRef<byte[]> ref)
    {
        Process process = null;
        DataOutputStream os = null;
        InputStream is = null;
        ByteArrayOutputStream baos = null;

        try
        {
            process = Runtime.getRuntime().exec("su");
            os = new DataOutputStream(process.getOutputStream());
            os.writeBytes(command + "\n");
            os.writeBytes("exit\n");
            os.flush();
            process.waitFor();
            if(ref != null)
            {
                is = process.getInputStream();
                if(is != null)
                {
                    baos = new ByteArrayOutputStream();
                    int Buf_Size = 1024;
                    byte b[] = new byte[Buf_Size];
                    int len = 0;
                    while((len = is.read(b)) > 0)
                        baos.write(b, 0, len);
                    baos.flush();
                    byte res[] = baos.toByteArray();
                    ref.Ref(res);
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return false;
        }
        finally
        {
            try
            {
                if (os != null)
                    os.close();
                if(is != null)
                    is.close();
                if(baos != null)
                    baos.close();
                process.destroy();
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        return true;
    }

    public static final String GetIMEI(Context context) {
        String imei = null;

        try
        {
            TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);

            if(Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) // 棒棒糖
            {
                imei = telephonyManager.getDeviceId();
            }
            else
            {
                /*Method method = telephonyManager.getClass().getMethod("getImei");
                imei = (String) method.invoke(telephonyManager);*/
                imei = telephonyManager.getImei();
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            imei = "863412048794880"; // TODO: test
        }
        return imei;
    }
}
