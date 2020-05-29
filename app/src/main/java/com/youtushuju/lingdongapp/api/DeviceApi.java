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

    private static final String ID_URL_HOST = "47.95.238.76:2018";
    //private static final String ID_URL_HOST = "www.iqust.top";
    private static final String ID_URL_PATH = "/api/device";
    private static final int ID_REQUEST_TIMEOUT = 10000;

    private DeviceApi() {}

    public interface OnResponse
    {
        public void HandleResponse(NetworkReply reply);
    }

    public static DeviceApiResp VerifyFace(String imei, String image)
    {
        NetworkAccessManager manager;
        NetworkRequest req;
        NetworkReply reply;
        DeviceApiResp resp;
        DeviceApiReq apiReq;

        apiReq = new DeviceApiReq(DeviceApiDef.ID_DEVICE_API_METHOD_FACE_LOGIN, imei);
        apiReq.AddData("base64", "DEBUG_IMAGE_BASE64_CODE......");
        String json = apiReq.Dump();
        Logf.d(ID_TAG, "人脸验证请求数据(%s)", json);
        apiReq.AddData("base64", image);
        json = apiReq.Dump();

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

    public static DeviceApiResp UploadWeight(String imei, String id, String weightOld, String weightNew, String weightAll, String res, String deviceId)
    {
        NetworkAccessManager manager;
        NetworkRequest req;
        NetworkReply reply;
        DeviceApiResp resp;
        DeviceApiReq apiReq;

        apiReq = new DeviceApiReq(DeviceApiDef.ID_DEVICE_API_METHOD_RUBBISH_RECORD, imei);
        apiReq.AddData("weightOld", weightOld);
        apiReq.AddData("weightNew", weightNew);
        apiReq.AddData("weightAll", weightAll);
        apiReq.AddData("userId", id);
        apiReq.AddData("dropChannel", deviceId);
        apiReq.AddData("dropStatus", res);
        String json = apiReq.Dump();
        Logf.d(ID_TAG, "上报重量请求数据(%s)", json);

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

    public static DeviceApiResp Heartbeat(String imei, String code, String info)
    {
        NetworkAccessManager manager;
        NetworkRequest req;
        NetworkReply reply;
        DeviceApiResp resp;
        DeviceApiReq apiReq;

        apiReq = new DeviceApiReq(DeviceApiDef.ID_DEVICE_API_METHOD_DEVICE_STATUS, imei);
        apiReq.AddData("statusCode", code);
        apiReq.AddData("statusInfo", info);
        String json = apiReq.Dump();
        Logf.d(ID_TAG, "心跳请求数据(%s)", json);

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
