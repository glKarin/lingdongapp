package com.youtushuju.lingdongapp.api;

import com.youtushuju.lingdongapp.common.Common;
import com.youtushuju.lingdongapp.common.Logf;
import com.youtushuju.lingdongapp.device.SerialDataDef;
import com.youtushuju.lingdongapp.network.NetworkAccessManager;
import com.youtushuju.lingdongapp.network.NetworkReply;
import com.youtushuju.lingdongapp.network.NetworkRequest;

import java.util.HashMap;
import java.util.Map;

public final class DeviceApiDef {
    public static final int ID_ERROR_CODE_SUCCESS = 0;
    public static final int ID_ERROR_CODE_PARAMETER_ERROR = 1;
    public static final int ID_ERROR_CODE_API_NOT_FOUND_ERROR = 404;
    public static final int ID_ERROR_CODE_SERVER_ERROR = 500;
    public static final int ID_ERROR_CODE_ERROR = -1;

    // UNUSED: c字段
    public static final String ID_DEVICE_API_COMMAND_VERIFY_FACE = "113";
    public static final String ID_DEVICE_API_COMMAND_UPLOAD_FACE = "27";

    public static final String ID_DEVICE_API_METHOD_FACE_LOGIN = "FaceLogin";
    public static final String ID_DEVICE_API_METHOD_DEVICE_STATUS = "DeviceStatus";
    public static final String ID_DEVICE_API_METHOD_RUBBISH_RECORD = "RubbishRecord";
    public static final String ID_DEVICE_API_METHOD_CLEAR_RUBBISH = "ClearRubbish";
    public static final String ID_DEVICE_API_METHOD_BASE = "Base";
    public static final String ID_DEVICE_API_METHOD_QRCODE_LOGIN = "QrcodeLogin";

    public static final String ID_KITCHEN_WASTE_DOOR_ID = SerialDataDef.ID_DOOR_ID_1; // 厨余垃圾
    public static final String ID_OTHER_WASTE_DOOR_ID = SerialDataDef.ID_DOOR_ID_2; // 其他垃圾
    public static final String ID_KITCHEN_WASTE_RECYCLE_DOOR_ID = SerialDataDef.ID_DOOR_ID_3; // 厨余垃圾回收
    public static final String ID_OTHER_WASTE_RECYCLE_DOOR_ID = SerialDataDef.ID_DOOR_ID_4; // 其他垃圾回收

    private DeviceApiDef() {}

}
