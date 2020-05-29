package com.youtushuju.lingdongapp.common;

import androidx.annotation.NonNull;

import java.util.HashMap;
import java.util.Map;

public final class CVar
{
	public static final int CONST_TYPE_INT = 1; // if 64 bit integer, using string type
	public static final int CONST_TYPE_BOOL = 1 << 1;
	public static final int CONST_TYPE_STRING = 1 << 2;
	public static final int CONST_TYPE_FLOAT = 1 << 3;

	public static final int CONST_LIFE_ARCHIVE = 1 << 8;
	public static final int CONST_LIFE_DIRTY = 1 << 9;

	private String m_name = "";
	private int m_mask = CONST_TYPE_STRING;
	private Object m_defaultValue = null;
	private Object m_value = null;
	private String m_description = "";
	private static Map<String, CVar> _globalCVars = new HashMap<String, CVar>();

	private CVar()
	{
	}

	public CVar(String name, Object value, int mask, String desc)
	{
		m_name = name;
		m_defaultValue = m_value = value;
		m_mask = mask;
		m_description = desc;

		_globalCVars.put(name, this);
	}

	public static CVar cvar(String name, Object value, int mask, String desc)
	{
		boolean has = _globalCVars.containsKey(name);
		CVar var = has ? _globalCVars.get(name) : new CVar();

		var.m_name = name;
		var.m_defaultValue = var.m_value = value;
		var.m_mask = mask;
		var.m_description = desc;
		if(!has)
			_globalCVars.put(name, var);
		return var;
	}

	public void SetValue(Object v)
	{
		m_value = v;
	}

	public void Remove()
	{
		_globalCVars.remove(m_name);
	}

	public static void Remove(String name)
	{
		_globalCVars.remove(name);
	}

	public static void Dump()
	{
		// TODO
	}

	public static void Restore()
	{
		// TODO
	}

	private Object GetValue()
	{
		return m_value != null ? m_value : m_defaultValue;
	}

	// GetXXX dont throw exception
	public int GetInt()
	{
		Object value = GetValue();
		if(value == null)
			return 0;
		if(value instanceof Integer)
			return ((Integer)value).intValue();
		if(value instanceof Byte)
			return ((Byte)value).intValue();
		if(value instanceof Short)
			return ((Short)value).intValue();
		if(value instanceof Boolean)
			return ((Boolean)value).booleanValue() ? 1 : 0;
		if(value instanceof Long)
			return ((Long)value).intValue();
		if(value instanceof Float)
			return ((Float)value).intValue();
		if(value instanceof Double)
			return ((Double)value).intValue();
		try
		{
			return Integer.parseInt(value.toString());
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return 0;
	}

	public float GetFloat()
	{
		Object value = GetValue();
		if(value == null)
			return 0;
		if(value instanceof Float)
			return ((Float)value).floatValue();
		if(value instanceof Double)
			return ((Double)value).floatValue();
		if(value instanceof Integer)
			return ((Integer)value).intValue();
		if(value instanceof Byte)
			return ((Byte)value).intValue();
		if(value instanceof Short)
			return ((Short)value).intValue();
		if(value instanceof Boolean)
			return ((Boolean)value).booleanValue() ? 1 : 0;
		if(value instanceof Long)
			return ((Long)value).intValue();
		try
		{
			return Integer.parseInt(value.toString());
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return 0;
	}

	public boolean GetBool()
	{
		Object value = GetValue();
		if(value == null)
			return false;
		if(value instanceof Boolean)
			return ((Boolean)value).booleanValue();
		if(value instanceof Integer)
			return ((Integer)value).intValue() != 0;
		if(value instanceof Byte)
			return ((Byte)value).byteValue() != 0;
		if(value instanceof Short)
			return ((Short)value).shortValue() != 0;
		if(value instanceof Long)
			return ((Long)value).longValue() != 0L;
		if(value instanceof Float)
			return ((Float)value).intValue() != 0;
		if(value instanceof Double)
			return ((Double)value).longValue() != 0;
		String str = value.toString();
		try
		{
			return Integer.parseInt(str) != 0;
		}
		catch (Exception e)
		{
			return str != null && !str.isEmpty();
		}
	}

	// always dont return null
	public String GetString()
	{
		Object value = GetValue();
		if(value == null)
			return "";
		String str = value.toString();
		return(str == null ? "" : str);
	}

	public static int GetInt(String name)
	{
		if(!_globalCVars.containsKey(name))
			return 0;
		CVar var = _globalCVars.get(name);
		return var.GetInt();
	}

	public static float GetFloat(String name)
	{
		if(!_globalCVars.containsKey(name))
			return 0;
		CVar var = _globalCVars.get(name);
		return var.GetFloat();
	}

	public static boolean GetBool(String name)
	{
		if(!_globalCVars.containsKey(name))
			return false;
		CVar var = _globalCVars.get(name);
		return var.GetBool();
	}

	public static String GetString(String name)
	{
		if(!_globalCVars.containsKey(name))
			return "";
		CVar var = _globalCVars.get(name);
		return var.GetString();
	}

	@NonNull
	@Override
	public String toString() {
		return String.format("CVar: name(%s), value(%s), default(%s), mask(0X%x), description(%s)",
				m_name, m_value != null ? m_value.toString() : "", m_defaultValue != null ? m_defaultValue.toString() : "", m_mask, m_description);
	}
}
