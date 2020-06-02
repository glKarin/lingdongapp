package com.youtushuju.lingdongapp.json;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

// Now JsonList extend HashMap, can instance with new operator.
public class JsonMap extends HashMap<String, Object> implements JsonResult
{
	private Map<String, Integer> m_type = null;

	public JsonMap()
	{
		super();
		m_type = new HashMap<String, Integer>();
	}

	public JsonMap(Map<String, Object> m)
	{
		super();
		m_type = new HashMap<String, Integer>();
		putAll(m);
	}

	@Nullable
	@Override
	public Object put(String key, Object value) {
		Put(key, value);
		return value;
	}

	@Override
	public void putAll(@NonNull Map<? extends String, ?> m) {
		Set<? extends String> keys;
		Iterator<? extends String> itor;

		keys = m.keySet();
		itor = keys.iterator();
		while(itor.hasNext())
		{
			String key = itor.next();
			Put(key, m.get(key));
		}
	}

	@Override
	public void clear() {
		super.clear();
		m_type.clear();
	}

	@Override
	public boolean remove(@Nullable Object key, @Nullable Object value) {
		boolean ret = super.remove(key, value);
		m_type.remove(key);
		return ret;
	}

	public<T> T GetT(String name)
	{
		return (T)Get(name);
	}

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
		return get(name);
	}

	public JsonMap Put(String name, Object value)
	{
		int type = JsonResult.JSON_VALUE_TYPE_UNDEFINED;
		if(value instanceof String || value instanceof Character)
			type = JsonResult.JSON_VALUE_TYPE_STRING;
		else if(value instanceof Float || value instanceof Double)
			type = JsonResult.JSON_VALUE_TYPE_FLOAT;
		else if(value instanceof Byte || value instanceof Short || value instanceof Integer || value instanceof Long)
			type = JsonResult.JSON_VALUE_TYPE_INTEGER;
		else if(value instanceof Object[])
			type = JsonResult.JSON_VALUE_TYPE_ARRAY;
		else if(value instanceof Boolean)
			type = JsonResult.JSON_VALUE_TYPE_BOOL;
		else if(value instanceof JsonList)
			type = JsonResult.JSON_VALUE_TYPE_ARRAY;
		else if(value instanceof JsonMap)
			type = JsonResult.JSON_VALUE_TYPE_OBJECT;
		else
			type = JsonResult.JSON_VALUE_TYPE_OBJECT;

		super.put(name, value);
		m_type.put(name, type);

		return this;
	}

	public int GetType(String name)
	{
		return m_type.get(name);
	}

	@Override
	public int GetJsonResultType()
	{
		return JSON_RESULT_OBJECT;
	}

	public static JsonMap FromMap(Map<String, Object> m)
	{
		return new JsonMap(m);
	}

	public boolean Contains(String name)
	{
		return containsKey(name);
	}
}

