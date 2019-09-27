package com.shanwtime.basicmq.redis;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import com.shanwtime.basicmq.utils.BooleanExtensions;
import com.shanwtime.basicmq.utils.CollectionExtensions;
import com.shanwtime.basicmq.utils.JsonHelper;
import com.shanwtime.basicmq.utils.MapExtensions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.shanwtime.basicmq.utils.RecommendFunction;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.JedisPubSub;
import redis.clients.jedis.Pipeline;

/**
 * Created by Menong on 2015/9/17.
 * Redis client
 */
public class RedisClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(RedisClient.class);

    private final JedisPool pool;

    private final int expired;

    private final boolean isClosedCache;

    public RedisClient(JedisPoolConfig config, String ip, Integer port, int timeOut, int expired) {
        this.pool = new JedisPool(config, ip, port, timeOut);
        this.expired = expired;
        this.isClosedCache = false;
    }

    public RedisClient(JedisPoolConfig config, String ip, Integer port,
                       int timeOut, int expired, Boolean isClosedCache) {
        this.pool = new JedisPool(config, ip, port, timeOut);
        this.expired = expired;
        this.isClosedCache = BooleanExtensions.getBoolean(isClosedCache, false);
    }

    private static void returnResource(Jedis jedis) {
        try {
            if (jedis != null) {
                jedis.close();
            }
        } catch (Exception e) {
            LOGGER.error("Jedis close error." + e.getMessage(), e);
        }
    }

    public void subscribe(JedisPubSub jedisPubSub, String channels) {
        Jedis jedis = null;
        try {
            jedis = pool.getResource();
            jedis.subscribe(jedisPubSub, channels);
        } catch (Exception ex) {
            LOGGER.error(ex.getMessage(), ex);
        } finally {
            returnResource(jedis);
        }
    }

    public long ttl(String key) {
        Jedis jedis = null;
        try {
            jedis = pool.getResource();
            return jedis.ttl(key);
        } catch (Exception ex) {
            LOGGER.error(ex.getMessage(), ex);
        } finally {
            returnResource(jedis);
        }
        return 0L;
    }

    public String getCustomKey(Object id, Class<?> clazz) {
        return clazz.getName() + "#" + id;
    }

    public String getCustomKey(Object id, Class<?> clazz, String keyPrefix) {
        return keyPrefix + "." + clazz.getName() + "#" + id;
    }

    public <K> String getCustomKey(K id, Class<?> clazz, Function<K, String> getKey) {
        return getCustomKey(getKey.apply(id), clazz);
    }

    public <K> String getCustomKey(K id, Class<?> clazz, Function<K, String> getKey, String keyPrefix) {
        return getCustomKey(getKey.apply(id), clazz, keyPrefix);
    }

    public String getListCustomKey(Object id, Class<?> clazz) {
        return "list#" + clazz.getName() + "#" + id;
    }

    public String getListCustomKey(Class<?> clazz) {
        return "list#" + clazz.getName() + ".ALL";
    }

    public <K> String[] getCustomKeys(List<K> ids, Class<?> clazz) {
        return getCustomKeys(ids, clazz, K::toString);
    }

    public <K> String[] getCustomKeys(List<K> ids, Class<?> clazz, Function<K, String> getKey) {
        String[] keys = new String[ids.size()];
        for (int i = 0; i < ids.size(); i++) {
            keys[i] = getCustomKey(getKey.apply(ids.get(i)), clazz);
        }
        return keys;
    }

    public <K> String[] getListCustomKeys(List<K> ids, Class<?> clazz, Function<K, String> getKey) {
        String[] keys = new String[ids.size()];
        for (int i = 0; i < ids.size(); i++) {
            keys[i] = getListCustomKey(getKey.apply(ids.get(i)), clazz);
        }
        return keys;
    }

    public <K, V> String set(K id, V object, Function<K, String> getKey) {
        return set(getKey.apply(id), object, expired);
    }

    public <K, V> String set(K id, V object, Function<K, String> getKey, int expired) {
        return set(getKey.apply(id), object, expired);
    }

    public String set(final String key, Object object) {
        return set(key, object, expired);
    }

    public String set(final String key, Object object, int seconds) {
        Jedis jedis = null;
        try {
            jedis = pool.getResource();
            String value = JsonHelper.serialize(object);
            return jedis.setex(key, seconds, value);
        } catch (Exception ex) {
            LOGGER.error(ex.getMessage(), ex);
            return null;
        } finally {
            returnResource(jedis);
        }
    }

    public String setNotExpire(final String key, Object object) {
        Jedis jedis = null;
        try {
            jedis = pool.getResource();
            String value = JsonHelper.serialize(object);
            return jedis.set(key, value);
        } catch (Exception ex) {
            LOGGER.error(ex.getMessage(), ex);
            return null;
        } finally {
            returnResource(jedis);
        }
    }

    public long setnx(final String key, Object object) {
        Jedis jedis = null;
        try {
            jedis = pool.getResource();
            String value = JsonHelper.serialize(object);
            return jedis.setnx(key, value);
        } catch (Exception ex) {
            LOGGER.error(ex.getMessage(), ex);
            return 0;
        } finally {
            returnResource(jedis);
        }
    }

    public long setnx(final String key, Object object, int expired) {
        Jedis jedis = null;
        try {
            jedis = pool.getResource();
            String value = JsonHelper.serialize(object);
            Long index = jedis.setnx(key, value);
            Long ttl = jedis.ttl(key);
            if (ttl != null && ttl == -1) {
                jedis.expire(key, expired);
            }
            return index;
        } catch (Exception ex) {
            LOGGER.error(ex.getMessage(), ex);
            return 0;
        } finally {
            returnResource(jedis);
        }
    }

    public String setnx(final String key, final String value, final long expired) {
        Jedis jedis = null;
        try {
            jedis = pool.getResource();
            return jedis.set(key, value, "nx", "ex", expired);
        } catch (Exception ex) {
            LOGGER.error(ex.getMessage(), ex);
            return null;
        } finally {
            returnResource(jedis);
        }
    }

    public <K, V> void mset(Map<K, V> map, Class<V> clazz, int seconds) {
        Jedis jedis = null;
        try {
            jedis = pool.getResource();
            Pipeline pipeline = jedis.pipelined();
            for (Map.Entry<K, V> entry : map.entrySet()) {
                String key = getCustomKey(entry.getKey(), clazz);
                pipeline.set(key, JsonHelper.serialize(entry.getValue()));
                pipeline.expire(key, seconds);
            }
            pipeline.syncAndReturnAll();
        } catch (Exception ex) {
            LOGGER.error(ex.getMessage(), ex);
        } finally {
            returnResource(jedis);
        }
    }

    public <K, V> void msetList(Map<K, List<V>> map, Class<V> clazz, int seconds) {
        Jedis jedis = null;
        try {
            jedis = pool.getResource();
            Pipeline pipeline = jedis.pipelined();
            for (Map.Entry<K, List<V>> entry : map.entrySet()) {
                String key = getListCustomKey(entry.getKey(), clazz);
                pipeline.set(key, JsonHelper.serialize(entry.getValue()));
                pipeline.expire(key, seconds);
            }
            pipeline.syncAndReturnAll();
        } catch (Exception ex) {
            LOGGER.error(ex.getMessage(), ex);
        } finally {
            returnResource(jedis);
        }
    }

    public <K, V> void hmset(Map<K, Map<String, V>> map, Class<V> clazz, int seconds) {
        if (MapExtensions.isEmpty(map)) {
            return;
        }

        Jedis jedis = null;
        try {
            jedis = pool.getResource();
            Pipeline pipeline = jedis.pipelined();
            for (Map.Entry<K, Map<String, V>> entry : map.entrySet()) {
                String key = getCustomKey(entry.getKey(), clazz);
                Map<String, String> value = toStringMap(entry.getValue());
                if (MapExtensions.isEmpty(value)) {
                    continue;
                }

                pipeline.hmset(key, value);
                pipeline.expire(key, seconds);
            }
            pipeline.syncAndReturnAll();
        } catch (Exception ex) {
            LOGGER.error(ex.getMessage(), ex);
        } finally {
            returnResource(jedis);
        }
    }

    public void hmset(String key, int expired, Map<String, String> memberMap) {
        Jedis jedis = null;
        try {
            jedis = pool.getResource();
            jedis.hmset(key, memberMap);
            if (expired > 0) {
                jedis.expire(key, expired);
            }
        } catch (Exception ex) {
            LOGGER.error(ex.getMessage(), ex);
        } finally {
            returnResource(jedis);
        }
    }

    private Map<String, String> toStringMap(Map<String, ?> map) {
        if (MapExtensions.isEmpty(map)) {
            return null;
        }

        Map<String, String> pairs = new LinkedHashMap<>();
        for (Map.Entry<String, ?> entry : map.entrySet()) {
            pairs.put(entry.getKey(), JsonHelper.serialize(entry.getValue()));
        }
        return pairs;
    }

    public Long incr(final String key) {
        Jedis jedis = null;
        try {
            jedis = pool.getResource();
            return jedis.incr(key);
        } catch (Exception ex) {
            LOGGER.error(ex.getMessage(), ex);
            return 0L;
        } finally {
            returnResource(jedis);
        }
    }

    public Long increment(final String key, Long value) {
        Jedis jedis = null;
        try {
            jedis = pool.getResource();
            return jedis.incrBy(key, value);
        } catch (Exception ex) {
            LOGGER.error(ex.getMessage(), ex);
            return 0L;
        } finally {
            returnResource(jedis);
        }
    }

    public Long increment(final String key, Long value, int seconds) {
        Jedis jedis = null;
        try {
            jedis = pool.getResource();
            Long incr = jedis.incrBy(key, value);
            Long ttl = jedis.ttl(key);
            if (ttl != null && ttl == -1) {
                jedis.expire(key, seconds);
            }
            return incr;
        } catch (Exception ex) {
            LOGGER.error(ex.getMessage(), ex);
            return 0L;
        } finally {
            returnResource(jedis);
        }
    }

    public Double increment(final String key, Double value) {
        Jedis jedis = null;
        try {
            jedis = pool.getResource();
            return jedis.incrByFloat(key, value);
        } catch (Exception ex) {
            LOGGER.error(ex.getMessage(), ex);
            return 0D;
        } finally {
            returnResource(jedis);
        }
    }

    public Long decrement(final String key, Long value) {
        Jedis jedis = null;
        try {
            jedis = pool.getResource();
            return jedis.decrBy(key, value);
        } catch (Exception ex) {
            LOGGER.error(ex.getMessage(), ex);
            return 0L;
        } finally {
            returnResource(jedis);
        }
    }

    public boolean isExist(final String key) {
        Jedis jedis = null;
        try {
            jedis = pool.getResource();
            return jedis.exists(key);
        } catch (Exception ex) {
            LOGGER.error(ex.getMessage(), ex);
            return false;
        } finally {
            returnResource(jedis);
        }
    }

    public void hset(final String key, String field, String value, int seconds) {
        Jedis jedis = null;
        try {
            jedis = pool.getResource();
            jedis.hset(key, field, value);
            Long ttl = jedis.ttl(key);
            if (ttl != null && ttl == -1) {
                jedis.expire(key, seconds);
            }
        } catch (Exception ex) {
            LOGGER.error(ex.getMessage(), ex);
        } finally {
            returnResource(jedis);
        }
    }

    public String hget(final String key, String field) {
        Jedis jedis = null;
        try {
            jedis = pool.getResource();
            return jedis.hget(key, field);
        } catch (Exception ex) {
            LOGGER.error(ex.getMessage(), ex);
            return null;
        } finally {
            returnResource(jedis);
        }
    }

    public void hdel(final String key, String field) {
        try (Jedis jedis = pool.getResource()) {
            jedis.hdel(key, field);
        } catch (Exception ex) {
            LOGGER.error(ex.getMessage(), ex);
        }
    }

    public Map<String, String> hgetAll(final String key) {
        try (Jedis jedis = pool.getResource()) {
            return jedis.hgetAll(key);
        } catch (Exception ex) {
            LOGGER.error(ex.getMessage(), ex);
        }
        return null;
    }

    public void del(final String key) {
        Jedis jedis = null;
        try {
            jedis = pool.getResource();
            jedis.del(key);
        } catch (Exception ex) {
            LOGGER.error(ex.getMessage(), ex);
        } finally {
            returnResource(jedis);
        }
    }

    public String getSet(final String key, final String value) {
        Jedis jedis = null;
        try {
            jedis = pool.getResource();
            return jedis.getSet(key, value);
        } catch (Exception ex) {
            LOGGER.error(ex.getMessage(), ex);
            return null;
        } finally {
            returnResource(jedis);
        }
    }

    public <V> List<V> getList(Class<V> clazz, Supplier<List<V>> reload) {
        Supplier<String> getKey = () -> getListCustomKey(clazz);
        String value = get(getKey.get());
        if (StringUtils.isNotBlank(value)) {
            return JsonHelper.deSerializeList(value, clazz);
        }

        if (reload == null) {
            return null;
        }

        return reload.get();
    }

    public <K, V> List<V> getList(K key, Class<V> clazz, Function<K, List<V>> reload) {

        Function<K, String> getKey = id -> getListCustomKey(id, clazz);
        String value = get(getKey.apply(key));
        if (StringUtils.isNotBlank(value)) {
            return JsonHelper.deSerializeList(value, clazz);
        }

        if (reload == null) {
            return null;
        }

        return reload.apply(key);
    }

    public <K, V, T, E> List<V> getList(K key, Class<V> clazz, T arg1, E arg2,
                                        RecommendFunction<T, E, List<V>> reload) {

        Function<K, String> getKey = id -> getListCustomKey(id, clazz);
        String value = get(getKey.apply(key));
        if (StringUtils.isNotBlank(value)) {
            return JsonHelper.deSerializeList(value, clazz);
        }
        if (reload == null) {
            return null;
        }
        return reload.apply(arg1, arg2);
    }

    public String get(final String key, final boolean isClosedCache) {
        if (isClosedCache) {
            return null;
        }
        Jedis jedis = null;
        try {
            jedis = pool.getResource();
            return jedis.get(key);
        } catch (Exception ex) {
            LOGGER.error(ex.getMessage(), ex);
            return null;
        } finally {
            returnResource(jedis);
        }
    }

    public String get(final String key) {
        return get(key, isClosedCache);
    }

    public String get(final String key, String defaultValue, boolean isClosedCache) {
        if (isClosedCache) {
            return null;
        }
        Jedis jedis = null;
        try {
            jedis = pool.getResource();
            String value = jedis.get(key);
            return StringUtils.defaultIfEmpty(value, defaultValue);
        } catch (Exception ex) {
            LOGGER.error(ex.getMessage(), ex);
            return null;
        } finally {
            returnResource(jedis);
        }
    }

    public String get(final String key, String defaultValue) {
        return get(key, defaultValue, isClosedCache);
    }

    public <K, V> V get(K key, Class<V> clazz, Function<K, V> reload) {
        return get(key, clazz, id -> getCustomKey(id, clazz), reload);
    }

    public <K, V> V get(K key, Class<V> clazz, Function<K, V> reload, boolean isCacheNull) {
        return get(key, clazz, id -> getCustomKey(id, clazz), reload, isCacheNull);
    }

    public <K, V> V get(K key, Class<V> clazz, Function<K, V> reload, String keyPrefix) {
        return get(key, clazz, id -> getCustomKey(id, clazz, keyPrefix), reload);
    }

    public <K, V> V get(K key, Class<V> clazz, Function<K, V> reload, int expired) {
        return get(key, clazz, id -> getCustomKey(id, clazz), reload, expired);
    }

    public <K, V> V get(K key, Class<V> clazz, Function<K, V> reload, int expired,  boolean isCacheNull) {
        return get(key, clazz, id -> getCustomKey(id, clazz), reload, expired, isCacheNull);
    }

    public <T> T get(Supplier<String> getKey, Class<T> clazz, Supplier<T> reload) {
        String value = get(getKey.get());
        if (StringUtils.isNotBlank(value)) {
            return JsonHelper.deSerialize(value, clazz);
        }

        if (reload == null) {
            return null;
        }

        T object = reload.get();
        if (object == null) {
            return null;
        }

        set(getKey.get(), object);
        return object;
    }

    public <K, V> V get(K key, Class<V> clazz, Function<K, String> getKey, Function<K, V> reload, boolean isCacheNull) {
        String value = get(getKey.apply(key));
        if (StringUtils.isNotBlank(value)) {
            return JsonHelper.deSerialize(value, clazz);
        }

        if (reload == null) {
            return null;
        }

        V object = reload.apply(key);
        if (object == null && !isCacheNull) {
            return null;
        }

        set(key, object, getKey);
        return object;
    }

    public <K, V> V get(K key, Class<V> clazz, Function<K, String> getKey, Function<K, V> reload) {
        return get(key, clazz, getKey, reload, false);
    }

    public <K, V> V get(K key, Class<V> clazz, Function<K, String> getKey, Function<K, V> reload, int expired, boolean isCacheNull) {
        String value = get(getKey.apply(key));
        if (StringUtils.isNotBlank(value)) {
            try {
                return JsonHelper.deSerialize(value, clazz);
            } catch (Exception e) {
                LOGGER.error(String.format("redis json序列化失败：[value:%s]", value), e);
            }
        }

        if (reload == null) {
            return null;
        }

        V object = reload.apply(key);
        if (object == null) {
            if (isCacheNull) {
                set(key, null, getKey, 5 * 60);
            }
            return null;
        }
        set(key, object, getKey, expired);
        return object;
    }

    public <K, V> V get(K key, Class<V> clazz, Function<K, String> getKey, Function<K, V> reload, int expired) {
        return get(key, clazz, getKey, reload, expired, false);
    }

    public List<String> mget(String... keys) {
        if (keys == null || keys.length <= 0) {
            return new ArrayList<>();
        }
        if (isClosedCache) {
            List<String> values = new ArrayList<>(keys.length);
            for (int i = 0; i < keys.length; ++i) {
                values.add(null);
            }
            return values;
        }
        Jedis jedis = null;
        try {
            jedis = pool.getResource();
            return jedis.mget(keys);
        } catch (Exception ex) {
            LOGGER.error(ex.getMessage(), ex);
            List<String> values = new ArrayList<>(keys.length);
            for (int i = 0; i < keys.length; ++i) {
                values.add(null);
            }
            return values;
        } finally {
            returnResource(jedis);
        }
    }

    public <V> Map<Integer, V> mget(List<Integer> keys, Class<V> clazz, Function<List<Integer>,
            Map<Integer, V>> reload) {
        return mget(keys, clazz, Object::toString, reload);
    }

    public <K, V> Map<K, V> mget(List<K> keys, Class<V> clazz, Function<K, String> getKey, Function<List<K>,
            Map<K, V>> reload) {
        if (CollectionExtensions.isEmpty(keys)) {
            return null;
        }

        Map<K, V> map = new LinkedHashMap<>(keys.size());
        List<K> notExists = new ArrayList<>(keys.size());
        List<String> values = mget(getCustomKeys(keys, clazz, getKey));
        for (int i = 0; i < keys.size(); i++) {
            String json = values.get(i);
            V value = null;
            if (StringUtils.isBlank(json)) {
                notExists.add(keys.get(i));
            } else {
                value = JsonHelper.deSerialize(json, clazz);
            }
            map.put(keys.get(i), value);
        }

        if (CollectionUtils.isEmpty(notExists)) {
            return map;
        }

        Map<K, V> db = reload.apply(notExists);
        if (MapExtensions.isNotEmpty(db)) {
            db.forEach(map::put);
        }

        return map;
    }

    public <K, V> Map<K, Map<String, V>> hgetAll(List<K> keys,
                                                 Class<V> clazz,
                                                 Function<List<K>, Map<K, Map<String, V>>> reload) {
        return hgetAll(keys, clazz, Object::toString, reload);
    }

    public <K, V> Map<K, Map<String, V>> hgetAll(List<K> keys,
                                                 Class<V> clazz,
                                                 Function<K, String> getKey,
                                                 Function<List<K>, Map<K, Map<String, V>>> reload) {
        if (CollectionExtensions.isEmpty(keys)) {
            return null;
        }

        Map<K, Map<String, V>> map = new LinkedHashMap<>(keys.size());
        List<K> notExists = new ArrayList<>(keys.size());
        if (isClosedCache) {
            notExists = keys;
        } else {
            List<Object> pipeReturn = new ArrayList<>();
            Jedis jedis = null;
            long beginMills = System.currentTimeMillis();
            try {
                jedis = pool.getResource();
                Pipeline pipeline = jedis.pipelined();
                keys.forEach(key -> pipeline.hgetAll(getCustomKey(key, clazz, getKey)));
                pipeReturn = pipeline.syncAndReturnAll();
            } catch (Exception e) {
                long endMills = System.currentTimeMillis();
                String errorLog = "pipeline syncAndReturnAll error. keys: " + keys.size() + ", usedMills: " + (endMills - beginMills) + "," + e.getMessage();
                LOGGER.error(errorLog);
            } finally {
                returnResource(jedis);
            }

            for (int i = 0; i < keys.size(); i++) {
                Object obj = pipeReturn.get(i);
                if (!(obj instanceof Map)) {
                    notExists.add(keys.get(i));
                    continue;
                }

                Map<String, String> hash = (Map<String, String>) obj;
                if (MapExtensions.isEmpty(hash)) {
                    notExists.add(keys.get(i));
                    continue;
                }

                Map<String, V> value = new LinkedHashMap<>();
                map.put(keys.get(i), value);
                hash.forEach(
                        (key, val) -> value.put(key, JsonHelper.deSerialize(val, clazz))
                );
            }

            if (CollectionUtils.isEmpty(notExists)) {
                return map;
            }
        }

        Map<K, Map<String, V>> db = reload.apply(notExists);
        if (MapExtensions.isNotEmpty(db)) {
            db.forEach(map::put);
        }

        return map;
    }


    public <T> int getCount(T key, RecommendFunction<T, String, Integer> reload) {
        StackTraceElement[] temp = Thread.currentThread().getStackTrace();
        String prefix = null;
        if (temp != null && temp.length > 2) {
            StackTraceElement stackTraceElement = temp[2];
            prefix = stackTraceElement.getClassName() + "." + stackTraceElement.getMethodName();
            String redisKey = prefix + "#" + key;
            String value = get(redisKey);
            if (StringUtils.isNotBlank(value)) {
                return Integer.parseInt(value);
            }
        }
        if (reload == null) {
            return 0;
        }
        return reload.apply(key, prefix);
    }

    public <K, V> Map<K, V> getMultiAllMap(List<K> keys, Class<V> clazz, Function<List<K>, Map<K, V>> reload,
                                           int expired) {
        Map<K, V> returnMap = Maps.newHashMap();
        if (CollectionUtils.isEmpty(keys)) {
            return returnMap;
        }
        StackTraceElement[] temp = Thread.currentThread().getStackTrace();
        if (temp == null || temp.length <= 2) {
            return null;
        }
        StackTraceElement stackTraceElement = temp[2];
        String redisKey = stackTraceElement.getClassName() + "." + stackTraceElement.getMethodName();
        Jedis jedis = null;
        try {
            jedis = pool.getResource();
            List<K> noCacheKeyList = Lists.newArrayList();
            if (!isClosedCache) {
                List<String> keyList = keys.stream().map(key -> String.valueOf(key)).collect(Collectors.toList());
                List<String> valueList = jedis.hmget(redisKey, keyList.toArray(new String[keyList.size()]));
                fillCache(keys, clazz, returnMap, noCacheKeyList, valueList);
                if (CollectionUtils.isEmpty(noCacheKeyList)) {
                    return returnMap;
                }
            } else {
                noCacheKeyList = keys;
            }
            return getKvMap(keys, reload, expired, returnMap, redisKey, jedis, noCacheKeyList);
        } catch (Exception ex) {
            LOGGER.error(ex.getMessage(), ex);
            return null;
        } finally {
            returnResource(jedis);
        }
    }

    private <K, V> void fillCache(List<K> keys, Class<V> clazz, Map<K, V> returnMap, List<K> noCacheKeyList,
                                  List<String> valueList) {
        if (valueList != null) {
            for (int i = 0; i < keys.size(); ++i) {
                String value = valueList.get(i);
                if (value != null) {
                    returnMap.put(keys.get(i), JsonHelper.deSerialize(value, clazz));
                } else {
                    noCacheKeyList.add(keys.get(i));
                }
            }
        }
    }

    private <K, V> Map<K, V> getKvMap(List<K> keys, Function<List<K>, Map<K, V>> reload, int expired, Map<K, V>
            returnMap, String redisKey, Jedis jedis, List<K> noCacheKeyList) {
        if (reload == null) {
            return returnMap;
        }
        Map<K, V> dataMap = reload.apply(noCacheKeyList);
        if (dataMap == null) {
            return returnMap;
        }
        Map<String, String> resultMap = Maps.newHashMap();
        dataMap.forEach((k, v) -> {
            resultMap.put(String.valueOf(k), JsonHelper.serialize(v));
        });
        Pipeline pipeline = jedis.pipelined();
        pipeline.hmset(redisKey, resultMap);
        pipeline.expire(redisKey, expired);
        pipeline.syncAndReturnAll();
        keys.forEach(key -> {
            returnMap.put(key, dataMap.get(key));
        });
        return returnMap;
    }

    public <K, V> V getAllMap(K key, Class<V> clazz, Function<List<K>, Map<K, V>> reload, int expired) {
        Map<K, V> multiAllMap = getMultiAllMap(Lists.newArrayList(key), clazz, reload, expired);
        if (multiAllMap != null) {
            return multiAllMap.get(key);
        }
        return null;
    }

    public Long zadd(final String key, final Map<String, Double> dataMap, int expired) {
        Jedis jedis = null;
        try {
            jedis = pool.getResource();
            Long value = jedis.zadd(key, dataMap);
            if (expired > 0) {
                jedis.expire(key, expired);
            }
            return value;
        } catch (Exception ex) {
            LOGGER.error(ex.getMessage(), ex);
            return 0L;
        } finally {
            returnResource(jedis);
        }
    }

    public Long zadd(final String key, final Map<String, Double> dataMap) {
        Jedis jedis = null;
        try {
            jedis = pool.getResource();
            Long value = jedis.zadd(key, dataMap);
            jedis.expire(key, expired);
            return value;
        } catch (Exception ex) {
            LOGGER.error(ex.getMessage(), ex);
            return 0L;
        } finally {
            returnResource(jedis);
        }
    }

    public Long zadd(final String key, final String member, final double score, final int exipred) {
        Jedis jedis = null;
        try {
            jedis = pool.getResource();
            Long value = jedis.zadd(key, score, member);
            if (exipred > 0) {
                jedis.expire(key, expired);
            }
            return value;
        } catch (Exception ex) {
            LOGGER.error(ex.getMessage(), ex);
            return 0L;
        } finally {
            returnResource(jedis);
        }
    }

    public Double zincrby(final String redisKey, final String itemKey, final double itemValue) {
        Jedis jedis = null;
        try {
            jedis = pool.getResource();
            Double value = jedis.zincrby(redisKey, itemValue, itemKey);
            return value;
        } catch (Exception ex) {
            LOGGER.error(ex.getMessage(), ex);
            return 0D;
        } finally {
            returnResource(jedis);
        }
    }

    public Long zrank(final String key, final String member) {
        Jedis jedis = null;
        try {
            jedis = pool.getResource();
            Long index = jedis.zrank(key, member);
            if (index == null) {
                return -1L;
            }
            return index;
        } catch (Exception ex) {
            LOGGER.error(ex.getMessage(), ex);
            return 0L;
        } finally {
            returnResource(jedis);
        }
    }

    public long zrevrank(final String key, final String member) {
        Jedis jedis = null;
        try {
            jedis = pool.getResource();
            Long index = jedis.zrevrank(key, member);
            if (index == null) {
                return -1L;
            }
            return index;
        } catch (Exception ex) {
            LOGGER.error(ex.getMessage(), ex);
            return -1L;
        } finally {
            returnResource(jedis);
        }
    }

    public Long zcard(final String key) {
        Jedis jedis = null;
        try {
            jedis = pool.getResource();
            Long index = jedis.zcard(key);
            if (index == null) {
                return 0L;
            }
            return index;
        } catch (Exception ex) {
            LOGGER.error(ex.getMessage(), ex);
            return 0L;
        } finally {
            returnResource(jedis);
        }
    }

    public Set<String> zrange(final String key, long startIndex, long endIndex) {
        Jedis jedis = null;
        try {
            jedis = pool.getResource();
            return jedis.zrange(key, startIndex, endIndex);
        } catch (Exception ex) {
            LOGGER.error(ex.getMessage(), ex);
            return null;
        } finally {
            returnResource(jedis);
        }
    }

    public Object lua(final String script, List<String> keys, List<String> args) {
        Jedis jedis = null;
        try {
            jedis = pool.getResource();
            return jedis.eval(script, keys, args);
        } catch (Exception ex) {
            LOGGER.error(ex.getMessage(), ex);
            return 0;
        } finally {
            returnResource(jedis);
        }
    }

    public List<String> hmget(String key, String... members) {
        Jedis jedis = null;
        try {
            jedis = pool.getResource();
            return jedis.hmget(key, members);
        } catch (Exception ex) {
            LOGGER.error(ex.getMessage(), ex);
            return new ArrayList<>();
        } finally {
            returnResource(jedis);
        }
    }

    public Long hincrBy(final String key, final long value, final String member, final int expired) {
        Jedis jedis = null;
        try {
            jedis = pool.getResource();
            Long newValue = jedis.hincrBy(key, member, value);
            if (expired > 0) {
                jedis.expire(key, expired);
            }
            return newValue;
        } catch (Exception ex) {
            LOGGER.error(ex.getMessage(), ex);
            return 0L;
        } finally {
            returnResource(jedis);
        }
    }

    public boolean hexists(final String key, final String member) {
        Jedis jedis = null;
        try {
            jedis = pool.getResource();
            Boolean hexists = jedis.hexists(key, member);
            if (hexists == null) {
                return false;
            }
            return hexists;
        } catch (Exception ex) {
            LOGGER.error(ex.getMessage(), ex);
            return false;
        } finally {
            returnResource(jedis);
        }
    }

    public Boolean sismember(final String key, final String member) {
        Jedis jedis = null;
        try {
            jedis = pool.getResource();
            return jedis.sismember(key, member);
        } catch (Exception ex) {
            LOGGER.error(ex.getMessage(), ex);
            return false;
        } finally {
            returnResource(jedis);
        }
    }

    public Set<String> smembers(final String key) {
        Jedis jedis = null;
        try {
            jedis = pool.getResource();
            return jedis.smembers(key);
        } catch (Exception ex) {
            LOGGER.error(ex.getMessage(), ex);
            return null;
        } finally {
            returnResource(jedis);
        }
    }

    public Long sadd(final String key, int expired, String... member) {
        Jedis jedis = null;
        try {
            jedis = pool.getResource();
            Long sadd = jedis.sadd(key, member);
            if (sadd != null && sadd > 0 && expired > 0) {
                jedis.expire(key, expired);
            }
            return sadd;
        } catch (Exception ex) {
            LOGGER.error(ex.getMessage(), ex);
            return 0L;
        } finally {
            returnResource(jedis);
        }
    }

    public Long listRightPush(final String key, int expired, String... member) {
        Jedis jedis = null;
        try {
            jedis = pool.getResource();
            Long rpush = jedis.rpush(key, member);
            if (rpush != null && rpush > 0 && expired > 0) {
                jedis.expire(key, expired);
            }
            return rpush;
        } catch (Exception ex) {
            LOGGER.error(ex.getMessage(), ex);
            return 0L;
        } finally {
            returnResource(jedis);
        }
    }

    public Long listLength(final String key) {
        Jedis jedis = null;
        try {
            jedis = pool.getResource();
            return jedis.llen(key);
        } catch (Exception ex) {
            LOGGER.error(ex.getMessage(), ex);
            return 0L;
        } finally {
            returnResource(jedis);
        }
    }

    public List<String> listRange(final String key, final int start, final int stop) {
        Jedis jedis = null;
        try {
            jedis = pool.getResource();
            return jedis.lrange(key, start, stop);
        } catch (Exception ex) {
            LOGGER.error(ex.getMessage(), ex);
            return null;
        } finally {
            returnResource(jedis);
        }
    }
}
