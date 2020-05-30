package com.youtushuju.lingdongapp.json;
import java.util.*;
import org.json.*;
import java.lang.reflect.*;

public final class JSON
{
	private JSON() {}
	
	public static JsonResult Parse(String str)
	{
		JsonResult ret = null;
		
		try
		{
			JSONObject json = new JSONObject(str);
			
			if(json != null)
			{
				ret = ParseObject(json);
			}
		}
		catch(Exception e)
		{
			try
			{
				JSONArray json = new JSONArray(str);

				if(json != null)
				{
					ret = ParseArray(json);
				}
			}
			catch(Exception se)
			{
			}
		}
		
		return ret;
	}

	public static String Stringify(Object obj)
	{
		String ret = null;
		Object o;

		if(obj == null)
			return null;
		try
		{
			if(obj instanceof JsonMap)
			{
				o = FormatObject((JsonMap)obj);
				if(o != null && !JSONObject.NULL.equals(o))
				{
					ret = ((JSONObject)o).toString();
				}
			}
			if(obj instanceof Map)
			{
				o = FormatObject((Map)obj);
				if(o != null && !JSONObject.NULL.equals(o))
				{
					ret = ((JSONObject)o).toString();
				}
			}
			else if(obj instanceof JsonList)
			{
				o = FormatArray((JsonList) obj);
				if(o != null && !JSONObject.NULL.equals(o))
				{
					ret = ((JsonList)o).toString();
				}
			}
			else if(obj instanceof List)
			{
				o = FormatArray((List) obj);
				if(o != null && !JSONObject.NULL.equals(o))
				{
					ret = ((JsonList)o).toString();
				}
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}

		return ret;
	}

	public static String Format(Object obj)
	{
		return Stringify(obj);
	}

	// parse
	private static JsonMap ParseObject(JSONObject object) throws Exception
	{
		if(object == null)
			return null;

		JsonMap ret = new JsonMap();
		Iterator<String> itor = object.keys();
		while(itor.hasNext())
		{
			String name = itor.next();
			Object value = object.get(name);
			if(value instanceof JSONObject)
				ret.Put(name, ParseObject((JSONObject)value));
			else if(value instanceof JSONArray)
				ret.Put(name, ParseArray((JSONArray)value));
			else if(JSONObject.NULL.equals(value))
				ret.Put(name, null);
			else
				ret.Put(name, value);
		}
		return ret;
	}

	private static JsonList ParseArray(JSONArray array) throws Exception
	{
		if(array == null)
			return null;

		JsonList ret = new JsonList();
		int len = array.length();
		for(int i = 0; i < len; i++)
		{
			Object value = array.get(i);
			if(value instanceof JSONObject)
				ret.Put(ParseObject((JSONObject)value));
			else if(value instanceof JSONArray)
				ret.Put(ParseArray((JSONArray)value));
			else if(JSONObject.NULL.equals(value))
				ret.Put(null);
			else
				ret.Put(value);
		}
		return ret;
	}

	// format
	private static Object FormatObject(JsonMap object) throws Exception
	{
		if(object == null)
			return JSONObject.NULL;

		JSONObject ret = new JSONObject();
		String keys[] = object.GetKeys();
		for (String name : keys)
		{
			Object value = object.Get(name);
			if(value instanceof JsonMap)
				ret.put(name, FormatObject((JsonMap)value));
			else if(value instanceof Map)
				ret.put(name, FormatObject((Map)value));
			else if(value instanceof JsonList)
				ret.put(name, FormatArray((JsonList)value));
			else if(value instanceof List)
				ret.put(name, FormatArray((List)value));
			else if(value instanceof Object[])
				ret.put(name, FormatArray((Object[])value));
			else if(JSONObject.NULL.equals(value))
				ret.put(name, null);
			else
				ret.put(name, value);
		}
		return ret;
	}

	private static Object FormatArray(JsonList array) throws Exception
	{
		if(array == null)
			return JSONObject.NULL;

		JSONArray ret = new JSONArray();
		int len = array.Length();
		for(int i = 0; i < len; i++)
		{
			Object value = array.Get(i);
			if(value instanceof JsonMap)
				ret.put(FormatObject((JsonMap)value));
			else if(value instanceof Map)
				ret.put(FormatObject((Map)value));
			else if(value instanceof JsonList)
				ret.put(FormatArray((JsonList)value));
			else if(value instanceof List)
				ret.put(FormatArray((List)value));
			else if(value instanceof Object[])
				ret.put(FormatArray((Object[])value));
			else if(JSONObject.NULL.equals(value))
				ret.put(null);
			else
				ret.put(value);
		}
		return ret;
	}

	private static Object FormatObject(Map object) throws Exception
	{
		if(object == null)
			return JSONObject.NULL;

		JSONObject ret = new JSONObject();
		Set<Object> keys = object.keySet();
		for (Object o : keys)
		{
			String name = o.toString();
			Object value = object.get(name);
			if(value instanceof JsonMap)
				ret.put(name, FormatObject((JsonMap)value));
			else if(value instanceof Map)
				ret.put(name, FormatObject((Map)value));
			else if(value instanceof JsonList)
				ret.put(name, FormatArray((JsonList)value));
			else if(value instanceof List)
				ret.put(name, FormatArray((List)value));
			else if(value instanceof Object[])
				ret.put(name, FormatArray((Object[])value));
			else if(JSONObject.NULL.equals(value))
				ret.put(name, null);
			else
				ret.put(name, value);
		}
		return ret;
	}

	private static Object FormatArray(List array) throws Exception
	{
		if(array == null)
			return JSONObject.NULL;

		JSONArray ret = new JSONArray();
		for(Object value : array)
		{
			if(value instanceof JsonMap)
				ret.put(FormatObject((JsonMap)value));
			else if(value instanceof Map)
				ret.put(FormatObject((Map)value));
			else if(value instanceof JsonList)
				ret.put(FormatArray((JsonList)value));
			else if(value instanceof List)
				ret.put(FormatArray((List)value));
			else if(value instanceof Object[])
				ret.put(FormatArray((Object[])value));
			else if(JSONObject.NULL.equals(value))
				ret.put(null);
			else
				ret.put(value);
		}
		return ret;
	}

	private static Object FormatArray(Object array[]) throws Exception
	{
		if(array == null)
			return JSONObject.NULL;

		JSONArray ret = new JSONArray();
		for(Object value : array)
		{
			if(value instanceof JsonMap)
				ret.put(FormatObject((JsonMap)value));
			else if(value instanceof Map)
				ret.put(FormatObject((Map)value));
			else if(value instanceof JsonList)
				ret.put(FormatArray((JsonList)value));
			else if(value instanceof List)
				ret.put(FormatArray((List)value));
			else if(value instanceof Object[])
				ret.put(FormatArray((Object[])value));
			else if(JSONObject.NULL.equals(value))
				ret.put(null);
			else
				ret.put(value);
		}
		return ret;
	}
}