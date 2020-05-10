package com.youtushuju.lingdongapp.json;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.*;
import java.lang.reflect.*;

// Now JsonList extend ArrayList, can instance with new operator.
public class JsonList extends ArrayList implements JsonResult
{
	private Map<Integer, Integer> m_type = null;

	public JsonList()
	{
		super();
		m_type = new HashMap<Integer, Integer>();
	}

	public JsonList(List l)
	{
		super();
		m_type = new HashMap<Integer, Integer>();
		addAll(l);
	}

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
		return get(index);
	}

	@Override
	public boolean add(Object o) {
		Put(o);
		return true;
	}

	@Override
	public void add(int index, Object element) {
		Put(element, index);
	}

	@Override
	public Object remove(int index) {
		Object ret = super.remove(index);
		m_type.remove(index);
		return ret;
	}

	@Override
	public void clear() {
		super.clear();
		m_type.clear();
	}

	@Override
	public boolean addAll(@NonNull Collection c) {
		if(c == null)
			return false;

		int count = 0;
		Iterator itor = c.iterator();
		while(itor.hasNext())
		{
			Put(itor.next());
			count++;
		}
		return count > 0;
	}

	@Override
	public boolean remove(@Nullable Object o) {
		int key = -1;
		for (int i = 0; i < Length(); i++)
		{
			if(o == null)
			{
				if(Get(i) == null)
				{
					remove(i);
					return true;
				}
			}
			else
			{
				if(o.equals(Get(i)))
				{
					remove(i);
					return true;
				}
			}
		}
		return false;
	}

	public JsonList Put(Object value)
	{
		return Put(value, Length());
	}

	public JsonList Put(Object value, int index)
	{
		int type = JsonResult.JSON_VALUE_TYPE_UNDEFINED;
		if(value instanceof String || value instanceof Character)
			type = JsonResult.JSON_VALUE_TYPE_STRING;
		else if(value instanceof Float || value instanceof Double)
			type = JsonResult.JSON_VALUE_TYPE_FLOAT;
		else if(value instanceof Byte || value instanceof Short || value instanceof Integer || value instanceof Long)
			type = JsonResult.JSON_VALUE_TYPE_INTEGER;
		else if(value instanceof Array)
			type = JsonResult.JSON_VALUE_TYPE_ARRAY;
		else if(value instanceof Boolean)
			type = JsonResult.JSON_VALUE_TYPE_BOOL;
		else if(value instanceof JsonList)
			type = JsonResult.JSON_VALUE_TYPE_ARRAY;
		else if(value instanceof JsonMap)
			type = JsonResult.JSON_VALUE_TYPE_OBJECT;
		else
			type = JsonResult.JSON_VALUE_TYPE_OBJECT;

		if(index < 0)
		{
			super.add(0, value);
			m_type.put(0, type);
		}
		else if(index >= Length())
		{
			super.add(value);
			m_type.put(Length(), type);
		}
		else
		{
			super.add(index, value);
			m_type.put(index, type);
		}

		return this;
	}

	public int GetType(int index)
	{
		return m_type.get(index);
	}

	@Override
	public int GetJsonResultType()
	{
		return JSON_RESULT_ARRAY;
	}

	public static JsonList FromList(List l)
	{
		return new JsonList(l);
	}
}
