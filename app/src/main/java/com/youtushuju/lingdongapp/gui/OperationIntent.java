package com.youtushuju.lingdongapp.gui;

import java.util.HashMap;
import java.util.Map;

public class OperationIntent {
    // 刷脸意图枚举
    public static final int ENUM_FACE_INTENT_NOTHING = 0;
    public static final int ENUM_FACE_INTENT_OPEN_DOOR = 1; // 开门
    public static final int ENUM_FACE_INTENT_OPEN_MENU = 2; // 进入菜单

    private int m_type = ENUM_FACE_INTENT_NOTHING;
    private Map<String, Object> m_data = null;

    public OperationIntent()
    {
        m_data = new HashMap<String, Object>();
    }

    public OperationIntent SetType(int type)
    {
        m_type = type;
        return this;
    }

    public int Type()
    {
        return m_type;
    }

    public OperationIntent SetData(String key, Object value)
    {
        m_data.put(key, value);
        return this;
    }

    public OperationIntent RemoveData(String key)
    {
        m_data.remove(key);
        return this;
    }

    public Object GetData(String key)
    {
        return m_data.get(key);
    }

    public<T> T GetData_t(String key)
    {
        return (T)m_data.get(key);
    }

    public void Reset()
    {
        m_type = 0;
        Clear();
    }

    public void Clear()
    {
        m_data.clear();
    }

    public boolean IsValid()
    {
        return m_type != ENUM_FACE_INTENT_NOTHING;
    }
}
