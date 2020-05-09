package com.youtushuju.lingdongapp.device;

import com.youtushuju.lingdongapp.common.Common;
import com.youtushuju.lingdongapp.common.Logf;

public final class SerialPortFunc_test extends SerialPortFunc {
    private static final String ID_TAG = "SerialPortFunc_test";
    private TestThread m_thread = null;
    private String m_lastData = null;
    private boolean m_inited = false;

    public SerialPortFunc_test()
    {
        super();
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
        m_device = path;
        m_baudrate = baudrate;
        m_inited = true;
        Logf.d(ID_TAG, "串口读写初始化");
        return true;
    }

    public void ShutdownSerialPort()
    {
        Close();
        m_inited = false;
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
        m_thread = new TestThread(this /* 以父子关系访问, 内部不直接访问外部 */);
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
        return m_inited;
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

        m_lastData = new String(data);
        return m_lastData.length();
    }

    private class TestThread extends Thread
    {
        private static final String ID_TAG = "SerialPortFunc_test::TestThread";
        SerialPortFunc_test parent = null;

        public TestThread(SerialPortFunc_test parent)
        {
            Logf.d(ID_TAG, "创建串口读写线程");
            this.parent = parent;
        }

        public void start()
        {
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
                if(parent.m_lastData != null && parent.m_onDataReceivedListener != null)
                {
                    byte data[] = m_lastData.getBytes();
                    parent.m_onDataReceivedListener.OnDataReceived(data, data.length);
                    parent.m_lastData = null;
                }
            }
        }
    }
}