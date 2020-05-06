package com.youtushuju.lingdongapp.json;

public interface JsonMap extends JsonResult
{
	public int GetType(String name);
	public Object Get(String name);
	public JsonMap Put(String name, Object value);
	public<T> T GetT(String name);
	public String [] /* Set<String> */ GetKeys();
}

