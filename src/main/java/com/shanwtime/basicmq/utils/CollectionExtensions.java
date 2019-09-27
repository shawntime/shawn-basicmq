package com.shanwtime.basicmq.utils;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Created by tlbgqq on 2017/11/17.
 */
public class CollectionExtensions {

    private CollectionExtensions() {
        // Utility classes should always be final and have a private constructor
    }

    public static boolean isEmpty(Collection collection) {
        return collection == null || collection.isEmpty();
    }

    public static boolean isNotEmpty(Collection collection) {
        return !isEmpty(collection);
    }

    public static <K, V> Map<K, V> toMap(Function<V, K> getKey, Collection<V> list) {
        if (CollectionExtensions.isEmpty(list)) {
            return null;
        }

        Map<K, V> map = new LinkedHashMap<>();
        list.forEach(e -> map.put(getKey.apply(e), e));
        return map;
    }

    public static <K, V> HashMap<K, V> toHashMap(Function<V, K> getKeyFromV, Collection<V> list) {
        if (getKeyFromV != null && CollectionExtensions.isNotEmpty(list)) {
            HashMap<K, V> hashMap = new HashMap<K, V>();
            list.forEach(t -> {
                Object key = getKeyFromV.apply(t);
                if (hashMap.containsKey(key)) {
                    throw new IllegalArgumentException("存在重复的key=" + String.valueOf(key));
                } else {
                    hashMap.put((K) key, t);
                }
            });
            return hashMap;
        } else {
            return null;
        }
    }

    public static <T> int getSizeWhenIsNullReturnZero(Collection collection) {
        int size = 0;
        if (isNotEmpty(collection)) {
            size = collection.size();
        }
        return size;
    }
}
