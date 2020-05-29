package com.youtushuju.lingdongapp.api;

import com.youtushuju.lingdongapp.common.Common;
import com.youtushuju.lingdongapp.common.Logf;
import com.youtushuju.lingdongapp.json.JSON;
import com.youtushuju.lingdongapp.json.JsonMap;
import com.youtushuju.lingdongapp.json.JsonResult;

import java.util.HashMap;
import java.util.Map;

public class DeviceApiReq {
    private static final String ID_TAG = "DeviceApiReq";

    public String method;
    public String imei;
    public Map<String, Object> data;

    public DeviceApiReq()
    {
    }

    public DeviceApiReq(String method, String imei)
    {
        this.method = method;
        this.imei = imei;
    }

    public DeviceApiReq AddData(String name, Object value)
    {
        if(data == null)
            data = new HashMap<String, Object>();
        data.put(name, value);
        return this;
    }

    public String Dump()
    {
        JsonMap map = new JsonMap();
        map.put("method", method);
        map.put("imei", imei);
        map.put("data", JsonMap.FromMap(data));
        return JSON.Stringify(map);
    }

    private void Reset()
    {
        method = null;
        imei = null;
        data = null;
    }

    public String toString()
    {
        return "Request -> " + Dump();
    }
}
