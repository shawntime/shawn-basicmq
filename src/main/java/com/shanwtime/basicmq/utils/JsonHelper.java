package com.shanwtime.basicmq.utils;

import java.lang.reflect.Type;
import java.util.List;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;

/**
 * Created by tlbgqq on 2017/11/17.
 */
public class JsonHelper {

    private static final String defaultDateFormat = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX";

    private JsonHelper() {
        // Utility classes should always be final and have a private constructor
    }

    public static <T> String serialize(T object) {
        JSON.DEFFAULT_DATE_FORMAT = defaultDateFormat;
        return serialize(object, SerializerFeature.WriteMapNullValue, SerializerFeature.WriteDateUseDateFormat);
    }

    public static <T> String serialize(T object, SerializerFeature... features) {
        return JSON.toJSONString(object, features);
    }

    public static <T> T deSerialize(String string, Class<T> clazz) {
        return JSON.parseObject(string, clazz);
    }

    public static <T> T deSerialize(String string, Type type) {
        return JSON.parseObject(string, type);
    }

    public static <T> List<T> deSerializeList(String string, Class<T> clazz) {
        return JSON.parseArray(string, clazz);
    }

}
