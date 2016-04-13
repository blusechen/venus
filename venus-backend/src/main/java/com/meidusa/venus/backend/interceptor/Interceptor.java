package com.meidusa.venus.backend.interceptor;

import com.meidusa.toolkit.common.bean.util.Initialisable;
import com.meidusa.toolkit.common.bean.util.InitialisationException;
import com.meidusa.venus.backend.EndpointInvocation;

public interface Interceptor extends Initialisable {
    public void init() throws InitialisationException;

    public void destroy();

    public Object intercept(EndpointInvocation invocation);
}
