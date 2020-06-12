package com.youtushuju.lingdongapp.api;

import android.graphics.Bitmap;

import com.youtushuju.lingdongapp.common.Common;

public class UserModel {
    public static final String ENUM_USER_ROLE_NORMAL = "0"; // 普通用户
    public static final String ENUM_USER_ROLE_MAINTENANCE = "1"; // 维护人员
    public static final String ENUM_USER_ROLE_ADMIN = "2"; // 管理员

    public static final String CONST_TEST_USER_ID = "1"; // 测试人员ID

    private String m_id = null;
    private String m_username = null;
    private String m_usericon = null;
    private Bitmap m_photo = null;
    private String m_isEmployee = ENUM_USER_ROLE_NORMAL;

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

    public void SetIsEmployee(String i)
    {
        m_isEmployee = i;
    }

    public String IsEmployee()
    {
        return m_isEmployee;
    }

    public void SetId(String id)
    {
        m_id = id;
    }

    public boolean IsValid()
    {
        return m_id != null;
    }

    public boolean IsAdministrator()
    {
        return ContainRole(ENUM_USER_ROLE_ADMIN);
    }

    public boolean IsMaintenance()
    {
        return ContainRole(ENUM_USER_ROLE_MAINTENANCE);
    }

    public boolean IsNormal()
    {
        return ContainRole(ENUM_USER_ROLE_NORMAL);
    }

    private boolean ContainRole(String role)
    {
        if(Common.StringIsEmpty(m_isEmployee))
            return false;
        String roles[] = m_isEmployee.split(",");
        for(String r : roles)
        {
            if(role.equals(r))
                return true;
        }
        return false;
    }

    public String GetRoleName()
    {
        if(IsAdministrator())
            return "管理员";
        if(IsMaintenance())
            return "清维人员";
        if(IsNormal())
            return "普通用户";
        if(!Common.StringIsEmpty(m_isEmployee))
            return "其他人员(" + m_isEmployee + ")";
        return "未识别人员";
    }

    public String Usericon()
    {
        return m_usericon;
    }

    public void SetUsericon(String usericon)
    {
        m_usericon = usericon;
    }
}
