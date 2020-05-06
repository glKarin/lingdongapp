package com.youtushuju.lingdongapp.device;

import android.content.Context;
import android.os.Build;
import android.telephony.TelephonyManager;

import com.youtushuju.lingdongapp.common.Common;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public final class DeviceUtility {
    private static final String ID_TAG = "DeviceUtility";

    public static final List<String> GetDevList(String prefix) {
        List<String> ret = null;
        File dir = new File("/dev");
        if(!dir.canRead())
            return null;
        String files[] = dir.list();
        ret = new ArrayList<String>();
        boolean empty = Common.StringIsEmpty(prefix);
        for (String str : files)
        {
            if(empty || prefix.startsWith(prefix))
                ret.add("/dev/" + str);
        }
        return ret;
    }
}
