package com.youtushuju.lingdongapp.device;

import com.youtushuju.lingdongapp.common.Common;

public final class PutOpenDoorRespStruct extends SerialRespStruct {
    public static final String CONST_RES_SUCCESS = "01"; // 成功
    public static final String CONST_RES_FAIL = "00"; // 失败
    public static final String CONST_RES_FULL = "02"; // 满箱

    public String device_id;
    public String weightOld;
    public String weightNew;
    public String weightAll;

    public PutOpenDoorRespStruct()
    {
        super(SerialDataDef.ID_SERIAL_DATA_TYPE_PUT_OPEN_DOOR);
    }

    public PutOpenDoorRespStruct(String deviceId, String token, String res, String weightOld, String weightNew, String weightAll)
    {
        super(SerialDataDef.ID_SERIAL_DATA_TYPE_PUT_OPEN_DOOR, token, res);
        this.device_id = deviceId;
        this.weightOld = weightOld;
        this.weightNew = weightNew;
        this.weightAll = weightAll;
    }

    public boolean IsValid()
    {
        return super.IsValid()
                && !Common.StringIsBlank(device_id)
                ;
    }

    @Override
    public boolean IsSuccess()
    {
        return super.IsSuccess() && CONST_RES_SUCCESS.equals(res);
    }

    @Override
    public String toString()
    {
        return super.toString() + String.format(", DeviceID(%s), weightOld(%s), weightNew(%s), weightAll(%s)", device_id, weightOld, weightNew, weightAll);
    }

   /* @Override
    public boolean IsValid() {
        return super.IsValid()
                //&& weight != null
                ;
    }*/
}
