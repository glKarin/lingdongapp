package com.youtushuju.lingdongapp.common;
import java.io.*;

public final class FS
{
	public static final long KB = 1024L; // 1000
	public static final long MB = KB * KB;
	public static final long GB = MB * KB;
	public static final long TB = GB * KB;
	
	private FS() {}
	
	public static int rm(String path)
	{
		if(path == null || path.isEmpty())
			return 0;
		File file = new File(path);
		return rm(file);
	}
	
	public static int rm(File file)
	{
		if(file == null)
			return 0;
		if(file.isDirectory())
		{
			File files[] = file.listFiles();
			int num = 0;
			for(File f : files)
			{
				num += rm(f);
			}
			if(file.delete())
				num++;
			return num;
		}
		else if(file.isFile())
		{
			return file.delete() ? 1 : 0;
		}
		return 0;
	}
	
	public static long du(String path)
	{
		if(path == null || path.isEmpty())
			return 0;
		File file = new File(path);
		return du(file);
	}

	public static long du(File file)
	{
		if(file == null)
			return 0;
		if(file.isDirectory())
		{
			File files[] = file.listFiles();
			long size = 0;
			for(File f : files)
			{
				size += du(f);
			}
			size += file.length();
			return size;
		}
		else if(file.isFile())
		{
			return file.length();
		}
		return 0;
	}
	
	public static boolean mv(String dst, String src)
	{
		return false;
	}

	public static boolean cp(String dst, String src)
	{
		return false;
	}
	
	public static String FormatSize(long size)
	{
		if(size < KB)
			return size + " B";
		if(size < MB)
			return String.format("%.1f K", ((double)size / (double)KB));
		if(size < GB)
			return String.format("%.1f M", ((double)size / (double)MB));
		if(size < TB)
			return String.format("%.1f G", ((double)size / (double)GB));
		return String.format("%.1f T", ((double)size / (double)TB));
	}
}
