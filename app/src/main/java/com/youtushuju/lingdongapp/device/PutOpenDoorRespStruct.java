package com.youtushuju.lingdongapp.device;

public final class PutOpenDoorRespStruct extends SerialRespStruct {
    public String weight;

    public PutOpenDoorRespStruct()
    {
        super();
    }

    public PutOpenDoorRespStruct(String res, String weight, String token)
    {
        super(SerialDataDef.ID_SERIAL_DATA_TYPE_PUT_OPEN_DOOR, res, token);
        this.weight = weight;
    }

    @Override
    public boolean IsSuccess()
    {
        return super.IsSuccess() && Integer.parseInt(res) == 1;
    }
}
