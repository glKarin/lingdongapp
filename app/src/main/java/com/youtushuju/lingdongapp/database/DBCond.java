package com.youtushuju.lingdongapp.database;

import com.youtushuju.lingdongapp.common.Pair;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public abstract class DBCond {
    public DBCond()
    {
    }

    public Pair<String, String[]> Get()
    {
        Field fields[] = getClass().getFields();
        Map<String, String> vals = new HashMap<String, String>();
        //fields = getClass().getDeclaredFields(); // 获取全部
        for (Field f : fields)
        {
            int m = f.getModifiers();
            Object value = null;
            if(!Modifier.isPublic(m))
                continue;
            if(Modifier.isStatic(m))
                continue;
            try
            {
                value = f.get(this);
                if(value == null)
                    continue;
            }
            catch (Exception e)
            {
                e.printStackTrace();
                continue;
            }
            vals.put(f.getName(), value.toString());
        }

        if(vals.isEmpty())
            return null;

        Set<String> keys = vals.keySet();
        StringBuffer sb = new StringBuffer();
        String vs[] = new String[keys.size()];
        Iterator<String> itor = keys.iterator();
        int i = 0;
        while(itor.hasNext())
        {
            String name = itor.next();
            sb.append(name).append(" = ").append("?"); // TODO: now only `=` operator
            vs[i++] = vals.get(name);
            if(itor.hasNext())
                sb.append(" and "); // TODO: now only `AND` condition
        }

        return Pair.make_pair(sb.toString(), vs);
    }

}
