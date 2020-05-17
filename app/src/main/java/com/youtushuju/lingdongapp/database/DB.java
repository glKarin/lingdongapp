package com.youtushuju.lingdongapp.database;
import android.database.sqlite.*;
import android.content.*;
import android.database.*;
import java.util.*;
import android.util.*;

import com.youtushuju.lingdongapp.common.Common;

import java.lang.reflect.*;

public final class DB
{
	private static final String DB_NAME = "lingdongapp.db";
	public static final String ID_RECORD_TABLE_NAME = "record";
	private static final int DB_VERSION = 1;
	private SQLiteOpenHelper m_helper = null;
	private static DB _db = null;
	private Context m_applicationContext = null;

	private DB()
	{
	}

	private void InitDB(Context context)
	{
		m_applicationContext =context.getApplicationContext();
		m_helper = new SQLiteOpenHelper_local(m_applicationContext, DB_NAME, null, DB_VERSION);
	}

	public static DB Instance(Context context)
	{
		if(_db == null)
		{
			if(context == null)
				return null;
			_db = new DB();
			_db.InitDB(context);
		}

		return _db;
	}
	
	public void DropDatabase()
	{
		m_applicationContext.deleteDatabase(DB_NAME);
	}

	public List<Map<String, Object>> GetAll(String table, String columns[], String condition, String condValues[], String orderBy, String limit)
	{
		DBM dbm = new RDBM(m_helper);
		SQLiteDatabase db = dbm.db;
		Cursor cursor = db.query(table, columns, condition, condValues, null, null, orderBy, limit);
		if(cursor == null)
			return null;
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		while(cursor.moveToNext())
		{
			Map<String, Object> item = new HashMap<String, Object>();
			int colCount = cursor.getColumnCount();
			for(int i = 0; i < colCount; i++)
			{
				Object value = null;
				switch(cursor.getType(i))
				{
					case Cursor.FIELD_TYPE_INTEGER:
						value = cursor.getLong(i);
						break;
					case Cursor.FIELD_TYPE_STRING:
						value = cursor.getString(i);
						break;
					case Cursor.FIELD_TYPE_FLOAT:
						value = cursor.getDouble(i);
						break;
					case Cursor.FIELD_TYPE_BLOB:
						value = cursor.getBlob(i);
						break;
					case Cursor.FIELD_TYPE_NULL:
					default:
						break;
				}
				item.put(cursor.getColumnName(i), value);
			}
			list.add(item);
		}
		cursor.close();
		return list;
	}

	public List<Object> GetCol(String table, String column, String condition, String condValues[], String orderBy, String limit)
	{
		DBM dbm = new RDBM(m_helper);
		SQLiteDatabase db = dbm.db;
		Cursor cursor = db.query(table, new String[]{column}, condition, condValues, null, null, orderBy, limit);
		if(cursor == null)
			return null;
		List<Object> list = new ArrayList<Object>();
		while(cursor.moveToNext())
		{
			Object value = null;
			switch(cursor.getType(0))
			{
				case Cursor.FIELD_TYPE_INTEGER:
					value = cursor.getLong(0);
					break;
				case Cursor.FIELD_TYPE_STRING:
					value = cursor.getString(0);
					break;
				case Cursor.FIELD_TYPE_FLOAT:
					value = cursor.getDouble(0);
					break;
				case Cursor.FIELD_TYPE_BLOB:
					value = cursor.getBlob(0);
					break;
				case Cursor.FIELD_TYPE_NULL:
				default:
					break;
			}
			list.add(value);
		}
		cursor.close();
		return list;
	}

	public Map<String, Object> GetRow(String table, String columns[], String condition, String condValues[], String orderBy)
	{
		DBM dbm = new RDBM(m_helper);
		SQLiteDatabase db = dbm.db;
		Cursor cursor = db.query(table, columns, condition, condValues, null, null, orderBy, "0, 1");
		if(cursor == null)
			return null;
		Map<String, Object> row = null;
		if(cursor.moveToFirst())
		{
			row = new HashMap<String, Object>();
			int colCount = cursor.getColumnCount();
			for(int i = 0; i < colCount; i++)
			{
				Object value = null;
				switch(cursor.getType(i))
				{
					case Cursor.FIELD_TYPE_INTEGER:
						value = cursor.getLong(i);
						break;
					case Cursor.FIELD_TYPE_STRING:
						value = cursor.getString(i);
						break;
					case Cursor.FIELD_TYPE_FLOAT:
						value = cursor.getDouble(i);
						break;
					case Cursor.FIELD_TYPE_BLOB:
						value = cursor.getBlob(i);
						break;
					case Cursor.FIELD_TYPE_NULL:
					default:
						break;
				}
				row.put(cursor.getColumnName(i), value);
			}
		}
		cursor.close();
		return row;
	}

	public Object GetOne(String table, String column, String condition, String condValues[], String orderBy)
	{
		DBM dbm = new RDBM(m_helper);
		SQLiteDatabase db = dbm.db;
		Cursor cursor = db.query(table, new String[]{column}, condition, condValues, null, null, orderBy, "0, 1");
		if(cursor == null)
			return null;
		Object value = null;
		if(cursor.moveToFirst())
		{
			switch(cursor.getType(0))
			{
				case Cursor.FIELD_TYPE_INTEGER:
					value = cursor.getLong(0);
					break;
				case Cursor.FIELD_TYPE_STRING:
					value = cursor.getString(0);
					break;
				case Cursor.FIELD_TYPE_FLOAT:
					value = cursor.getDouble(0);
					break;
				case Cursor.FIELD_TYPE_BLOB:
					value = cursor.getBlob(0);
					break;
				case Cursor.FIELD_TYPE_NULL:
				default:
					break;
			}
		}
		cursor.close();
		return value;
	}

	public<T> List<T> GetCol_T(String table, String column, String condition, String condValues[], String orderBy, String limit)
	{
		List<Object> list = GetCol(table, column, condition, condValues, orderBy, limit);
		if(list == null)
			return null;
		List<T> ret = new ArrayList<T>();
		for(Object obj : list)
			ret.add((T)obj);
		return ret;
	}

	public<T> T GetOne_T(String table, String column, String condition, String condValues[], String orderBy)
	{
		return (T)GetOne(table, column, condition, condValues, orderBy);
	}

	public<T> List<T> GetAll_T(Class<T> clazz, Map<String, String> map, String table, String columns[], String condition, String condValues[], String orderBy, String limit)
	{
		Constructor<T> constructor = null;
		try
		{
			constructor = clazz.getConstructor();
		}
		catch(NoSuchMethodException e)
		{
			e.printStackTrace();
			return null;
		}
		DBM dbm = new RDBM(m_helper);
		SQLiteDatabase db = dbm.db;
		Cursor cursor = db.query(table, columns, condition, condValues, null, null, orderBy, limit);
		if(cursor == null)
			return null;
		List<T> list = new ArrayList<T>();
		while(cursor.moveToNext())
		{
			T obj = null;
			try
			{
				obj = constructor.newInstance();
			}
			catch(InstantiationException | InvocationTargetException | IllegalAccessException e)
			{
				e.printStackTrace();
				list = null;
				break;
			}
			CursorToObject(cursor, clazz, obj, map);
			list.add(obj);
		}
		cursor.close();
		return list;
	}
		
	public<T> T GetRow_T(Class<T> clazz, Map<String, String> map, String table, String columns[], String condition, String condValues[], String orderBy)
	{
		Constructor<T> constructor = null;
		try
		{
			constructor = clazz.getConstructor();
		}
		catch(NoSuchMethodException e)
		{
			e.printStackTrace();
			return null;
		}
			DBM dbm = new RDBM(m_helper);
			SQLiteDatabase db = dbm.db;
			Cursor cursor = db.query(table, columns, condition, condValues, null, null, orderBy, "0, 1");
			if(cursor == null)
				return null;
			T obj = null;
			if(cursor.moveToFirst())
			{
				try
				{
					obj = constructor.newInstance();
					CursorToObject(cursor, clazz, obj, map);
				}
				catch(InstantiationException | InvocationTargetException | IllegalAccessException e)
				{
					e.printStackTrace();
				}
			}
			cursor.close();
			return obj;
		}

		public int Count(String table, String column, String condition, String condValues[], String groupBy, String having, boolean distinct)
		{
			DBM dbm = new RDBM(m_helper);
			SQLiteDatabase db = dbm.db;
			String col = column == null || column.isEmpty() ? "*" : column;
			Cursor cursor = db.query(distinct, table, new String[]{"COUNT(" + col + ")"}, condition, condValues, groupBy, having, null, null);
			if(cursor == null)
				return -1;
			int count = 0;
			if(cursor.moveToNext())
			{
				count = cursor.getInt(0);
			}
			cursor.close();
			return count;
		}
		
		public void Execute(String sql, Object ...args)
		{
			DBM dbm = new WDBM(m_helper);
			SQLiteDatabase db = dbm.db;
			db.execSQL(sql, args);
		}
		
		public List<Map<String, Object>> Query(String sql, String ...args)
		{
			DBM dbm = new RDBM(m_helper);
			SQLiteDatabase db = dbm.db;
			Cursor cursor = db.rawQuery(sql, args);
			if(cursor == null)
				return null;
			List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
			while(cursor.moveToNext())
			{
				Map<String, Object> item = new HashMap<String, Object>();
				int colCount = cursor.getColumnCount();
				for(int i = 0; i < colCount; i++)
				{
					Object value = null;
					switch(cursor.getType(i))
					{
						case Cursor.FIELD_TYPE_INTEGER:
							value = cursor.getLong(i);
							break;
						case Cursor.FIELD_TYPE_STRING:
							value = cursor.getString(i);
							break;
						case Cursor.FIELD_TYPE_FLOAT:
							value = cursor.getDouble(i);
							break;
						case Cursor.FIELD_TYPE_BLOB:
							value = cursor.getBlob(i);
							break;
						case Cursor.FIELD_TYPE_NULL:
						default:
							break;
					}
					item.put(cursor.getColumnName(i), value);
				}
				list.add(item);
			}
			cursor.close();
			return list;
		}

	public<T> List<T> Query_T(Class<T> clazz, Map<String, String> map, String sql, String ...args)
	{
		Constructor<T> constructor = null;
		try
		{
			constructor = clazz.getConstructor();
		}
		catch(NoSuchMethodException e)
		{
			e.printStackTrace();
			return null;
		}
		DBM dbm = new RDBM(m_helper);
		SQLiteDatabase db = dbm.db;
		Cursor cursor = db.rawQuery(sql, args);
		if(cursor == null)
			return null;
		List<T> list = new ArrayList<T>();
		while(cursor.moveToNext())
		{
			T obj = null;
			try
			{
				obj = constructor.newInstance();
			}
			catch(InstantiationException | InvocationTargetException | IllegalAccessException e)
			{
				e.printStackTrace();
				list = null;
				break;
			}
			CursorToObject(cursor, clazz, obj, map);
			list.add(obj);
		}
		cursor.close();
		return list;
	}

	private<T> void CursorToObject(Cursor cursor, Class<T> clazz, T obj, Map<String, String> map)
	{
		int colCount = cursor.getColumnCount();
		for(int i = 0; i < colCount; i++)
		{
			String colName = cursor.getColumnName(i);
			String propName;
			if(map == null || !map.containsKey(colName))
				propName = colName;
			else
				propName = map.get(colName);
			try
			{
				Field field = clazz.getField(propName);
				switch(cursor.getType(i))
				{
					case Cursor.FIELD_TYPE_INTEGER:
						field.setLong(obj, cursor.getLong(i));
						break;
					case Cursor.FIELD_TYPE_STRING:
						field.set(obj, cursor.getString(i));
						break;
					case Cursor.FIELD_TYPE_FLOAT:
						field.set(obj, cursor.getDouble(i));
						break;
					case Cursor.FIELD_TYPE_BLOB:
						field.set(obj, cursor.getBlob(i));
						break;
					case Cursor.FIELD_TYPE_NULL:
					default:
						field.set(obj, null);
						break;
				}
				continue;
			}
			catch(NoSuchFieldException | IllegalAccessException e)
			{
			}

			String methodName = "Set" + Common.UCFirst(propName);
			Method method = null;
			switch(cursor.getType(i))
			{
				case Cursor.FIELD_TYPE_INTEGER:
					try
					{
						method = clazz.getMethod(methodName, new Class[]{long.class});
						method.invoke(obj, cursor.getLong(i));
						break;
					}
					catch(NoSuchMethodException | IllegalAccessException | InvocationTargetException e){}
					try
					{
						method = clazz.getMethod(methodName, new Class[]{Long.class});
						method.invoke(obj, cursor.getLong(i));
						break;
					}
					catch(NoSuchMethodException | IllegalAccessException | InvocationTargetException e){}

					try
					{
						method = clazz.getMethod(methodName, new Class[]{int.class});
						method.invoke(obj, cursor.getInt(i));
						break;
					}
					catch(NoSuchMethodException | IllegalAccessException | InvocationTargetException e){}
					try
					{
						method = clazz.getMethod(methodName, new Class[]{Integer.class});
						method.invoke(obj, cursor.getInt(i));
						break;
					}
					catch(NoSuchMethodException | IllegalAccessException | InvocationTargetException e){}

					try
					{
						method = clazz.getMethod(methodName, new Class[]{Short.class});
						method.invoke(obj, cursor.getShort(i));
						break;
					}
					catch(NoSuchMethodException | IllegalAccessException | InvocationTargetException e){}
					try
					{
						method = clazz.getMethod(methodName, new Class[]{short.class});

						method.invoke(obj, cursor.getShort(i));
						break;
					}
					catch(NoSuchMethodException | IllegalAccessException | InvocationTargetException e){}

					try
					{
						method = clazz.getMethod(methodName, new Class[]{byte.class});
						method.invoke(obj, (byte)cursor.getInt(i));
						break;
					}
					catch(NoSuchMethodException | IllegalAccessException | InvocationTargetException e){}
					try
					{
						method = clazz.getMethod(methodName, new Class[]{Byte.class});
						method.invoke(obj, (byte)cursor.getInt(i));
						break;
					}
					catch(NoSuchMethodException | IllegalAccessException | InvocationTargetException e){}
					break;
				case Cursor.FIELD_TYPE_STRING:
					try
					{
						method = clazz.getMethod(methodName, new Class[]{String.class});
						method.invoke(obj, cursor.getString(i));
					}
					catch(NoSuchMethodException | IllegalAccessException | InvocationTargetException e){}
					break;
				case Cursor.FIELD_TYPE_FLOAT:
					try
					{
						method = clazz.getMethod(methodName, new Class[]{double.class});
						method.invoke(obj, cursor.getDouble(i));
						break;
					}
					catch(NoSuchMethodException | IllegalAccessException | InvocationTargetException e){}
					try
					{
						method = clazz.getMethod(methodName, new Class[]{Double.class});
						method.invoke(obj, cursor.getDouble(i));
						break;
					}
					catch(NoSuchMethodException | IllegalAccessException | InvocationTargetException e){}

					try
					{
						method = clazz.getMethod(methodName, new Class[]{float.class});
						method.invoke(obj, cursor.getFloat(i));
						break;
					}
					catch(NoSuchMethodException | IllegalAccessException | InvocationTargetException e){}
					try
					{
						method = clazz.getMethod(methodName, new Class[]{Float.class});
						method.invoke(obj, cursor.getFloat(i));
						break;
					}
					catch(NoSuchMethodException | IllegalAccessException | InvocationTargetException e){}
					break;
				case Cursor.FIELD_TYPE_BLOB:
					try
					{
						method = clazz.getMethod(methodName, new Class[]{byte[].class});
						method.invoke(obj, cursor.getBlob(i));
					}
					catch(NoSuchMethodException | IllegalAccessException | InvocationTargetException e){}
					break;
				case Cursor.FIELD_TYPE_NULL:
				default:
					break;
			}
		}
	}

	public boolean Insert(String table, ContentValues values)
	{
		DBM dbm = new WDBM(m_helper);
		SQLiteDatabase db = dbm.db;
		long res = db.insert(table, null, values);
		return res != -1;
	}

	public long InsertAndResultId(String table, ContentValues values)
	{
		DBM dbm = new WDBM(m_helper);
		SQLiteDatabase db = dbm.db;
		long res = db.insert(table, null, values);
		return res;
	}

	public int Update(String table, ContentValues values, String condition, String condValues[])
	{
		DBM dbm = new WDBM(m_helper);
		SQLiteDatabase db = dbm.db;
		return db.update(table, values, condition, condValues);
	}

	public int Delete(String table, String condition, String condValues[])
	{
		DBM dbm = new WDBM(m_helper);
		SQLiteDatabase db = dbm.db;
		return db.delete(table, condition, condValues);
	}

	private abstract class DBM
	{
		public SQLiteDatabase db = null;
		public DBM(SQLiteOpenHelper helper)
		{
			db = Open(helper);
		}

		@Override
		protected void finalize() throws Throwable
		{
			super.finalize();
			db.close();
			Log.d("DB", "Database connection closed.");
		}
		protected abstract SQLiteDatabase Open(SQLiteOpenHelper helper);
	}

	private class RDBM extends DBM
	{
		public RDBM(SQLiteOpenHelper helper)
		{
			super(helper);
		}

		protected SQLiteDatabase Open(SQLiteOpenHelper helper)
		{
			return helper.getReadableDatabase();
		}
	}

	private class WDBM extends DBM
	{
		public WDBM(SQLiteOpenHelper helper)
		{
			super(helper);
		}

		protected SQLiteDatabase Open(SQLiteOpenHelper helper)
		{
			return helper.getWritableDatabase();
		}
	}

	private class SQLiteOpenHelper_local extends SQLiteOpenHelper
	{
		public SQLiteOpenHelper_local(Context context, String name, SQLiteDatabase.CursorFactory factory, int version)
		{
			super(context, name, factory, version);
		}

		@Override
		public void onCreate(SQLiteDatabase db)
		{
			db.execSQL("CREATE TABLE " + ID_RECORD_TABLE_NAME + "("
					+ "`id` INTEGER PRIMARY KEY AUTOINCREMENT, "
					+ "`name` TEXT NOT NULL, "
					+ "`device` TEXT NOT NULL, "
					+ "`operation` TEXT NOT NULL, "
					+ "`time` TIMESTAMP NOT NULL, "
					+ "`weight` TEXT, "
					+ "`result` INTEGER DEFAULT 0, "
					+ "`uuid` TEXT, "
					+ "`create_time` TIMESTAMP"
					+ ")"
					);
		}

		@Override
		public void onUpgrade(SQLiteDatabase p1, int p2, int p3)
		{
			// TODO: Implement this method
		}
	}
}
