package com.youtushuju.lingdongapp.device;

import com.youtushuju.lingdongapp.common.Common;
import com.youtushuju.lingdongapp.common.Logf;

public final class DropModeReqStruct extends SerialReqStruct {
    public static final String CONST_DROP_MODE_FACE = "01";
    public static final String CONST_DROP_MODE_KEY = "02";
    public static final String CONST_DROP_MODE_CODE = "03";
    public String dropSetMode;

    public DropModeReqStruct()
    {
        super(SerialDataDef.ID_SERIAL_DATA_TYPE_DROP_MODE);
    }

    public DropModeReqStruct(String dropMode)
    {
        super(SerialDataDef.ID_SERIAL_DATA_TYPE_DROP_MODE);
        dropSetMode = dropMode;
    }

    @Override
    public boolean IsValid() {
        return super.IsValid() && !Common.StringIsEmpty(dropSetMode);
    }

    public static String FullDropMode()
    {
        StringBuffer sb = new StringBuffer();
        sb.append("[")
            .append(CONST_DROP_MODE_FACE).append(",")
            .append(CONST_DROP_MODE_KEY).append(",")
            .append(CONST_DROP_MODE_CODE)
            .append("]")
        ;
        return sb.toString();
    }

    public static String EmptyDropMode()
    {
        return "[]";
    }

    public static String MakeDropMode(String ...mode)
    {
        if(mode == null || mode.length == 0)
            return EmptyDropMode();
        StringBuffer sb = new StringBuffer();
        sb.append("[");
        for (int i = 0; i < mode.length; i++)
        {
            sb.append(mode[i]);
            if(i < mode.length - 1)
                sb.append(",");
        }
        sb.append("]");
        return sb.toString();
    }

    public static boolean DropModeIsValid(String rule)
    {
        return rule != null && rule.matches("^\\[(.*)\\]$");
    }

    public static boolean AllowDropMode(String target, String rule)
    {
        if(target == null || !DropModeIsValid(rule))
            return false;
        String rules = rule.substring(1, rule.length() - 1);
        String list[] = rules.split(",");
        if(list == null)
            return false;
        for (String r : list)
        {
            if(target.equals(r))
                return true;
        }
        return false;
    }

    public static String GetDropModeName(String res)
    {
        if(CONST_DROP_MODE_FACE.equals(res))
            return "扫脸开门";
        if(CONST_DROP_MODE_KEY.equals(res))
            return "按键开门";
        if(CONST_DROP_MODE_CODE.equals(res))
            return "扫码开门";
        return "";
    }
}
