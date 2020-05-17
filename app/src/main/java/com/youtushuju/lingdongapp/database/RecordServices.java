package com.youtushuju.lingdongapp.database;

import android.content.Context;

import com.youtushuju.lingdongapp.common.Common;
import com.youtushuju.lingdongapp.common.Pair;

import java.util.HashMap;
import java.util.Map;

public class RecordServices implements DBServicesInterface<RecordModel> {
    private DB m_db = null;

    public RecordServices(Context context)
    {
        m_db = DB.Instance(context);
    }

    public static class RecordCond extends DBCond
    {
        public String time = null;
        public String name = null;
        public String device = null;
    }

    public static class RecordOrder extends DBOrder
    {
        public String id = null;
        public String time = ID_ORDER_BY_DESC;
    }

    public int Count(DBCond cond, DBOrder order)
    {
        String cols = null;
        String vals[] = null;
        String o = order != null ? order.Get() : null;

        if(cond != null)
        {
            Pair<String, String[]> c = cond.Get();
            if(c != null)
            {
                cols = c.first;
                vals = c.second;
            }
        }
        return m_db.Count(RecordModel.GetTableName(), "*", cols, vals, null, null, false);
    }

    @Override
    public Paged<RecordModel> List(DBCond cond, DBOrder order, Paged<RecordModel> paged)
    {
        String cols = null;
        String vals[] = null;
        String o = order != null ? order.Get() : null;

        if(cond != null)
        {
            Pair<String, String[]> c = cond.Get();
            if(c != null)
            {
                cols = c.first;
                vals = c.second;
            }
        }

        return DBUtility.GetPagedList_T(m_db, RecordModel.class, RecordModel.GetColumnToPropertyMap(), RecordModel.GetTableName(), new String[]{"*"}, cols, vals, o, paged);
    }

    public boolean Add(RecordModel item)
    {
        if(item == null)
            return false;

        long id = m_db.InsertAndResultId(RecordModel.GetTableName(), item.MakeContentValues(false));
        if(id != -1)
        {
            item.SetId(id);
            return true;
        }
        return false;
    }

    @Override
    public boolean DeleteById(long id)
    {
        int c = m_db.Delete(RecordModel.GetTableName(), DBModel_base.ID_DEFAULT_ID_COLUMN_NAME + " = ?", new String[]{"" + id});
        return c == 1;
    }

    public int DeleteAll()
    {
        return m_db.Delete(RecordModel.GetTableName(), null, null);
    }
}
