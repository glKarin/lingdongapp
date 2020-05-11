package com.youtushuju.lingdongapp.common;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;

public final class Common
{
	private Common(){}

	public static String TimestampToStr(long ts)
	{
		SimpleDateFormat format;

		format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		return format.format(new Date(ts));
	}

	public static String TimestampToDateStr(long ts)
	{
		SimpleDateFormat format;

		format = new SimpleDateFormat("yyyy-MM-dd");
		return format.format(new Date(ts));
	}

	public static String Now()
	{
		return TimestampToStr(System.currentTimeMillis());
	}

	public static int Rand()
	{
		return Rand(Integer.MIN_VALUE, Integer.MAX_VALUE);
	}

	public static int Rand(int min, int max)
	{
		int delta = max - min; // must min < max
		int bits = String.valueOf(delta).length();
		double d = Math.random();
		int i = (int)(d * Math.pow(10, bits));
		return (i % delta) + min;
	}

	public static String RandStr(int count)
	{
		final String Alphaset = "1234567890ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
		final int Length = Alphaset.length();
		StringBuffer buf = new StringBuffer();
		for(int i = 0; i < count; i++)
		{
			buf.append(Alphaset.charAt(Rand(0, Length)));
		}
		return buf.toString();
	}

	public static boolean StringIsEmpty(String in)
	{
		return in == null || in.isEmpty();
	}

	public static boolean StringIsBlank(String in)
	{
		return in == null || in.trim().isEmpty();
	}

	public static<T> boolean ArrayIsEmpty(T in[])
	{
		return in == null || in.length == 0;
	}

	/*StandardCharsets.ISO_8859_1
	StandardCharsets.UTF_8*/
	public static String ByteArray8BitsString(byte arr[])
	{
		try
		{
			return arr != null ? new String(arr, "ISO8859-1") : null;
		}
		catch (UnsupportedEncodingException e)
		{
			e.printStackTrace();
			//return null;
			return new String(arr);
		}
	}

	public static byte[] String8BitsByteArray(String str)
	{
		try
		{
			return str != null ? str.getBytes("ISO8859-1") : null;
		}
		catch (UnsupportedEncodingException e)
		{
			e.printStackTrace();
			//return null;
			return str.getBytes();
		}
	}
}
