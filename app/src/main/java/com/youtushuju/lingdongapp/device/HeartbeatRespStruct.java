package com.youtushuju.lingdongapp.device;

public final class HeartbeatRespStruct extends SerialRespStruct {
    public static final String CONST_RES_OFFLINE = "00"; // 离线
    public static final String CONST_RES_ONLINE = "01"; // 设备在线
    public static final String CONST_RES_BROKEN = "02"; // 设备故障
    public static final String CONST_RES_OVERFLOW = "03"; // 设备满溢
    public static final String CONST_RES_EXCEPTION_ALARM = "04"; // 重量异常报警

    public HeartbeatRespStruct()
    {
        super();
    }

    public HeartbeatRespStruct(String token, String res)
    {
        super(SerialDataDef.ID_SERIAL_DATA_TYPE_HEARTBEAT, token, res);
    }

    public static String GetDeviceStatusName(String res)
    {
        if(CONST_RES_OFFLINE.equals(res))
            return "离线";
        if(CONST_RES_ONLINE.equals(res))
            return "设备在线";
        if(CONST_RES_BROKEN.equals(res))
            return "设备故障";
        if(CONST_RES_OVERFLOW.equals(res))
            return "设备满溢";
        if(CONST_RES_EXCEPTION_ALARM.equals(res))
            return "重量异常报警";
        return "";
    }

    public static boolean DeviceIsNormal(String status)
    {
        return HeartbeatRespStruct.CONST_RES_ONLINE.equals(status);
    }
}
