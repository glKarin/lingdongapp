package com.youtushuju.lingdongapp.device;

import com.youtushuju.lingdongapp.common.Logf;

public abstract class SerialPortFunc {
    protected boolean m_isOpened = false;
    protected OnDataReceivedListener m_onDataReceivedListener = null;
    protected String m_device = null;
    protected int m_baudrate = 0;
    protected String m_buffer = "";

    public abstract boolean InitSerialPort(String path, int baudrate);
    public abstract void ShutdownSerialPort();
    public abstract boolean Open();
    public abstract boolean Close();
    public abstract boolean IsInited();
    public abstract int Send(byte data[]);

    public interface OnDataReceivedListener
    {
        public void OnDataReceived(final byte data[], final int length);
    }

    public SerialPortFunc SetOnDataReceivedListener(OnDataReceivedListener l)
    {
        m_onDataReceivedListener = l;
        return this;
    }

    public boolean IsOpened()
    {
        return m_isOpened;
    }

    protected void SetOpened(boolean on)
    {
        m_isOpened = on;
    }

    protected void RecvBuffer(byte data[], int length)
    {
        if(data == null || length == 0)
            return;
        synchronized(m_buffer)
        {
            m_buffer += new String(data, 0, length);
        }
    }

    public String ReadBuffer()
    {
        synchronized(m_buffer)
        {
            if(m_buffer == null || m_buffer.isEmpty())
                return null;
            String ret = m_buffer;
            m_buffer = "";
            return ret;
        }
    }

    public void CleanBuffer()
    {
        synchronized(m_buffer)
        {
            m_buffer = "";
        }
    }
}
