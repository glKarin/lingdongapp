package com.youtushuju.lingdongapp.api;

import com.youtushuju.lingdongapp.common.Common;
import com.youtushuju.lingdongapp.common.Logf;
import com.youtushuju.lingdongapp.device.SerialDataDef;
import com.youtushuju.lingdongapp.json.JSON;
import com.youtushuju.lingdongapp.json.JsonMap;
import com.youtushuju.lingdongapp.network.NetworkAccessManager;
import com.youtushuju.lingdongapp.network.NetworkReply;
import com.youtushuju.lingdongapp.network.NetworkRequest;

import java.util.HashMap;
import java.util.Map;

public class DeviceApi {
    private static final String ID_TAG = "DeviceApi";

    private static final String ID_URL_HOST = "47.95.238.76:2018";
    //private static final String ID_URL_HOST = "www.iqust.top";
    private static final String ID_URL_PATH = "/api/device";
    private static final int ID_REQUEST_TIMEOUT = 10000;

    public static final int ID_ERROR_CODE_SUCCESS = 0;
    public static final int ID_ERROR_CODE_PARAMETER_ERROR = 1;
    public static final int ID_ERROR_CODE_API_NOT_FOUND_ERROR = 404;
    public static final int ID_ERROR_CODE_SERVER_ERROR = 500;
    public static final int ID_ERROR_CODE_ERROR = -1;

    public static final String ID_DEVICE_API_COMMAND_VERIFY_FACE = "113";
    public static final String ID_DEVICE_API_COMMAND_UPLOAD_FACE = "27";

    public static final String ID_KITCHEN_WASTE_DOOR_ID = SerialDataDef.ID_DOOR_ID_1; // 厨余垃圾
    public static final String ID_OTHER_WASTE_DOOR_ID = SerialDataDef.ID_DOOR_ID_2; // 其他垃圾
    public static final String ID_KITCHEN_WASTE_RECYCLE_DOOR_ID = SerialDataDef.ID_DOOR_ID_3; // 厨余垃圾回收
    public static final String ID_OTHER_WASTE_RECYCLE_DOOR_ID = SerialDataDef.ID_DOOR_ID_4; // 其他垃圾回收

    private DeviceApi() {}

    public interface OnResponse
    {
        public void HandleResponse(NetworkReply reply);
    }

    public static DeviceApiResp VerifyFace(String imei, String image)
    {
        JsonMap data;
        NetworkAccessManager manager;
        NetworkRequest req;
        NetworkReply reply;
        DeviceApiResp resp;

        data = new JsonMap();
        data.put("c", ID_DEVICE_API_COMMAND_VERIFY_FACE);
        data.put("imei", imei);
        data.put("m", "IMAGE_BASE64_CODE......");
        String json = JSON.Stringify(data);
        Logf.d(ID_TAG, "人脸验证请求数据(%s)", json);
        data.put("m", image);

        json = JSON.Stringify(data);

        if(Common.StringIsBlank(json))
            return null;

        manager = new NetworkAccessManager();
        manager.SetTimeout(ID_REQUEST_TIMEOUT);
        String url = "http://" + ID_URL_HOST + ID_URL_PATH;
        Map<String, String> headers = new HashMap<String, String>();

        req = new NetworkRequest(url);
        req.AddHeader("Content-type", "application/json");
        //Logf.d(ID_TAG, "人脸验证请求数据(%s)", json);
        reply = manager.SyncPost(req,
                //json.getBytes()
                Common.String8BitsByteArray(json) // TODO: 8bits
        );

        if(reply == null)
            return null;
        String respJson = new String(reply.GetReplyData());
        resp = DeviceApiResp.FromJson(respJson);
        return resp;
    }

    public static DeviceApiResp UploadWeight(String imei, String id, String weight, String res, String deviceId)
    {
        JsonMap data;
        NetworkAccessManager manager;
        NetworkRequest req;
        NetworkReply reply;
        DeviceApiResp resp;

        data = new JsonMap();
        data.put("c", ID_DEVICE_API_COMMAND_UPLOAD_FACE);
        data.put("imei", imei);
        data.put("n", id);
        data.put("m", weight);
        data.put("res", res);
        data.put("device_id", deviceId);
        String json = JSON.Stringify(data);
        Logf.d(ID_TAG, "上报重量请求数据(%s)", json);

        json = JSON.Stringify(data);

        if(Common.StringIsBlank(json))
            return null;

        manager = new NetworkAccessManager();
        manager.SetTimeout(ID_REQUEST_TIMEOUT);
        String url = "http://" + ID_URL_HOST + ID_URL_PATH;
        Map<String, String> headers = new HashMap<String, String>();

        req = new NetworkRequest(url);
        req.AddHeader("Content-type", "application/json");
        //Logf.d(ID_TAG, "上报重量请求数据(%s)", json);
        reply = manager.SyncPost(req,
                //json.getBytes()
                Common.String8BitsByteArray(json) // TODO: 8bits
        );

        if(reply == null)
            return null;
        String respJson = new String(reply.GetReplyData());
        resp = DeviceApiResp.FromJson(respJson);
        return resp;
    }
}
