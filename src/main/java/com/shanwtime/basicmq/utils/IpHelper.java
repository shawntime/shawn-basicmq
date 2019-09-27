package com.shanwtime.basicmq.utils;

import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

/**
 * Created by Menong on 2015/9/10.
 * ip地址转换helper
 */
public final class IpHelper {

    private static final Pattern ipPattern = Pattern.compile("\\b((?!\\d\\d\\d)\\d+|1\\d\\d|2[0-4]\\d|25[0-5])\\.("
            + "(?!\\d\\d\\d)\\d+|1\\d\\d|2[0-4]\\d|25[0-5])\\.((?!\\d\\d\\d)\\d+|1\\d\\d|2[0-4]\\d|25[0-5])\\.("
            + "(?!\\d\\d\\d)\\d+|1\\d\\d|2[0-4]\\d|25[0-5])\\b");

    private IpHelper() {
        // Utility classes should always be final and have a private constructor
    }

    public static boolean verifyIp(String ip) {
        if (StringUtils.isBlank(ip)) {
            return false;
        }

        return ipPattern.matcher(ip).matches();
    }

    public static long ipToLong(String ip) {
        long[] array = new long[4];
        int position1 = ip.indexOf(".");
        int position2 = ip.indexOf(".", position1 + 1);
        int position3 = ip.indexOf(".", position2 + 1);
        array[0] = Long.parseLong(ip.substring(0, position1));
        array[1] = Long.parseLong(ip.substring(position1 + 1, position2));
        array[2] = Long.parseLong(ip.substring(position2 + 1, position3));
        array[3] = Long.parseLong(ip.substring(position3 + 1));
        return (array[0] << 24) + (array[1] << 16) + (array[2] << 8) + array[3];
    }

    public static String ipToString(long ip) {
        StringBuffer sb = new StringBuffer("");
        sb.append(String.valueOf((ip >>> 24)));
        sb.append(".");
        sb.append(String.valueOf((ip & 0x00FFFFFF) >>> 16));
        sb.append(".");
        sb.append(String.valueOf((ip & 0x0000FFFF) >>> 8));
        sb.append(".");
        sb.append(String.valueOf((ip & 0x000000FF)));
        return sb.toString();
    }

}
