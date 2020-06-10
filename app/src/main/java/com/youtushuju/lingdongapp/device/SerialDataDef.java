package com.youtushuju.lingdongapp.device;

public final class SerialDataDef {
    public static final String ID_LINGDONG_SERIAL_PATH = "/dev/ttyS1";
    public static final int ID_LINGDONG_SERIAL_BAUDRATE = 9600;

    public static final String ID_SERIAL_DATA_TYPE_PUT_OPEN_DOOR = "PutOpenDoor"; // 投放开门
    public static final String ID_SERIAL_DATA_TYPE_GET_OPEN_DOOR = "GetOpenDoor"; // 取货开门(维护专用)
    public static final String ID_SERIAL_DATA_TYPE_HEARTBEAT = "heartbeat"; // 心跳
    public static final String ID_SERIAL_DATA_TYPE_DROP_MODE = "dropMode"; // 设置投放模式
    public static final String ID_SERIAL_DATA_TYPE_SMART_CODE = "SmartCode"; // 扫码

    public static final String ID_SERIAL_DATA_DEFAULT_TOKEN = "123456"; // 固定token

    public static final String ID_DOOR_ID_1 = "01";
    public static final String ID_DOOR_ID_2 = "02";
    public static final String ID_DOOR_ID_3 = "03";
    public static final String ID_DOOR_ID_4 = "04";
    public static final String ID_DOOR_ID_5 = "05";
    public static final String ID_DOOR_ID_6 = "06";
    public static final String ID_DOOR_ID_7 = "07";
    public static final String ID_DOOR_ID_8 = "08";
    public static final String ID_DOOR_ID_9 = "09";
    public static final String ID_DOOR_ID_10 = "10";
    public static final String ID_DOOR_ID_11 = "11";
    public static final String ID_DOOR_ID_12 = "12";
}
