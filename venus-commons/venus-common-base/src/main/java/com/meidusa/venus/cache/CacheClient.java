package com.meidusa.venus.cache;

public interface CacheClient {
    Object get(String key);

    boolean set(String key, Object value, int expired);

    boolean delete(String key);

}
