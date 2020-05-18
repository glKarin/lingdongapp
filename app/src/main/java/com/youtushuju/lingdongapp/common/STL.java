package com.youtushuju.lingdongapp.common;

import java.util.Collection;
import java.util.Iterator;

public final class STL {
    private STL()
    {
    }

    public static boolean CollectionIsEmpty(Collection list)
    {
        return list == null || list.isEmpty();
    }

    public static String[] CollectionToStringArray(Collection list)
    {
        if(list == null)
            return null;

        String ret[] = new String[list.size()];
        int i = 0;
        Iterator itor = list.iterator();
        while(itor.hasNext())
        {
            ret[i++] = itor.next().toString();
        }
        return ret;
    }

    public static String CollectionJoin(Collection list, String ch)
    {
        if(list == null)
            return null;

        String ret = "";
        int i = 0;
        Iterator itor = list.iterator();
        while(itor.hasNext())
        {
            ret += itor.next().toString();
            if(itor.hasNext())
                ret += ch;
        }
        return ret;
    }
}
