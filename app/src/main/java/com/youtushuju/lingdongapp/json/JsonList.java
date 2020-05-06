package com.youtushuju.lingdongapp.json;
import java.util.*;
import java.lang.reflect.*;

public interface JsonList extends JsonResult
{
	public int GetType(int index);
	public Object Get(int index);
	public JsonList Put(Object value);
	public<T> T GetT(int index);
	public int Length();
}
