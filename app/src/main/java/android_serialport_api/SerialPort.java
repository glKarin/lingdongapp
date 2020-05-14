/*
 * Copyright 2009 Cedric Priscal
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License. 
 */

package android_serialport_api;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.util.Log;

import androidx.annotation.NonNull;

import com.youtushuju.lingdongapp.common.Logf;

public class SerialPort {

	private static final String TAG = "SerialPort";

	public static final int ID_IO_WAY_JAVA_IO = 0;
	public static final int ID_IO_WAY_C = 1;

	/*
	 * Do not remove or rename the field mFd: it is used by native method close();
	 */
	private FileDescriptor mFd;
	private FileInputStream mFileInputStream = null;
	private FileOutputStream mFileOutputStream = null;

	private int m_ioWay = ID_IO_WAY_C;

	public SerialPort(File device, int baudrate, int flags) throws SecurityException, IOException {

		/* Check access permission */
		if (!device.canRead() || !device.canWrite()) {
			try {
				/* Missing read/write permission, trying to chmod the file */
				Process su;
				su = Runtime.getRuntime().exec("/sbin/su");
				String cmd = "chmod 666 " + device.getAbsolutePath() + "\n"
						+ "exit\n";
				su.getOutputStream().write(cmd.getBytes());
				if ((su.waitFor() != 0) || !device.canRead()
						|| !device.canWrite()) {
					Logf.e(TAG, "SerialPort(%s, %d, %x): 以超级用户执行命令(%s)错误", device.getPath(), baudrate, flags, "chmod 666 " + device.getAbsolutePath() + "\n"
							+ "exit\n");
					throw new SecurityException();
				}
			} catch (Exception e) {
				e.printStackTrace();
				throw new SecurityException();
			}
		}

		mFd = open(device.getAbsolutePath(), baudrate, flags);
		if (mFd == null) {
			Log.e(TAG, "native open returns null");
			Logf.e(TAG, "open(%s, %d, %x): 本地打开文件错误", device.getPath(), baudrate, flags);
			throw new IOException();
		}
		Logf.e(TAG, "open(%s, %d, %x): 本地打开文件成功, 文件描述符(%s)", device.getPath(), baudrate, flags, mFd.toString());
		if(m_ioWay == ID_IO_WAY_JAVA_IO)
			mFileInputStream = new FileInputStream(mFd);
		else
			mFileInputStream = null; // call Recv()

		mFileOutputStream = new FileOutputStream(mFd);
	}

	// Getters and setters
	public InputStream getInputStream() {
		return mFileInputStream;
	}

	public OutputStream getOutputStream() {
		return mFileOutputStream;
	}

	public void Shutdown()
	{
		close();
		mFd = new FileDescriptor();
		// mFd = null;
	}

	public boolean DeviceOpened()
	{
		return mFd != null && mFd.valid();
	}

	public byte[] Recv_java_io()
	{
		if(mFileInputStream == null)
			return null;

		try
		{
			int length = mFileInputStream.available();
			if(length == 0)
				return null;
			byte ret[] = new byte[length];
			int rlen = mFileInputStream.read(ret, 0, length);
			if(rlen != length)
			{
				// ignore?
				return null;
			}
			return ret;
		}
		catch (IOException e)
		{
			e.printStackTrace();
			return null;
		}
	}

	public int Recv_java_io(@NonNull byte ret[])
	{
		if(mFileInputStream == null)
			return -1;

		try
		{
			int length = mFileInputStream.available();
			if(length == 0)
				return 0;
			int rlen = mFileInputStream.read(ret, 0, ret.length);
			if(rlen != ret.length)
			{
				// ignore?
				return -2;
			}
			return rlen;
		}
		catch (IOException e)
		{
			e.printStackTrace();
			return -3;
		}
	}

	public int Send_java_io(@NonNull byte data[])
	{
		if(mFileOutputStream == null)
			return -1;

		if(data.length == 0)
			return 0;
		try
		{
			mFileOutputStream.write(data, 0, data.length);
			mFileOutputStream.flush();
			return data.length;
		}
		catch (IOException e)
		{
			e.printStackTrace();
			return -2;
		}
	}

	// JNI
	private native static FileDescriptor open(String path, int baudrate, int flags);
	public native void close();
	static {
		System.loadLibrary("serial_port");
	}

	// karin
	public native int Recv(byte data[], int length, int timeout_second);
	public native int Send(byte data[], int length);
	protected native int GetFD();
}
