package com.youtushuju.lingdongapp.device;

public final class SmartCodeRespStruct extends SerialRespStruct {
    public String code;

    public SmartCodeRespStruct()
    {
        super(SerialDataDef.ID_SERIAL_DATA_TYPE_SMART_CODE);
    }

    public SmartCodeRespStruct(String token, String res)
    {
        super(SerialDataDef.ID_SERIAL_DATA_TYPE_SMART_CODE, token, res);
    }
}
