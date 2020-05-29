package com.youtushuju.lingdongapp.device;

public final class PutOpenDoorRespStruct extends SerialRespStruct {
    public String weightOld;
    public String weightNew;
    public String weightAll;

    public PutOpenDoorRespStruct()
    {
        super();
    }

    public PutOpenDoorRespStruct(String deviceId, String token, String res, String weightOld, String weightNew, String weightAll)
    {
        super(SerialDataDef.ID_SERIAL_DATA_TYPE_PUT_OPEN_DOOR, deviceId, token, res);
        this.weightOld = weightOld;
        this.weightNew = weightNew;
        this.weightAll = weightAll;
    }
/*
    @Override
    public boolean IsSuccess()
    {
        return super.IsSuccess() && Integer.parseInt(res) == 1;
    }
    */

    @Override
    public String toString()
    {
        return super.toString() + String.format(", weightOld(%s), weightNew(%s), weightAll(%s)", weightOld, weightNew, weightAll);
    }

    @Override
    public boolean IsValid() {
        return super.IsValid()
                //&& weight != null
                ;
    }
}
