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

    public SerialRespStruct(String type, String device_id, String token)
    {
        super();
        this.type = type;
        this.device_id = device_id;
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
                ;
    }

    @Override
    public String toString()
    {
        return String.format("[%s]: Type(%s), DeviceID(%s), Token(%s)",
                getClass().getName(), type, device_id, token);
    }

}
