package com.meidusa.venus.cache;

import java.util.HashMap;
import java.util.Map;

public class SimpleCacheClient implements CacheClient {

    private Map<String, Object> cache = new HashMap<String, Object>();

    public SimpleCacheClient() {
        super();
    }

    public Object get(String key) {
        return cache.get(key);
    }

    public boolean set(String key, Object value, int expired) {
        synchronized (cache) {
            cache.put(key, value);
        }
        return true;
    }

    public boolean delete(String key) {
        synchronized (cache) {
            cache.remove(key);
        }
        return true;
    }

    public boolean exists(String key) {
        return cache.containsKey(key);
    }

}
