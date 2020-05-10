package com.youtushuju.lingdongapp.json;

public interface JsonResult
{
	public static final int JSON_RESULT_NULL = 0;
	public static final int JSON_RESULT_OBJECT = 1;
	public static final int JSON_RESULT_ARRAY = 2;

	public static final int JSON_VALUE_TYPE_UNDEFINED = 0;
	public static final int JSON_VALUE_TYPE_INTEGER = 1;
	public static final int JSON_VALUE_TYPE_STRING = 2;
	public static final int JSON_VALUE_TYPE_BOOL = 3;
	public static final int JSON_VALUE_TYPE_FLOAT = 4;
	public static final int JSON_VALUE_TYPE_ARRAY = 5;
	public static final int JSON_VALUE_TYPE_OBJECT = 6;

	public int GetJsonResultType();
}
