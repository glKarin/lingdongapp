package com.youtushuju.lingdongapp.device;

import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;

import com.smatek.uart.UartComm;
import com.youtushuju.lingdongapp.common.Common;
import com.youtushuju.lingdongapp.common.Logf;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android_serialport_api.SerialPort;
import android_serialport_api.SerialPortFinder;

public final class SerialPortFunc_uc extends SerialPortFunc {
    private static final String ID_TAG = "SerialPortFunc_uc";
    private boolean mIsRS485 = false;
    private UartComm UC = null;
    private int uart_fd = -1;
    private int parityCheck = 0; // 1 2
    private boolean mRunning = false;
    private HandlerThread thread = null;
    private Handler mRecvHandler = null;
    private boolean mNeedSendData = false;

    public SerialPortFunc_uc()
    {
        super();
        UC = new UartComm();
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
        if(uart_fd < 0)
        {
            Logf.d(ID_TAG, "UART初始化");
            uart_fd = UC.uartInit(path);
            Logf.d(ID_TAG, "文件描述符(%s)", uart_fd);
            UC.setOpt(uart_fd, baudrate, 8, parityCheck, 1);
            Logf.d(ID_TAG, "UART设置参数");
        }
        m_device = path;
        m_baudrate = baudrate;
        Logf.d(ID_TAG, "串口读写初始化");
        return uart_fd >= 0;
    }

    public void ShutdownSerialPort()
    {
        Close();
        if (uart_fd >= 0)
        {
            UC.uartDestroy(uart_fd);
            uart_fd = -1;
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
        mRunning = true;
        thread = new HandlerThread("UartRecvThread");
        thread.start();//创建一个HandlerThread并启动它
        mRecvHandler = new Handler(thread.getLooper());//使用HandlerThread的looper对象创建Handler，如果使用默认的构造方法，很有可能阻塞UI线程
        mRecvHandler.post(mRecvRunnable);//将线程post到Handler中
        m_isOpened = true;
        mNeedSendData = false;
        Logf.d(ID_TAG, "串口读写打开");
        return true;
    }

    public boolean Close()
    {
        if(!m_isOpened)
            return true;
        mRunning = false;
        if(thread != null)
        {
            mRecvHandler.removeCallbacks(mRecvRunnable);
            mRecvHandler = null;
            thread.quit();
            thread = null;
        }
        m_isOpened = false;
        mNeedSendData = false;
        CleanBuffer();
        Logf.d(ID_TAG, "串口读写关闭");
        return true;
    }

    public boolean IsOpened()
    {
        return IsInited() && m_isOpened;
    }

    public boolean IsInited()
    {
        return uart_fd >= 0;
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

        if(mIsRS485)
            UC.setRS485WriteRead(0);
        int intData[] = new int[data.length];
        for (int i = 0; i < data.length; i++)
            intData[i] = data[i]; // TODO: 直接修改C函数实现
        Log.d(ID_TAG, "will send data " + data.length);
        int len = UC.send(uart_fd, intData, intData.length);
        //mNeedSendData = false;
        //set RS485 to receive data mode by default.
        if(mIsRS485)
            UC.setRS485WriteRead(1);

        Logf.d(ID_TAG, "串口写入: " + new String(data));
        mNeedSendData = false; // 标记已发送
        return len;
    }

    //实现耗时操作的线程
    Runnable mRecvRunnable = new Runnable() {
        SerialPortFunc_uc parent = SerialPortFunc_uc.this;
        int buf_size = 256; // 常量 线程安全

        @Override
        public void run() {
            while(mRunning){
                if(mNeedSendData == true) {
                    // 在主线程发送， 发送为同步
                } else {
                    ByteArrayOutputStream baos = null;
                    int len = 0;
                    int[] buffer = new int[buf_size];

                    if(mIsRS485)
                        UC.setRS485WriteRead(1);
                    while((len = UC.recv(uart_fd, buffer, buf_size)) > 0)
                    {
                        if(baos == null)
                            baos = new ByteArrayOutputStream();
                        byte buf[] = new byte[len];
                        for (int i = 0; i < len; i++)
                            buf[i] = (byte)(buffer[i] & 0xff);
                        baos.write(buf, 0, len);
                    }
                    if(baos != null)
                    {
                        try
                        {
                            baos.flush();
                            byte data[] = baos.toByteArray();
                            if (data != null && data.length > 0) {
                                int size = data.length;
                                Logf.e(ID_TAG, "串口读取: " + new String(data));
                                RecvBuffer(data, size);
                                if (parent.m_onDataReceivedListener != null) // if(m_onDataReceivedListener != null) // 分离父子关系
                                    parent.m_onDataReceivedListener.OnDataReceived(data, size);
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
                                baos.close();
                            }
                            catch (IOException e)
                            {
                                e.printStackTrace();
                            }
                        }
                    }
                    mNeedSendData = true; // 标记已接收
                } //end else
            } //end while
        }
    };
}
