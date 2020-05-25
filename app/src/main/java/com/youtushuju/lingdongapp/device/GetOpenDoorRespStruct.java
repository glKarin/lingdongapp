package com.youtushuju.lingdongapp.device;

public final class GetOpenDoorRespStruct extends SerialRespStruct {
    public GetOpenDoorRespStruct()
    {
        super();
    }

    public GetOpenDoorRespStruct(String token, String res)
    {
        super(SerialDataDef.ID_SERIAL_DATA_TYPE_GET_OPEN_DOOR, null, token, res);
    }

    @Override
    public boolean IsSuccess()
    {
        return super.IsSuccess() && Integer.parseInt(res) == 1;
    }
}
