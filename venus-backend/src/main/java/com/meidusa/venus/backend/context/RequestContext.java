/**
 * 
 */
package com.meidusa.venus.backend.context;

import java.util.Map;

import com.meidusa.venus.backend.RequestInfo;
import com.meidusa.venus.backend.services.Endpoint;
import com.meidusa.venus.util.ThreadLocalConstant;
import com.meidusa.venus.util.ThreadLocalMap;

public class RequestContext {
    private Endpoint endPointer;
    private Map<String, Object> paramters;
    private RequestInfo requestInfo;
    private String clientId;

    /**
     * 用于跟踪venus请求的id,该id一但产生,将带经过多个venus系统
     */
    private String traceId;
    private String rootId;
    private String parentId;
    private String messageId;

    public String getTraceId() {
        return traceId;
    }

    public void setTraceId(String traceId) {
        this.traceId = traceId;
    }

    public String getRootId() {
        return rootId;
    }

    public void setRootId(String rootId) {
        this.rootId = rootId;
    }

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public Map<String, Object> getParameters() {
        return paramters;
    }

    public void setParameters(Map<String, Object> convertedParameters) {
        this.paramters = convertedParameters;
    }

    public Endpoint getEndPointer() {
        return endPointer;
    }

    public void setEndPointer(Endpoint endPointer) {
        this.endPointer = endPointer;
    }

    public RequestInfo getRequestInfo() {
        return requestInfo;
    }

    public void setRequestInfo(RequestInfo requestInfo) {
        this.requestInfo = requestInfo;
    }

    public static RequestContext getRequestContext() {
        return (RequestContext) ThreadLocalMap.get(ThreadLocalConstant.REQUEST_CONTEXT);
    }
}
