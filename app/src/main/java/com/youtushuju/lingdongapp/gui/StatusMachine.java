package com.youtushuju.lingdongapp.gui;

import androidx.annotation.NonNull;

import com.youtushuju.lingdongapp.common.Common;
import com.youtushuju.lingdongapp.device.HeartbeatRespStruct;
import com.youtushuju.lingdongapp.device.PutOpenDoorRespStruct;
import com.youtushuju.lingdongapp.device.SerialDataDef;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public final class StatusMachine {
    private static final String ID_TAG = "StatusMachine";
    public String device_status = HeartbeatRespStruct.CONST_RES_ONLINE; //  当前设备状态, 由心跳返回
    public Map<String, String> door_status = new HashMap<String, String>(); // 所有门状态
    // 时间戳
    public long heartbeat_timestamp = 0; // 最近心跳时间
    public long boot_timestamp = 0; // 启动时间
    public long shutdown_timestamp = 0; // 销毁时间
    // 计数
    public int heartbeat_count = 0; // 心跳总次数
    public int heartbeat_suc_count = 0; // 心跳成功次数
    public int heartbeat_err_count = 0; // 心跳失败次数
    public int verify_face_count = 0; // 刷脸总次数
    public int scan_code_count = 0; // 扫码总次数

    private static StatusMachine _instance = null;

    private StatusMachine()
    {
        Init();
    }

    public static StatusMachine Instance()
    {
        if(_instance == null)
        {
            _instance = new StatusMachine();
        }
        return _instance;
    }

    private void Init()
    {
        door_status.put(SerialDataDef.ID_DOOR_ID_1, PutOpenDoorRespStruct.CONST_RES_SUCCESS);
        door_status.put(SerialDataDef.ID_DOOR_ID_2, PutOpenDoorRespStruct.CONST_RES_SUCCESS);
        door_status.put(SerialDataDef.ID_DOOR_ID_3, PutOpenDoorRespStruct.CONST_RES_SUCCESS);
        door_status.put(SerialDataDef.ID_DOOR_ID_4, PutOpenDoorRespStruct.CONST_RES_SUCCESS);
        door_status.put(SerialDataDef.ID_DOOR_ID_5, PutOpenDoorRespStruct.CONST_RES_SUCCESS);
        door_status.put(SerialDataDef.ID_DOOR_ID_6, PutOpenDoorRespStruct.CONST_RES_SUCCESS);
        door_status.put(SerialDataDef.ID_DOOR_ID_7, PutOpenDoorRespStruct.CONST_RES_SUCCESS);
        door_status.put(SerialDataDef.ID_DOOR_ID_8, PutOpenDoorRespStruct.CONST_RES_SUCCESS);
        door_status.put(SerialDataDef.ID_DOOR_ID_9, PutOpenDoorRespStruct.CONST_RES_SUCCESS);
        door_status.put(SerialDataDef.ID_DOOR_ID_10, PutOpenDoorRespStruct.CONST_RES_SUCCESS);
        door_status.put(SerialDataDef.ID_DOOR_ID_11, PutOpenDoorRespStruct.CONST_RES_SUCCESS);
        door_status.put(SerialDataDef.ID_DOOR_ID_12, PutOpenDoorRespStruct.CONST_RES_SUCCESS);

        device_status = HeartbeatRespStruct.CONST_RES_ONLINE;
    }

    public boolean DoorIsFull(String doorId)
    {
        if(!door_status.containsKey(doorId))
            throw new IllegalArgumentException("传入门ID无效: " + doorId);
        String res = door_status.get(doorId);
        return PutOpenDoorRespStruct.CONST_RES_FULL.equals(res);
    }

    public void SetDoorStatus(String doorId, String res)
    {
        if(!door_status.containsKey(doorId))
            throw new IllegalArgumentException("传入门ID无效: " + doorId + ", 状态: " + res);
        door_status.put(doorId, res);
    }

    public boolean DeviceIsAccess()
    {
        return HeartbeatRespStruct.DeviceIsNormal(device_status);
    }

    @NonNull
    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("  * 当前设备状态: " + device_status).append("\n");

        sb.append("  * 当前门状态: ").append("\n");
        Set<String> keys = door_status.keySet();
        for (String key : keys)
            sb.append("\t" + key + ": " + door_status.get(key)).append("\n");

        sb.append("  * 程序启动时间: " + Common.TimestampToStr(boot_timestamp)).append("\n");
        sb.append("  * 程序销毁时间: " + (shutdown_timestamp > 0 ? Common.TimestampToStr(shutdown_timestamp) : "未结束")).append("\n");
        sb.append("  * 最近同步心跳时间: " + (heartbeat_timestamp > 0 ? Common.TimestampToStr(heartbeat_timestamp) : "从未同步")).append("\n");

        sb.append("  * 同步心跳次数: " + heartbeat_count).append("\n");
        sb.append("  * 同步心跳成功次数: " + heartbeat_suc_count).append("\n");
        sb.append("  * 同步心跳失败次数: " + heartbeat_err_count).append("\n");
        sb.append("  * 刷脸次数: " + verify_face_count).append("\n");
        sb.append("  * 扫码次数: " + scan_code_count).append("\n");
        return sb.toString();
    }
}
