package com.youtushuju.lingdongapp.database;
import com.youtushuju.lingdongapp.common.Logf;

import java.util.*;

public class Paged<T>
{
	public static final int DEFAULT_PAGE_SIZE = 20;
	private static final int MIN_PAGE_SIZE = 1;
	
	private int m_pageNo = 1;
	private int m_pageSize = DEFAULT_PAGE_SIZE;
	private int m_pageCount = 0;
	private int m_totalCount = 0;
	private List<T> data = null;
	
	public Paged()
	{
		super();
	}
	
	public Paged(int pn, int ps, int pc, int c, List<T> d)
	{
		super();
		SetPageNo(pn);
		SetPageSize(ps);
		SetPageCount(pc);
		SetTotalCount(c);
		SetData(d);
	}

	public void SetData(List<T> data)
	{
		this.data = data;
	}

	public List<T> Data()
	{
		return data;
	}

	public void SetTotalCount(int totalCount)
	{
		this.m_totalCount = totalCount > 0 ? totalCount : 0;
	}

	public int TotalCount()
	{
		return m_totalCount;
	}

	public void SetPageCount(int pageCount)
	{
		this.m_pageCount = pageCount > 0 ? pageCount : 0;
	}

	public int PageCount()
	{
		return m_pageCount;
	}

	public void SetPageSize(int m_pageSize)
	{
		this.m_pageSize = m_pageSize > MIN_PAGE_SIZE ? m_pageSize : MIN_PAGE_SIZE;
	}

	public int PageSize()
	{
		return m_pageSize;
	}	

	public void SetPageNo(int pageNo)
	{
		this.m_pageNo = pageNo > 1 ? pageNo : 1;
	}

	public int PageNo()
	{
		return m_pageNo;
	}
	
	public T Get(int i)
	{
		if(data != null && i < data.size())
			return data.get(i);
		return null;
	}
	
	public int DataCount()
	{
		return data != null ? data.size() : 0;
	}
	
	public boolean HasNext()
	{
		return m_pageNo < m_pageCount;
	}

	public int NextPageNo()
	{
		if(HasNext())
			return m_pageNo + 1;
		return 0;
	}

	public void CalePageCount()
	{
		int pc = 0; // (int)Math.ceil((float)m_pageCount / (float)m_pageSize);
		int pcb = m_totalCount / m_pageSize;
		int pce = (m_totalCount != pcb * m_pageSize) ? 1 : 0;
		pc = pcb + pce;
		SetPageCount(pc);
	}

	public void NextPage()
	{
		int pn = NextPageNo();
		if(pn > 0)
			SetPageNo(NextPageNo());
	}
	
	public Paged<T> Push(T t)
	{
		if(data == null)
			data = new ArrayList<T>();
		data.add(t);
		return this;
	}

	public Paged<T> Push(List<T> other)
	{
		if(data == null)
			data = new ArrayList<T>();
		data.addAll(other);
		return this;
	}
	
	public Paged<T> Clear()
	{
		if(data != null)
			data.clear();
		return this;
	}
	
	public Paged<T> Reset()
	{
		Clear();
		SetPageNo(1);
		SetPageCount(0);
		SetTotalCount(0);
		return this;
	}

	@Override
	public String toString()
	{
		return String.format("%d/%d, total %d items", m_pageNo, m_pageCount, m_totalCount);
	}
}
