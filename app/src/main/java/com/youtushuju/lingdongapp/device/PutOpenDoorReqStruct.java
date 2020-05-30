package com.youtushuju.lingdongapp.device;

import com.youtushuju.lingdongapp.common.Common;

public final class PutOpenDoorReqStruct extends SerialReqStruct {
    public String door_id;

    public PutOpenDoorReqStruct()
    {
        super(SerialDataDef.ID_SERIAL_DATA_TYPE_PUT_OPEN_DOOR);
    }

    public PutOpenDoorReqStruct(String doorId)
    {
        super(SerialDataDef.ID_SERIAL_DATA_TYPE_PUT_OPEN_DOOR);
        this.door_id = doorId;
    }

    public boolean IsValid()
    {
        return super.IsValid()
                && !Common.StringIsBlank(door_id)
                ;
    }

    @Override
    public String toString()
    {
        return super.toString() + String.format(", Door(%s)", door_id);
    }
}
