package com.youtushuju.lingdongapp.database;

import android.content.ContentValues;

import java.util.Map;

public abstract class DBModel_base {
    public static final String ID_DEFAULT_ID_COLUMN_NAME = "id";
    protected String m_idColumnName = ID_DEFAULT_ID_COLUMN_NAME;

    public abstract ContentValues MakeContentValues(boolean includeId);
}
