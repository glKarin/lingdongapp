package com.youtushuju.lingdongapp.device;

public final class GetOpenDoorRespStruct extends SerialRespStruct {
    public GetOpenDoorRespStruct()
    {
        super();
    }

    public GetOpenDoorRespStruct(String res, String token)
    {
        super(SerialDataDef.ID_SERIAL_DATA_TYPE_GET_OPEN_DOOR, res, token);
    }

    @Override
    public boolean IsSuccess()
    {
        return super.IsSuccess() && Integer.parseInt(res) == 1;
    }
}
