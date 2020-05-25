package com.youtushuju.lingdongapp.device;

import com.youtushuju.lingdongapp.common.Common;

public class SerialRespStruct extends JsonDataStruct {
    public String type;
    public String res;
    public String token;
    public String device_id;

    public SerialRespStruct()
    {
        super();
    }

    public SerialRespStruct(String type, String device_id, String token, String res)
    {
        super();
        this.type = type;
        this.device_id = device_id;
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
                && !Common.StringIsBlank(device_id)
                && !Common.StringIsBlank(token)
                && !Common.StringIsBlank(res)
                ;
    }

    @Override
    public String toString()
    {
        return String.format("[%s]: Type(%s), DeviceID(%s), Token(%s), Res(%s)",
                getClass().getName(), type, device_id, token, res);
    }

}
