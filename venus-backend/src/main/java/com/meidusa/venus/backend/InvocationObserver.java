package com.meidusa.venus.backend;

import com.meidusa.venus.backend.context.RequestContext;

public interface InvocationObserver {

    void beforeInvoke(EndpointInvocation invocation, RequestContext context);

    void afterInvoke(EndpointInvocation invocation, RequestContext context);
}
