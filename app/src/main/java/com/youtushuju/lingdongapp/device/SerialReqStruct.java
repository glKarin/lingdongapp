package com.youtushuju.lingdongapp.device;

import com.youtushuju.lingdongapp.common.Common;

import java.text.SimpleDateFormat;
import java.util.Date;

public class SerialReqStruct extends JsonDataStruct {
    public String type;
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

    public void Finish()
    {
        this.time = CurrentTime();
        this.token = SerialDataDef.ID_SERIAL_DATA_DEFAULT_TOKEN;
    }

    public boolean IsValid()
    {
        return !Common.StringIsBlank(type)
                && !Common.StringIsBlank(time)
                && !Common.StringIsBlank(token)
                ;
    }

    @Override
    public String toString()
    {
        return String.format("[%s]: Type(%s), Time(%s), Token(%s)",
                getClass().getName(), type, time, token);
    }

    public static String CurrentTime()
    {
        SimpleDateFormat format;

        format = new SimpleDateFormat("yyyyMMddHHmm");
        return format.format(new Date());
    }
}
