package com.youtushuju.lingdongapp.json;

public interface JsonResult
{
	public static final int JSON_RESULT_NULL = 0;
	public static final int JSON_RESULT_OBJECT = 1;
	public static final int JSON_RESULT_ARRAY = 2;
	
	public int GetJsonResultType();
}
