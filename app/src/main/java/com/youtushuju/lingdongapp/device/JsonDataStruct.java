package com.youtushuju.lingdongapp.device;

import com.youtushuju.lingdongapp.json.JSON;
import com.youtushuju.lingdongapp.json.JsonMap;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public abstract class JsonDataStruct implements DataStructInterface{
    protected Object m_properties = null; // 如果是null或空, 则默认全部公有属性. 类型可以是List<String>, Map<String, String>
    protected boolean m_includeNotExistsProperty = false; // 包涵不存在的公有属性
    protected boolean m_includeStaticProperty = false; // 包涵静态属性

    protected JsonDataStruct(){}
    protected JsonDataStruct(Object p)
    {
        m_properties = p;
    }

    // 不支持递归
    @Override
    public String Dump() {
        Map<String, Object> map;
        JsonMap jsonMap;

        map = GetPropMap();
        if(map == null)
            return null;

        jsonMap = JsonMap.FromMap(map);
        return JSON.Stringify(jsonMap);
    }

    @Override
    public boolean Restore() {
        return false; // TODO
    }

    private Map<String, Object> GetPropMap()
    {
        Map<String, Object> ret;
        Iterator<String> itor;
        Field fields[];
        Map<String, Object> writeFields;

        writeFields = new HashMap<String, Object>();
        fields = getClass().getFields();
        //fields = getClass().getDeclaredFields(); // 获取全部
        for (Field f : fields)
        {
            int m = f.getModifiers();
            Object value = null;
            if(!Modifier.isPublic(m))
                continue;
            if(!m_includeStaticProperty && Modifier.isStatic(m))
                continue;
            try
            {
                value = f.get(this);
            }
            catch (Exception e)
            {
                e.printStackTrace();
                return null;
            }
            writeFields.put(f.getName(), value);
        }

        ret = new HashMap<String, Object>();
        if(m_properties != null && m_properties instanceof List)
        {
            itor = ((List<String>)m_properties).iterator();
            while(itor.hasNext())
            {
                String name = itor.next();
                if(!m_includeNotExistsProperty && !writeFields.containsKey(name))
                    continue;
                ret.put(name, writeFields.containsKey(name) ? writeFields.get(name) : null);
            }
        }
        else if(m_properties != null && m_properties instanceof Map)
        {
            Map<String, String> map = (Map<String, String>)m_properties;
            itor = map.keySet().iterator();
            while(itor.hasNext())
            {
                String name = itor.next();
                if(!m_includeNotExistsProperty && !writeFields.containsKey(name))
                    continue;
                String jsonName = map.get(name); // json字段名称映射
                ret.put(jsonName, writeFields.containsKey(name) ? writeFields.get(name) : null);
            }
        }
        else
        {
            ret = writeFields;
        }

        return ret;
    }
}
