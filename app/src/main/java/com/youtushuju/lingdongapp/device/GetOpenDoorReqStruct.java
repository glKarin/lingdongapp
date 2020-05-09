package com.youtushuju.lingdongapp.device;

public final class GetOpenDoorReqStruct extends SerialReqStruct {

    public GetOpenDoorReqStruct()
    {
        super(SerialDataDef.ID_SERIAL_DATA_TYPE_PUT_OPEN_DOOR);
    }

    public GetOpenDoorReqStruct(String doorId)
    {
        super(SerialDataDef.ID_SERIAL_DATA_TYPE_GET_OPEN_DOOR, doorId);
    }
}
