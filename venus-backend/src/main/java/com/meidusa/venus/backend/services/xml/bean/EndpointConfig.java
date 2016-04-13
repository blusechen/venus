package com.meidusa.venus.backend.services.xml.bean;

import java.util.HashMap;
import java.util.Map;

import com.meidusa.venus.backend.interceptor.config.InterceptorConfig;

public class EndpointConfig {

    private String name;

    private int timeWait;

    private boolean active = true;

    private String interceptorStack;

    private PerformanceLogger performanceLogger;

    Map<String, InterceptorConfig> interceptorConfigs;

    public EndpointConfig() {
        this.interceptorConfigs = new HashMap<String, InterceptorConfig>();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getTimeWait() {
        return timeWait;
    }

    public void setTimeWait(int timeWait) {
        this.timeWait = timeWait;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getInterceptorStack() {
        return interceptorStack;
    }

    public void setInterceptorStack(String interceptorStack) {
        this.interceptorStack = interceptorStack;
    }

    public void addInterceptorConfig(InterceptorConfig config) {
        this.interceptorConfigs.put(config.getIntercepterRef(), config);
    }

    public Map<String, InterceptorConfig> getInterceptorConfigs() {
        return interceptorConfigs;
    }

    public void setInterceptorConfigs(Map<String, InterceptorConfig> interceptorConfigs) {
        this.interceptorConfigs = interceptorConfigs;
    }

    public PerformanceLogger getPerformanceLogger() {
        return performanceLogger;
    }

    public void setPerformanceLogger(PerformanceLogger performanceLogger) {
        this.performanceLogger = performanceLogger;
    }

}
