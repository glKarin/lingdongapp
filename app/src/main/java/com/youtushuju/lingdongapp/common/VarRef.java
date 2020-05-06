package com.youtushuju.lingdongapp.common;

public final class VarRef<T>
{
	public T ref;

	public VarRef(){}
	public VarRef(T t)
	{
		ref = t;
	}

	public T Ref(T t)
	{
	    T old = ref;
	    ref = t;
	    return old;
	}

    public T Unref()
    {
        T old = ref;
        ref = null;
        return old;
    }
}
