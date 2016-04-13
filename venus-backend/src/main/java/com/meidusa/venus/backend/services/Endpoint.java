/**
 * 
 */
package com.meidusa.venus.backend.services;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.meidusa.venus.backend.interceptor.InterceptorStack;
import com.meidusa.venus.backend.services.xml.bean.PerformanceLogger;

/**
 * describes a method of exposed service
 * 
 * @author Sun Ning
 * @since 2010-3-4
 */
public class Endpoint {

    private String name;
    private Method method;
    private int timeWait;
    private boolean async = false;
    private Parameter[] parameters;
    private boolean hasCtxParam;
    private Service service;
    private String keyExpression;
    // cache
    private volatile transient String[] parameterNames;
    // cache
    private transient String[] requiredParameterNames;
    // cache
    private volatile transient Map<String, Type> parameterTypeDict;

    private InterceptorStack interceptorStack;

    private PerformanceLogger performanceLogger;

    private boolean active = true;

    private boolean isVoid = true;

    public int getTimeWait() {
        return timeWait;
    }

    public void setTimeWait(int soTimeout) {
        this.timeWait = soTimeout;
    }

    public boolean isVoid() {
        return isVoid;
    }

    public void setVoid(boolean isVoid) {
        this.isVoid = isVoid;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public boolean isAsync() {
        return async;
    }

    public void setAsync(boolean async) {
        this.async = async;
    }

    /**
     * 
     * @return
     */
    public Map<String, Type> getParameterTypeDict() {
        if (parameterTypeDict == null) {
            synchronized (this) {
                if (parameterTypeDict == null) {
                    parameterTypeDict = new HashMap<String, Type>(this.parameters.length);
                    for (int i = 0; i < this.parameters.length; i++) {
                        parameterTypeDict.put(this.parameters[i].getParamName(), this.parameters[i].getType());
                    }
                }
            }
        }

        return parameterTypeDict;
    }

    /**
     * 
     * @return
     */
    public String[] getParameterNames() {
        String temp[] = parameterNames;
        if (temp == null) {
            synchronized (this) {
                temp = parameterNames;
                if (temp == null) {
                    temp = parameterNames = new String[this.parameters.length];
                    for (int i = 0; i < this.parameters.length; i++) {
                        parameterNames[i] = this.parameters[i].getParamName();
                    }
                }
            }
        }
        return temp;
    }

    /**
     * create an array of parameter to run the method, in the order of declaration. System parameter (such as delimiter)
     * will be ignored
     * 
     * @param requestParams
     * @return
     */
    public Object[] getParameterValues(Map<String, Object> requestParams) {
        List<Object> values = new ArrayList<Object>();

        for (int i = 0; i < this.getParameterNames().length; i++) {
            String name = this.getParameterNames()[i];
            Object data = requestParams.get(name);

            if (data == null) {
                values.add(this.getParameters()[i].getDefaultValue());
            } else {
                values.add(data);
            }
        }

        return values.toArray();
    }

    public synchronized String[] getRequiredParameterNames() {
        if (requiredParameterNames == null) {
            List<String> requiredSubList = new ArrayList<String>();

            for (Parameter p : this.getParameters()) {
                if (!p.isOptional()) {
                    requiredSubList.add(p.getParamName());
                }
            }

            requiredParameterNames = requiredSubList.toArray(new String[0]);
        }
        return requiredParameterNames;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the method
     */
    public Method getMethod() {
        return method;
    }

    /**
     * @param method the method to set
     */
    public void setMethod(Method method) {
        this.method = method;
    }

    /**
     * @return the arguments
     */
    public Parameter[] getParameters() {
        return parameters;
    }

    /**
     * @param arguments the arguments to set
     */
    public void setParameters(Parameter[] arguments) {
        this.parameters = arguments;
    }

    /**
     * @return the service
     */
    public Service getService() {
        return service;
    }

    /**
     * @param service the service to set
     */
    public void setService(Service service) {
        this.service = service;
    }

    public String getKeyExpression() {
        return keyExpression;
    }

    public void setKeyExpression(String keyExpression) {
        this.keyExpression = keyExpression;
    }

    public void setParameterTypeDict(Map<String, Type> parameterTypeDict) {
        this.parameterTypeDict = parameterTypeDict;
    }

    public void setRequiredParameterNames(String[] requiredParameterNames) {
        this.requiredParameterNames = requiredParameterNames;
    }

    public boolean isHasCtxParam() {
        return hasCtxParam;
    }

    public void setHasCtxParam(boolean hasCtxParam) {
        this.hasCtxParam = hasCtxParam;
    }

    public InterceptorStack getInterceptorStack() {
        return interceptorStack;
    }

    public void setInterceptorStack(InterceptorStack interceptorStack) {
        this.interceptorStack = interceptorStack;
    }

    public PerformanceLogger getPerformanceLogger() {
        return performanceLogger;
    }

    public void setPerformanceLogger(PerformanceLogger performanceLogger) {
        this.performanceLogger = performanceLogger;
    }

}
