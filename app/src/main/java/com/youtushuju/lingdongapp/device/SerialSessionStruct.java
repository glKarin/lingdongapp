package com.youtushuju.lingdongapp.device;

public final class SerialSessionStruct<T extends SerialReqStruct, U extends SerialRespStruct> {
    public T req = null;
    public U resp = null;

    public SerialSessionStruct()
    {
        super();
    }

    public SerialSessionStruct(T req, U resp)
    {
        SetSession(req, resp);
    }

    public boolean IsValid()
    {
        return req != null && resp != null;
    }

    public boolean IsValidSession()
    {
        if(!IsValid())
            return false;
        return req.IsValid() && resp.IsValid();
    }

    public boolean IsPair()
    {
        if(!IsValidSession())
            return false;
        if(!req.token.equals(resp.token))
            return false;
        return true;
    }

    public void SetSession(T req, U resp)
    {
        this.req = req;
        this.resp = resp;
    }

    public void Request(T req)
    {
        this.req = req;
    }

    public void Response(U resp)
    {
        this.resp = resp;
    }
}
