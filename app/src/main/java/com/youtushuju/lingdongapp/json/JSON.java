package com.youtushuju.lingdongapp.json;
import java.util.*;
import org.json.*;
import java.lang.reflect.*;

public final class JSON
{
	private JSON()
	{
	}
	
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
			else if(obj instanceof JsonList)
			{
				o = FormatArray((JsonList) obj);
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

		JsonMap ret = new JsonMap_local();
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

		JsonList ret = new JsonList_local();
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
			else if(value instanceof JsonList)
				ret.put(name, FormatArray((JsonList)value));
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
			else if(value instanceof JsonList)
				ret.put(FormatArray((JsonList)value));
			else if(JSONObject.NULL.equals(value))
				ret.put(null);
			else
				ret.put(value);
		}
		return ret;
	}

	// utils
	public static final class Utility
	{
		private Utility() {}

		public static JsonMap InstanceJsonMap(Map<String, Object> map) // 不总是返回一个非NULL的JsonMap
		{
			JsonMap ret = null;
			Iterator<String> itor;

			if (map == null)
				return null;

			ret = new JsonMap_local();
			itor = map.keySet().iterator();
			while (itor.hasNext()) {
				String name = itor.next();
				ret.Put(name, map.get(name));
			}
			return ret;
		}

		public static JsonMap InstanceJsonArray(Map<String, Object> map) // 不总是返回一个非NULL的JsonMap
		{
			JsonMap ret = null;
			Iterator<String> itor;

			if (map == null)
				return null;

			ret = new JsonMap_local();
			itor = map.keySet().iterator();
			while (itor.hasNext()) {
				String name = itor.next();
				ret.Put(name, map.get(name));
			}
			return ret;
		}
	}


	// internal
	private static class DataValue
	{
		public Object value;
		public int type;
		public DataValue() {}
		public DataValue(Object value, int type)
		{
			this.value = value;
			this.type = type;
		}
	}

	// implement by ArrayList
	private static class JsonList_local extends ArrayList<DataValue> implements JsonList
	{
		public int Length()
		{
			return super.size();
		}

		public<T> T GetT(int index)
		{
			return (T)(Get(index));
		}
		
		public Object Get(int index)
		{
			return get(index).value;
		}

		public JsonList Put(Object value)
		{
			DataValue d;

			d = new DataValue();
			int type = JsonDef.JSON_VALUE_TYPE_UNDEFINED;
			if(value instanceof String || value instanceof Character)
				type = JsonDef.JSON_VALUE_TYPE_STRING;
			else if(value instanceof Float || value instanceof Double)
				type = JsonDef.JSON_VALUE_TYPE_FLOAT;
			else if(value instanceof Byte || value instanceof Short || value instanceof Integer || value instanceof Long)
				type = JsonDef.JSON_VALUE_TYPE_INTEGER;
			else if(value instanceof Array)
				type = JsonDef.JSON_VALUE_TYPE_ARRAY;
			else if(value instanceof Boolean)
				type = JsonDef.JSON_VALUE_TYPE_BOOL;
			else
				type = JsonDef.JSON_VALUE_TYPE_OBJECT;

			d.type = type;
			d.value = value;
			super.add(d);
			
			return this;
		}

		public int GetType(int index)
		{
			return super.get(index).type;
		}

		@Override
		public int GetJsonResultType()
		{
			return JSON_RESULT_ARRAY;
		}
	}

	private static class JsonMap_local extends HashMap<String, DataValue> implements JsonMap
	{
		public<T> T GetT(String name)
		{
			return (T)Get(name);
		}

		@Override
		public String[] GetKeys()
		{
			Set<String> keys;
			Iterator<String> itor;
			String ret[];
			int i = 0;

			keys = super.keySet();
			itor = keys.iterator();
			ret = new String[keys.size()];
			while(itor.hasNext())
				ret[i++] = itor.next();

			return ret;
		}

		public Object Get(String name)
		{
			return get(name).value;
		}

		public JsonMap Put(String name, Object value)
		{
			DataValue d;

			d = new DataValue();
			int type = JsonDef.JSON_VALUE_TYPE_UNDEFINED;
			if(value instanceof String || value instanceof Character)
				type = JsonDef.JSON_VALUE_TYPE_STRING;
			else if(value instanceof Float || value instanceof Double)
				type = JsonDef.JSON_VALUE_TYPE_FLOAT;
			else if(value instanceof Byte || value instanceof Short || value instanceof Integer || value instanceof Long)
				type = JsonDef.JSON_VALUE_TYPE_INTEGER;
			else if(value instanceof Array)
				type = JsonDef.JSON_VALUE_TYPE_ARRAY;
			else if(value instanceof Boolean)
				type = JsonDef.JSON_VALUE_TYPE_BOOL;
			else
				type = JsonDef.JSON_VALUE_TYPE_OBJECT;

			d.type = type;
			d.value = value;
			super.put(name, d);
			
			return this;
		}

		public int GetType(String name)
		{
			return super.get(name).type;
		}

		@Override
		public int GetJsonResultType()
		{
			return JSON_RESULT_OBJECT;
		}
	}
}
