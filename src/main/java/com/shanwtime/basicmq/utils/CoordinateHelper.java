package com.shanwtime.basicmq.utils;

import java.math.BigDecimal;

import org.apache.commons.lang3.StringUtils;

/**
 * Created by zhouxiaoming on 2015/9/6.
 * 经纬度帮助类
 */
public final class CoordinateHelper {

    /**
     * 经纬度为空时的默认值
     */
    public static final BigDecimal NullCoordinate = new BigDecimal(9999);

    public static final BigDecimal MaxLongitude = new BigDecimal(180);

    public static final BigDecimal MinLongitude = new BigDecimal(-180);

    public static final BigDecimal MaxLatitude = new BigDecimal(90);

    public static final BigDecimal MinLatitude = new BigDecimal(-90);

    private CoordinateHelper() {
        // Utility classes should always be final and have a private constructor
    }

    /**
     * 获取经纬度
     */

    public static BigDecimal getCoordinate(String string) {
        BigDecimal value = NullCoordinate;
        if (!StringUtils.isBlank(string)) {
            value = new BigDecimal(string);
        }
        return value;
    }

    /**
     * 验证经纬度
     */
    public static boolean valid(BigDecimal lon, BigDecimal lat, boolean checkNull) {
        //不校验空值时，当lon,lat==9999返回真
        if (!checkNull && NullCoordinate.equals(lon) && NullCoordinate.equals(lat)) {
            return true;
        }
        return lon.compareTo(MinLongitude) == 1
                && lon.compareTo(MaxLongitude) == -1
                && lat.compareTo(MinLatitude) == 1
                && lat.compareTo(MaxLatitude) == -1;
    }

    public static boolean valid(BigDecimal lon, BigDecimal lat) {
        return valid(lon, lat, false);
    }
}
