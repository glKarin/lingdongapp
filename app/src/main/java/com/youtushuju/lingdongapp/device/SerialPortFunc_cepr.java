package com.youtushuju.lingdongapp.device;

import com.youtushuju.lingdongapp.common.Common;
import com.youtushuju.lingdongapp.common.Logf;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android_serialport_api.SerialPort;
import android_serialport_api.SerialPortFinder;

public final class SerialPortFunc_cepr extends SerialPortFunc {
    private static final String ID_TAG = "SerialPortFunc_cepr";
    public SerialPortFinder m_serialPortFinder = null;
    private SerialPort m_serialPort = null;
    private SerialThread m_thread = null;

    public SerialPortFunc_cepr()
    {
        super();
        m_serialPortFinder = new SerialPortFinder();
    }

    public boolean InitSerialPort(String path, int baudrate)
    {
        if(m_isOpened)
        {
            Logf.e(ID_TAG, "请先调用关闭函数");
            return false;
        }

        if(Common.StringIsBlank(path) || baudrate < 0)
            return false;
        if(m_serialPort == null)
        {
            try
            {
                m_serialPort = new SerialPort(new File(path), baudrate, 0);
            }
            catch(Exception e)
            {
                e.printStackTrace();
            }
        }
        m_device = path;
        m_baudrate = baudrate;
        Logf.d(ID_TAG, "串口读写初始化");
        return m_serialPort != null;
    }

    public void ShutdownSerialPort()
    {
        Close();
        if (m_serialPort != null)
        {
            m_serialPort.close();
            m_serialPort = null;
        }
        Logf.d(ID_TAG, "串口读写终止");
    }

    public boolean Open()
    {
        if(IsOpened())
        {
            Logf.e(ID_TAG, "请先调用关闭函数");
            return false;
        }
        if(!IsInited())
        {
            Logf.e(ID_TAG, "请先调用初始化函数");
            return false;
        }
        m_thread = new SerialThread(this /* 以父子关系访问, 内部不直接访问外部 */);
        m_thread.start();
        m_isOpened = true;
        Logf.d(ID_TAG, "串口读写打开");
        return true;
    }

    public boolean Close()
    {
        if(!m_isOpened)
            return true;
        if(m_thread != null)
        {
            m_thread.interrupt();
            m_thread.Destruction();
            m_thread = null;
        }
        m_isOpened = false;
        Logf.d(ID_TAG, "串口读写关闭");
        return true;
    }

    public boolean IsOpened()
    {
        return IsInited() && m_isOpened;
    }

    public boolean IsInited()
    {
        return m_serialPort != null;
    }

    public int Send(byte data[])
    {
        if(data == null || data.length == 0)
        {
            Logf.e(ID_TAG, "数据为空");
            return 0;
        }

        if(!IsOpened())
        {
            Logf.e(ID_TAG, "请先调用打开函数");
            return -1;
        }
        if (m_thread.input_stream == null)
        {
            Logf.e(ID_TAG, "串口写线程关闭");
            return -3;
        }

        try
        {
            m_thread.output_stream.write(data);
            m_thread.output_stream.flush();
            Logf.d(ID_TAG, "串口写入: " + new String(data));
            return data.length;
        }
        catch (IOException e)
        {
            e.printStackTrace();
            return -2;
        }
    }

    private class SerialThread extends Thread
    {
        private static final String ID_TAG = "SerialPortFunc_cepr::SerialThread";
        SerialPortFunc_cepr parent = null;
        InputStream input_stream = null;
        OutputStream output_stream = null;
        int buf_size = 64;

        public SerialThread(SerialPortFunc_cepr parent)
        {
            Logf.d(ID_TAG, "创建串口读写线程");
            this.parent = parent;
        }

        public void start()
        {
            input_stream = parent.m_serialPort.getInputStream();
            output_stream = parent.m_serialPort.getOutputStream();
            super.start();
            Logf.d(ID_TAG, "串口读写线程启动");
        }

        @Override
        public void interrupt() {
            super.interrupt();
            Logf.d(ID_TAG, "串口读写线程结束");
        }

        public void run()
        {
            while(!isInterrupted()) {
                if (input_stream == null)
                {
                    Logf.d(ID_TAG, "串口读线程关闭");
                    return;
                }
                ByteArrayOutputStream baos = null;

                try
                {
                    int len = 0;
                    byte[] buf = new byte[buf_size];
                    while((len = input_stream.read(buf)) > 0)
                    {
                        if(baos == null)
                            baos = new ByteArrayOutputStream();
                        baos.write(buf, 0, len);
                    }
                    if(baos != null)
                    {
                        baos.flush();
                        byte data[] = baos.toByteArray();
                        if(data != null && data.length > 0)
                        {
                            int size = data.length;
                            Logf.e(ID_TAG, "串口读取: " + new String(data));
                            if(parent.m_onDataReceivedListener != null) // if(m_onDataReceivedListener != null) // 分离父子关系
                                parent.m_onDataReceivedListener.OnDataReceived(data, size);
                        }
                    }
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                    Logf.e(ID_TAG, "读写错误");
                    return;
                }
                finally {
                    try
                    {
                        if(baos != null)
                            baos.close();
                    }
                    catch (IOException e)
                    {
                        e.printStackTrace();
                    }
                }
            }
        }

        public void Destruction()
        {
            input_stream = null;
            output_stream = null;
            Logf.d(ID_TAG, "串口读写线程销毁");
        }
    }
}
