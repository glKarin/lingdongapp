package com.youtushuju.lingdongapp.database;

import com.youtushuju.lingdongapp.common.Logf;

import java.util.List;
import java.util.Map;

public final class DBUtility
{

	private DBUtility()
	{
	}

	public static<T> Paged<T> GetPagedList_T(DB db, Class<T> clazz, Map<String, String> map, String table, String columns[], String condition, String condValues[], String orderBy, Paged<T> paged)
	{
		Paged<T> ret = paged != null ? paged : new Paged<T>();

		int count = db.Count(table, "*", condition, condValues, null, null, false);
		int limit = paged.PageSize();
		int start = (paged.PageNo() - 1) * limit;

		List<T> list = db.GetAll_T(clazz, map, table, columns, condition, condValues, orderBy, start + "," + limit);

		ret.SetTotalCount(count);
		ret.SetData(list);
		ret.CalePageCount();

		return ret;
	}
}
