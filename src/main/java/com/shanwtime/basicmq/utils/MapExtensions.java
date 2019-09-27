package com.shanwtime.basicmq.utils;

import java.util.Map;

/**
 * Created by tlbgqq on 2017/11/17.
 */
public class MapExtensions {

    private MapExtensions() {
        // Utility classes should always be final and have a private constructor
    }

    public static boolean isEmpty(Map map) {
        return map == null || map.isEmpty();
    }

    public static boolean isNotEmpty(Map map) {
        return !isEmpty(map);
    }
}
