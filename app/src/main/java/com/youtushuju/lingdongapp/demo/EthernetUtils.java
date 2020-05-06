package com.youtushuju.lingdongapp.demo;

import android.text.TextUtils;
import android.util.Log;

import java.util.regex.Pattern;

public class EthernetUtils {

    public boolean checkIPValue(String ipAddr, String gateway, String netMask, String dns1, String dns2) {
        boolean enable = false;
        Pattern pattern = Pattern.compile("(^((\\d|[01]?\\d\\d|2[0-4]\\d|25[0-5])\\.){3}(\\d|[01]?\\d\\d|2[0-4]\\d|25[0-5])$)|^(\\d|[1-2]\\d|3[0-2])$"); /*check subnet mask*/
        if (isValidIpAddress(ipAddr) && isValidIpAddress(gateway)
                && isValidIpAddress(dns1) && (pattern.matcher(netMask).matches())) {
            if (TextUtils.isEmpty(dns2)) { // 为空可以不考虑
                enable = true;
            } else {
                if (isValidIpAddress(dns2)) {
                    enable = true;
                } else {
                    enable = false;
                }
            }
        } else {
            enable = false;
        }
        return enable;
    }


    private boolean isValidIpAddress(String value) {
        int start = 0;
        int end = value.indexOf('.');
        int numBlocks = 0;

        while (start < value.length()) {

            if (-1 == end) {
                end = value.length();
            }

            try {
                int block = Integer.parseInt(value.substring(start, end));
                if ((block > 255) || (block < 0)) {
                    Log.w("EthernetIP",
                            "isValidIpAddress() : invalid 'block', block = "
                                    + block);
                    return false;
                }
            } catch (NumberFormatException e) {
                Log.w("EthernetIP", "isValidIpAddress() : e = " + e);
                return false;
            }

            numBlocks++;

            start = end + 1;
            end = value.indexOf('.', start);
        }
        return numBlocks == 4;
    }

}
