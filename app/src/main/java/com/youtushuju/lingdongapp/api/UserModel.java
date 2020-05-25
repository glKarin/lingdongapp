package com.youtushuju.lingdongapp.api;

import android.graphics.Bitmap;

public class UserModel {
    private String m_id = null;
    private String m_username = null;
    private Bitmap m_photo = null;

    public UserModel()
    {
    }

    public UserModel(String id, String username, Bitmap photo)
    {
        m_id = id;
        m_username = username;
        m_photo = photo;
    }

    public String Username()
    {
        return m_username;
    }

    public void SetUsername(String username)
    {
        m_username = username;
    }

    public Bitmap Photo()
    {
        return m_photo;
    }

    public void SetPhoto(Bitmap photo)
    {
        if(m_photo != photo)
        {
            // 不维护Bitmap内存
            /*if(m_photo != null && !m_photo.isRecycled())
                m_photo.recycle();*/
            m_photo = photo;
        }
    }

    public String Id()
    {
        return m_id;
    }

    public void SetId(String id)
    {
        m_id = id;
    }

    public boolean IsValid()
    {
        return m_id != null;
    }
}
