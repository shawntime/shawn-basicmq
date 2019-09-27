package com.shanwtime.basicmq.utils;

/**
 * Created by tlbgqq on 2015/12/14.
 */
public final class IntegerExtensions {
    private IntegerExtensions() {
        //private constructor
    }

    public static boolean isMoreThanZero(Integer integer) {
        return integer != null && integer.intValue() > 0 ? true : false;
    }

    public static int getIntValue(Integer integer, int defaultValue) {
        if (integer == null) {
            return defaultValue;
        } else {
            return integer.intValue();
        }
    }
}
