package com.youtushuju.lingdongapp.common;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Environment;

import androidx.annotation.NonNull;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;

// 优先加载扩展存储的文件, 其次是apk包的assets文件夹
public final class FD {
    private static final String ID_TAG = "FD";
    public static final int ENUM_FD_NONE = 0;
    public static final int ENUM_FD_ASSET = 1;
    public static final int ENUM_FD_FS = 2;

    private AssetFileDescriptor m_afd;
    private FileDescriptor m_fd;
    private FileInputStream m_fis;
    private int m_fdType = ENUM_FD_NONE;
    private String m_fsPath = "";
    private String m_path = null;

    public boolean Open(Context context, String filename)
    {
        boolean ok;

        SetFDType(ENUM_FD_NONE);
        m_path = null;
        ok = OpenFD(context, filename);
        if(ok)
        {
            SetFDType(ENUM_FD_FS);
            m_path = filename;
            return true;
        }
        ok = OpenAssetFD(context, filename);
        if(ok)
        {
            SetFDType(ENUM_FD_ASSET);
            m_path = filename;
            return true;
        }
        return false;
    }

    private boolean OpenFD(Context context, String filename)
    {
        FileDescriptor fd = null;
        FileInputStream fis = null;
        String path = GetFSFilePath(filename);
        File file = new File(path);

        //Logf.e(ID_TAG, file);
        if(!file.exists())
            return false;

        try
        {
            fis = new FileInputStream(file);

            m_fis = fis;
            m_fd = fis.getFD();
            Logf.e(ID_TAG, "打开扩展存储文件: " + m_fd.toString());
            return true;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            if(fis != null)
            {
                try
                {
                    fis.close();
                }
                catch (Exception ex)
                {
                    ex.printStackTrace();
                }
            }
            return false;
        }
    }

    private void CloseFD()
    {
        if(m_fd != null)
            m_fd = null;
        if(m_fis != null)
        {
            try
            {
                Logf.e(ID_TAG, "关闭扩展存储文件: " + m_fd.toString());
                m_fis.close();
            }
            catch (Exception ex)
            {
                ex.printStackTrace();
            }
            finally {
                m_fis = null;
            }
        }
    }

    private boolean OpenAssetFD(Context context, String filename)
    {
        AssetFileDescriptor afd = null;
        try
        {
            afd = context.getAssets().openFd(filename);
            m_afd = afd;
            Logf.e(ID_TAG, "打开apk Asset文件: " + m_afd.toString());
            return true;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return false;
        }
    }

    private void CloseAssetFD()
    {
        if(m_afd != null)
        {
            try
            {
                Logf.e(ID_TAG, "关闭apk Asset文件: " + m_afd.toString());
                m_afd.close();
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
            finally {
                m_fd = null;
            }
        }
    }

    public void Close()
    {
        SetFDType(ENUM_FD_NONE);
        m_path = null;
        CloseFD();
        CloseAssetFD();
    }

    private void SetFDType(int type)
    {
        m_fdType = type;
    }

    public int FDType()
    {
        return m_fdType;
    }

    public Object FD()
    {
        switch(m_fdType)
        {
            case ENUM_FD_FS:
                return m_fd;
            case ENUM_FD_ASSET:
                return m_afd;
            default:
                return null;
        }
    }

    public<T> T GetFD()
    {
        return (T)FD();
    }

    public void SetFSPath(String path)
    {
        m_fsPath = path != null ? path : "";
    }

    private String GetFSFilePath(String filename)
    {
        String path = Environment.getExternalStorageDirectory().getPath() + File.separator + m_fsPath;
        if(!path.endsWith("/") && !filename.startsWith("/"))
            path += File.separator;
        path += filename;
        return path;
    }

    public String Path()
    {
        return m_path;
    }

    public static FD Open(Context context, String filename, String dir)
    {
        FD fd;
        boolean ok;

        fd = new FD();
        fd.SetFSPath(dir);
        ok = fd.Open(context, filename);
        return ok ? fd : null;
    }

    @NonNull
    @Override
    public String toString() {
        switch(m_fdType)
        {
            case ENUM_FD_FS:
                return "扩展存储: " + (m_fd != null ? m_fd.toString() : "null");
            case ENUM_FD_ASSET:
                return "应用Asset: " + (m_afd != null ? m_afd.toString() : "null");
            default:
                return "无效";
        }
    }

    public boolean IsValid()
    {
        return m_fdType != ENUM_FD_NONE;
    }

    public static boolean LoadPlayerAsMedia(FD fd, MediaPlayer player)
    {
        if(fd == null || player == null)
            return false;
        Object obj = fd.FD();
        int type = fd.FDType();
        try
        {
            if(type == ENUM_FD_ASSET)
            {
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) // 牛轧糖
                    player.setDataSource((AssetFileDescriptor)obj);
                else
                    return false;
            }
            else
                player.setDataSource((FileDescriptor)obj);
            return true;
        }
        catch (IOException e)
        {
            e.printStackTrace();
            return false;
        }
    }
}
