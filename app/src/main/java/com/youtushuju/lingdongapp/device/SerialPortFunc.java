package com.youtushuju.lingdongapp.device;

public abstract class SerialPortFunc {
    protected boolean m_isOpened = false;
    protected OnDataReceivedListener m_onDataReceivedListener = null;
    protected String m_device = null;
    protected int m_baudrate = 0;

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
}
