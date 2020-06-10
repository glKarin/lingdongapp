package com.youtushuju.lingdongapp.device;

public final class DropModeRespStruct extends SerialRespStruct {

    public DropModeRespStruct()
    {
        super(SerialDataDef.ID_SERIAL_DATA_TYPE_DROP_MODE);
    }

    public DropModeRespStruct(String token, String res)
    {
        super(SerialDataDef.ID_SERIAL_DATA_TYPE_DROP_MODE, token, res);
    }
}
