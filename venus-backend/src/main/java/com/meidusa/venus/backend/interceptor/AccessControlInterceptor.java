package com.meidusa.venus.backend.interceptor;

import com.meidusa.venus.backend.EndpointInvocation;

/**
 * TODO
 * 
 * @author Struct
 * 
 */
public class AccessControlInterceptor extends AbstractInterceptor {

    @Override
    public Object intercept(EndpointInvocation invocation) {
        return invocation.invoke();
    }

}
