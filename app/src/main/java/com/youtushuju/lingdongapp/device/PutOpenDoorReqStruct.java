package com.youtushuju.lingdongapp.device;

public final class PutOpenDoorReqStruct extends SerialReqStruct {

    public PutOpenDoorReqStruct()
    {
        super();
    }

    public PutOpenDoorReqStruct(String doorId)
    {
        super(SerialDataDef.ID_SERIAL_DATA_TYPE_PUT_OPEN_DOOR, doorId);
    }
}
