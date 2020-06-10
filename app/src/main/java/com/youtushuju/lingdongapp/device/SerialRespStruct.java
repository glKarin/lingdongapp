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

    public SerialRespStruct(String type)
    {
        this.type = type;
    }

    public SerialRespStruct(String type, String token, String res)
    {
        this(type);
        this.token = token;
        this.res = res;
    }

    public boolean IsSuccess()
    {
        return IsValid();
    }

    public boolean IsValid()
    {
        return !Common.StringIsBlank(type)
                && !Common.StringIsBlank(token)
                && !Common.StringIsBlank(res)
                ;
    }

    @Override
    public String toString()
    {
        return String.format("[%s]: Type(%s), Token(%s), Res(%s)",
                getClass().getName(), type, token, res);
    }

}
