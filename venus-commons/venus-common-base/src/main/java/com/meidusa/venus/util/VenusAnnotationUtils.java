package com.meidusa.venus.util;

import java.lang.reflect.Method;

import com.meidusa.toolkit.common.util.StringUtil;
import com.meidusa.venus.annotations.Endpoint;
import com.meidusa.venus.annotations.Service;

public class VenusAnnotationUtils {
    public static String getApiname(Method method, Service service, Endpoint endpoint) {
        String serviceName = null;
        if (service == null || StringUtil.isEmpty(service.name())) {
            serviceName = method.getDeclaringClass().getCanonicalName();
        } else {
            serviceName = service.name();
        }

        String methodName = method.getName();
        if (endpoint == null || StringUtil.isEmpty(endpoint.name())) {
            methodName = method.getName();
        } else {
            methodName = endpoint.name();
        }

        return serviceName + "." + methodName;
    }
}
