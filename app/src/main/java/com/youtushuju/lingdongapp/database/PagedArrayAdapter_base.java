package com.youtushuju.lingdongapp.database;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import com.youtushuju.lingdongapp.common.Logf;
import com.youtushuju.lingdongapp.gui.ArrayAdapter_base;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public abstract class PagedArrayAdapter_base<T> extends ArrayAdapter_base<T>
{
    private Paged<T> m_paged = null;

    public PagedArrayAdapter_base(Context context, int resource)
    {
        super(context, resource);
        m_paged = new Paged<T>();
    }

    public PagedArrayAdapter_base(Context context, int resource, T array[])
    {
        super(context, resource, array);
        m_paged = new Paged<T>();
    }

    public PagedArrayAdapter_base(Context context, int resource, List<T> list)
    {
        super(context, resource, list);
        m_paged = new Paged<T>();
    }

    public Paged<T> GetPaged()
    {
        return m_paged;
    }

    public void SetData(T array[])
    {
        List<T> list = new ArrayList<T>();
        if(array != null)
        {
            for (T t : array)
                list.add(t);
        }
        SetData(list);
    }

    public void SetData(Collection<? extends T> list)
    {
        super.SetData(list);
        m_paged.SetData((List<T>)list);
    }

    @Override
    public void clear()
    {
        super.clear();
        m_paged.Reset();
    }

    public void SetPaged(Paged<T> paged)
    {
        if(m_paged != paged)
        {
            super.clear();
            m_paged = paged;
        }

        //if(m_paged != null)
        super.addAll(paged.Data());
    }
}

    