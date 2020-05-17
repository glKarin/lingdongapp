package com.youtushuju.lingdongapp.database;

import android.content.ContentValues;

import com.youtushuju.lingdongapp.common.Common;

import java.util.HashMap;
import java.util.Map;

public class RecordModel extends DBModel_base {
    public static final int ID_RESULT_SUCCESS = 1;
    public static final int ID_RESULT_ERROR = 2;

    private long m_id;
    private String m_name;
    private String m_device;
    private String m_operation;
    private int m_result;
    private String m_weight;
    private String m_uuid;
    private long m_time;
    private long m_createTime;

    public RecordModel()
    {
        super();
    }

    public long Id()
    {
        return m_id;
    }

    public void SetId(long id)
    {
        this.m_id = id;
    }

    public long CreateTime()
    {
        return m_createTime;
    }

    public void SetCreateTime(long ts)
    {
        this.m_createTime = ts;
    }

    public int Result()
    {
        return m_result;
    }

    public void SetResult(int r)
    {
        this.m_result = r;
    }

    public String Name()
    {
        return m_name;
    }

    public void SetName(String name)
    {
        this.m_name = name;
    }

    public String Device()
    {
        return m_device;
    }

    public void SetDevice(String device)
    {
        this.m_device = device;
    }

    public String Weight()
    {
        return m_weight;
    }

    public void SetWeight(String weight)
    {
        this.m_weight = weight;
    }

    public String Operation()
    {
        return m_operation;
    }

    public void SetOperation(String op)
    {
        this.m_operation = op;
    }

    public String Uuid()
    {
        return m_uuid;
    }

    public void SetUuid(String uuid)
    {
        this.m_uuid = uuid;
    }

    public long Time()
    {
        return m_time;
    }

    public void SetTime(long t)
    {
        this.m_time = t;
    }

    public static String ResultToString(int result)
    {
        return result == ID_RESULT_SUCCESS ? "成功" : (result == ID_RESULT_ERROR ? "失败" : "");
    }

    public String toString()
    {
        StringBuffer sb = new StringBuffer();
        sb.append("ID: " + m_id).append('\n');
        sb.append("姓名: " + m_name).append('\n');
        sb.append("操作类型: " + m_operation).append('\n');
        sb.append("设备ID: " + m_device).append('\n');
        sb.append("重量: " + m_weight).append('\n');
        sb.append("结果: " + ResultToString(m_result)).append('\n');
        sb.append("记录时间: " + Common.TimestampToStr(m_time)).append('\n');
        sb.append("UUID: " + m_uuid).append('\n');
        sb.append("创建时间: " + Common.TimestampToStr(m_createTime)).append('\n');

        return sb.toString();
    }

    public static Map<String, String> GetColumnToPropertyMap()
    {
        Map<String, String> map = new HashMap<String, String>();
        map.put(ID_DEFAULT_ID_COLUMN_NAME, "id");
        map.put("name", "name");
        map.put("device", "device");
        map.put("operation", "operation");
        map.put("result", "result");
        map.put("weight", "weight");
        map.put("uuid", "uuid");
        map.put("time", "time");
        map.put("create_time", "createTime");
        return map;
    }

    public static String GetTableName()
    {
        return DB.ID_RECORD_TABLE_NAME;
    }

    public ContentValues MakeContentValues(boolean includeId)
    {
        ContentValues cvs = new ContentValues();
        cvs.put("name", m_name);
        cvs.put("device", m_device);
        cvs.put("operation", m_operation);
        cvs.put("result", m_result);
        cvs.put("weight", m_weight);
        cvs.put("uuid", m_uuid);
        cvs.put("time", m_time);
        cvs.put("create_time", m_createTime);
        if(includeId)
            cvs.put(m_idColumnName, m_id);

        return cvs;
    }
}
