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

public final class DeviceApi {
    private static final String ID_TAG = "DeviceApi";

    //private static final String ID_URL_HOST = "47.95.238.76:2018";
    //private static final String ID_URL_HOST = "www.iqust.top";
    private static final String ID_URL_HOST = "lingx.iqust.top/garbage";
    private static final String ID_URL_PATH = "/api/device";

    //private static final String ID_URL_SCHEME = "http";
    private static final String ID_URL_SCHEME = "https";

    private static final int ID_REQUEST_TIMEOUT = 10000;

    private DeviceApi() {}

    public interface OnResponse
    {
        public void HandleResponse(NetworkReply reply);
    }

    private static final String CONST_API_URL = ID_URL_SCHEME + "://" + ID_URL_HOST + ID_URL_PATH;
    private static String APIUrl()
    {
        return CONST_API_URL;
        //return "http://" + ID_URL_HOST + ID_URL_PATH;
    }

    // 人脸识别
    public static DeviceApiResp VerifyFace(String imei, String image)
    {
        DeviceApiReq apiReq;

        apiReq = new DeviceApiReq(DeviceApiDef.ID_DEVICE_API_METHOD_FACE_LOGIN, imei);
        apiReq.AddData("base64", "DEBUG_IMAGE_BASE64_CODE......");
        Logf.d(ID_TAG, "人脸验证请求数据(%s)", apiReq.Dump());
        apiReq.AddData("base64", image);
        DeviceApiResp resp = ApiRequest(apiReq);
        return resp;
    }

    // 上报投递结果
    public static DeviceApiResp UploadWeight(String imei, String id, String weightOld, String weightNew, String weightAll, String res, String deviceId)
    {
        DeviceApiReq apiReq;

        apiReq = new DeviceApiReq(DeviceApiDef.ID_DEVICE_API_METHOD_RUBBISH_RECORD, imei, "上报重量");
        apiReq.AddData("weightOld", weightOld);
        apiReq.AddData("weightNew", weightNew);
        apiReq.AddData("weightAll", weightAll);
        apiReq.AddData("userId", id);
        apiReq.AddData("dropChannel", deviceId);
        apiReq.AddData("dropStatus", res);
        DeviceApiResp resp = ApiRequest(apiReq);
        return resp;
    }

    // 心跳同步
    // {"error_code":0,"error_msg":"success","data":{"id":1,"name":"设备一","imei":"863412048794880","address":"山东省威海市环翠区","deviceStatus":"1","deviceGroup":"1","heartbeatTime":30,"version":"1","dropmode":"[01,02,03]","setuptime":"202005280921"}}
    public static DeviceApiResp Heartbeat(String imei, String code, String info)
    {
        DeviceApiReq apiReq;

        apiReq = new DeviceApiReq(DeviceApiDef.ID_DEVICE_API_METHOD_DEVICE_STATUS, imei, "心跳");
        apiReq.AddData("statusCode", code);
        apiReq.AddData("statusInfo", info);
        DeviceApiResp resp = ApiRequest(apiReq);
        return resp;
    }

    // 清运设备
    public static DeviceApiResp CleanDevice(String imei, String id, String weightOld, String weightNew, String weightAll, String res, String deviceId)
    {
        DeviceApiReq apiReq;

        apiReq = new DeviceApiReq(DeviceApiDef.ID_DEVICE_API_METHOD_CLEAR_RUBBISH, imei, "清运设备");
        apiReq.AddData("weightOld", weightOld);
        apiReq.AddData("weightNew", weightNew);
        apiReq.AddData("weightAll", weightAll);
        apiReq.AddData("employee", id);
        apiReq.AddData("clearChannel", deviceId);
        apiReq.AddData("clearStatus", res);
        DeviceApiResp resp = ApiRequest(apiReq);
        return resp;
    }

    // 设置投放模式
    public static DeviceApiResp SyncConfig(String imei, int version, String dropMode[], String setUpTime, int heartbeatTime, String res)
    {
        DeviceApiReq apiReq;

        apiReq = new DeviceApiReq(DeviceApiDef.ID_DEVICE_API_METHOD_BASE, imei, "设置投放模式");
        apiReq.AddData("version", version);
        apiReq.AddData("dropMode", dropMode);
        apiReq.AddData("setUpTime", setUpTime);
        apiReq.AddData("heartbeatTime", heartbeatTime);
        DeviceApiResp resp = ApiRequest(apiReq);
        return resp;
    }

    // 二维码登陆
    public static DeviceApiResp LoginByQRCode(String imei, String code)
    {
        DeviceApiReq apiReq;

        apiReq = new DeviceApiReq(DeviceApiDef.ID_DEVICE_API_METHOD_QRCODE_LOGIN, imei, "扫码登录");
        apiReq.AddData("code", code);
        DeviceApiResp resp = ApiRequest(apiReq);
        return resp;
    }

    private static DeviceApiResp ApiRequest(DeviceApiReq apiReq)
    {
        NetworkAccessManager manager;
        NetworkRequest req;
        NetworkReply reply;
        DeviceApiResp resp;

        // TODO: res???
        String json = apiReq.Dump();
        if(apiReq.description != null)
            Logf.d(ID_TAG, apiReq.description + "请求数据(%s)", json);

        if(Common.StringIsBlank(json))
            return null;

        manager = new NetworkAccessManager();
        manager.SetTimeout(ID_REQUEST_TIMEOUT);
        String url = APIUrl();

        req = new NetworkRequest(url);
        req.AddHeader("Content-type", "application/json");
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

    public static DeviceApiReq CreateRequest(String method, String imei, Object...args)
    {
        DeviceApiReq req = new DeviceApiReq(method, imei);
        for (int i = 0; i < args.length; i += 2)
        {
            String name = args[i].toString();
            Object value = args[i + 1];
            req.AddData(name, value);
        }
        return req;
    }

    public static String MakeRequestJson(String method, String imei, Object...args)
    {
        return CreateRequest(method, imei, args).Dump();
    }
}
