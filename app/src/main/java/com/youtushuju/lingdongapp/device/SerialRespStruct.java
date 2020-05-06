package com.youtushuju.lingdongapp.device;

import com.youtushuju.lingdongapp.common.Common;

public class SerialRespStruct extends JsonDataStruct {
    public String type;
    public String res;
    public String token;

    public SerialRespStruct()
    {
        super();
    }

    public SerialRespStruct(String type, String res, String token)
    {
        super();
        this.type = type;
        this.res = res;
        this.token = token;
    }

    public boolean IsSuccess()
    {
        return IsValid();
    }

    public boolean IsValid()
    {
        return !Common.StringIsBlank(type)
                && !Common.StringIsBlank(res)
                && !Common.StringIsBlank(token)
                ;
    }

    @Override
    public String toString()
    {
        return String.format("[%s]: Type(%d), Res(%s), Token(%s)",
                getClass().getName(), type, res, token);
    }

}
