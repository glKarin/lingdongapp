package com.youtushuju.lingdongapp.device;

import com.youtushuju.lingdongapp.common.Common;

public final class DropModeReqStruct extends SerialReqStruct {
    public String dropSetMode;

    public DropModeReqStruct()
    {
        super(SerialDataDef.ID_SERIAL_DATA_TYPE_DROP_MODE);
    }

    public DropModeReqStruct(String dropMode)
    {
        super(SerialDataDef.ID_SERIAL_DATA_TYPE_DROP_MODE);
        dropSetMode = dropMode;
    }

    @Override
    public boolean IsValid() {
        return super.IsValid() && !Common.StringIsEmpty(dropSetMode);
    }
}
