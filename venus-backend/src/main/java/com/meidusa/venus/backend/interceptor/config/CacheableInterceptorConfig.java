package com.meidusa.venus.backend.interceptor.config;

public class CacheableInterceptorConfig extends InterceptorConfig {
    private String key;
    private CacheOperation operation;
    private int expired = 60 * 60 * 24 * 7;

    public CacheableInterceptorConfig() {
        super();
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public int getExpired() {
        return expired;
    }

    public void setExpired(int expired) {
        this.expired = expired;
    }

    public CacheOperation getOperation() {
        return operation;
    }

    public void setOperation(CacheOperation operation) {
        this.operation = operation;
    }

    @Override
    public String toString() {
        return "CacheableInterceptorConfig [key=" + key + ", operation=" + operation + "]";
    }

}
