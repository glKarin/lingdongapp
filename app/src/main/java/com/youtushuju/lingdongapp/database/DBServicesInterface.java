package com.youtushuju.lingdongapp.database;

public interface DBServicesInterface<T> {
    public Paged<T> List(DBCond cond, DBOrder order, Paged<T> paged);
    public boolean Add(T item);
    public boolean DeleteById(long id);
    public int DeleteAll();
    public int Count(DBCond cond, DBOrder order);
}
