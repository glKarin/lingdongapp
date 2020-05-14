package com.youtushuju.lingdongapp.device;

import com.youtushuju.lingdongapp.common.Common;

import java.text.SimpleDateFormat;
import java.util.Date;

public class SerialReqStruct extends JsonDataStruct {
    public String type;
    public String door_id;
    public String time; // yyyyMMddHHmm
    public String token;

    public SerialReqStruct()
    {
        super();
    }

    public SerialReqStruct(String type)
    {
        super();
        this.type = type;
        Finish();
    }

    public SerialReqStruct(String type, String door_id)
    {
        super();
        this.type = type;
        this.door_id = door_id;
        Finish();
    }

    public void Finish()
    {
        this.time = CurrentTime();
        this.token = SerialDataDef.ID_SERIAL_DATA_DEFAULT_TOKEN;
    }

    public boolean IsValid()
    {
        return !Common.StringIsBlank(type)
                && !Common.StringIsBlank(door_id)
                && !Common.StringIsBlank(time)
                && !Common.StringIsBlank(token)
                ;
    }

    @Override
    public String toString()
    {
        return String.format("[%s]: Type(%s), Door(%s), Time(%s), Token(%s)",
                getClass().getName(), type, door_id, time, token);
    }

    public static String CurrentTime()
    {
        SimpleDateFormat format;

        format = new SimpleDateFormat("yyyyMMddHHmm");
        return format.format(new Date());
    }
}
