package com.meidusa.venus.backend.interceptor;

import com.meidusa.venus.backend.interceptor.config.InterceptorConfig;

public interface Configurable {

    public void processConfig(Class<?> clazz, String ep, InterceptorConfig config);
}
