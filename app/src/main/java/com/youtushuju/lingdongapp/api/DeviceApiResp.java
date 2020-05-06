package com.youtushuju.lingdongapp.api;

import com.youtushuju.lingdongapp.common.Common;
import com.youtushuju.lingdongapp.common.Logf;
import com.youtushuju.lingdongapp.json.JSON;
import com.youtushuju.lingdongapp.json.JsonMap;
import com.youtushuju.lingdongapp.json.JsonResult;

public class DeviceApiResp {
    private static final String ID_TAG = "DeviceApiResp";
    private static final int ID_DEFAULT_ERROR_CODE = -127;

    public String json;
    public int error_code = ID_DEFAULT_ERROR_CODE;
    public String error_msg;
    public Object data;

    public DeviceApiResp()
    {
    }

    public boolean Restore(String json)
    {
        this.json = json;
        Logf.d(ID_TAG, "响应json(%s)", json);
        Reset();

        if(Common.StringIsBlank(json))
        {
            Logf.e(ID_TAG, "响应json为空");
            return false;
        }
        JsonResult result = JSON.Parse(json);
        if(result == null)
        {
            Logf.e(ID_TAG, "解析json错误");
            return false;
        }
        if(!(result instanceof JsonMap))
        {
            Logf.e(ID_TAG, "json格式错误");
            return false;
        }
        JsonMap map = (JsonMap)result;
        try
        {
            error_code = (int)map.Get("error_code");
            error_msg = map.<String>GetT("error_msg");
            data = map.<JsonResult>GetT("data");
        }
        catch (Throwable e)
        {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public static DeviceApiResp FromJson(String json)
    {
        DeviceApiResp resp = new DeviceApiResp();
        if(resp.Restore(json))
            return resp;
        return null;
    }

    public boolean IsSuccess()
    {
        return error_code == DeviceApi.ID_ERROR_CODE_SUCCESS;
    }

    private void Reset()
    {
        error_code = ID_DEFAULT_ERROR_CODE;
        error_msg = null;
        data = null;
    }

    public String toString()
    {
        return "Response -> " + json;
    }
}
