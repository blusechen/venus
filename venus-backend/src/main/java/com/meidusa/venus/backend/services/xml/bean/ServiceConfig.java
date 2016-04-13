package com.meidusa.venus.backend.services.xml.bean;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.meidusa.toolkit.common.util.StringUtil;
import com.meidusa.venus.util.ArrayRange;
import com.meidusa.venus.util.BetweenRange;
import com.meidusa.venus.util.Range;

public class ServiceConfig {
    private String name;

    private boolean active = true;

    private Class<?> type;

    private Object instance;

    private String version;

    private Map<String, EndpointConfig> endpointConfigMap = new HashMap<String, EndpointConfig>();

    private String interceptorStack;
    private Range versionRange;

    public String getVersion() {
        return version;
    }

    public Range getVersionRange() {
        if (versionRange != null) {
            return versionRange;
        }
        if (!StringUtil.isEmpty(version)) {
            version = version.trim();
            String[] tmps = StringUtils.split(version, "{}[], ");
            int[] rages = new int[tmps.length];
            for (int i = 0; i < tmps.length; i++) {
                rages[i] = Integer.valueOf(tmps[i]);
            }

            if (version.startsWith("[")) {
                versionRange = new BetweenRange(rages);
            } else {
                versionRange = new ArrayRange(rages);
            }
            return versionRange;
        } else {
            return null;
        }
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getInterceptorStack() {
        return interceptorStack;
    }

    public void setInterceptorStack(String interceptorStack) {
        this.interceptorStack = interceptorStack;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public Class<?> getType() {
        return type;
    }

    public void setType(Class<?> type) {
        this.type = type;
    }

    public Object getInstance() {
        return instance;
    }

    public void setInstance(Object instance) {
        this.instance = instance;
    }

    public EndpointConfig getEndpointConfig(String name) {
        return endpointConfigMap.get(name);
    }

    public void addEndpointConfig(EndpointConfig config) {
        endpointConfigMap.put(config.getName(), config);
    }
}
