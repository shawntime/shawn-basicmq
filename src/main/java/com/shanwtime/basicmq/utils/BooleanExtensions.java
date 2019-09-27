package com.shanwtime.basicmq.utils;

/**
 * Created by tlbgqq on 2017/11/17.
 */
public class BooleanExtensions {

    private BooleanExtensions() {
        // Utility classes should always be final and have a private constructor
    }

    public static boolean getBoolean(Boolean value, boolean defaultValue) {
        if (value == null) {
            return defaultValue;
        } else {
            return value;
        }
    }
}
