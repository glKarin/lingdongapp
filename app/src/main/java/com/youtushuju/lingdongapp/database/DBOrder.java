package com.youtushuju.lingdongapp.database;

import com.youtushuju.lingdongapp.common.Logf;
import com.youtushuju.lingdongapp.common.Pair;
import com.youtushuju.lingdongapp.common.STL;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

public abstract class DBOrder {
    public static final String ID_ORDER_BY_ASC = "ASC";
    public static final String ID_ORDER_BY_DESC = "DESC";

    public DBOrder()
    {
    }

    public String Get()
    {
        Field fields[] = getClass().getFields();
        List<String> list = new ArrayList<String>();
        //fields = getClass().getDeclaredFields(); // 获取全部
        for (int i = 0; i < fields.length; i++)
        {
            Field f = fields[i];
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

            String seq = value.toString();
            if("".equals(seq))
                seq = ID_ORDER_BY_ASC;
            else
            {
                if(!seq.equals(ID_ORDER_BY_ASC) && !seq.equals(ID_ORDER_BY_DESC))
                    continue;
            }
            list.add(f.getName() + " " + seq);
        }
        return list != null ? STL.CollectionJoin(list, ", ") : null;
    }

}
