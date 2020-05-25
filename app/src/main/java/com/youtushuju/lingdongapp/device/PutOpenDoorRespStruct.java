package com.youtushuju.lingdongapp.device;

public final class PutOpenDoorRespStruct extends SerialRespStruct {
    public String weight;

    public PutOpenDoorRespStruct()
    {
        super();
    }

    public PutOpenDoorRespStruct(String deviceId, String weight, String token, String res)
    {
        super(SerialDataDef.ID_SERIAL_DATA_TYPE_PUT_OPEN_DOOR, deviceId, token, res);
        this.weight = weight;
    }

    @Override
    public boolean IsSuccess()
    {
        return super.IsSuccess() && Integer.parseInt(res) == 1;
    }

    @Override
    public String toString()
    {
        return super.toString() + String.format(", weight(%s)", weight);
    }

    @Override
    public boolean IsValid() {
        return super.IsValid()
                //&& weight != null
                ;
    }
}
