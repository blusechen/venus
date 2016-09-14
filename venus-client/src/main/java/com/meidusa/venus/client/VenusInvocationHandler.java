/*
 * Copyright 2008-2108 amoeba.meidusa.com 
 * 
 * 	This program is free software; you can redistribute it and/or modify it under the terms of 
 * the GNU AFFERO GENERAL PUBLIC LICENSE as published by the Free Software Foundation; either version 3 of the License, 
 * or (at your option) any later version. 
 * 
 * 	This program is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * See the GNU AFFERO GENERAL PUBLIC LICENSE for more details. 
 * 	You should have received a copy of the GNU AFFERO GENERAL PUBLIC LICENSE along with this program; 
 * if not, write to the Free Software Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package com.meidusa.venus.client;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectFactory;

import com.meidusa.toolkit.common.util.StringUtil;
import com.meidusa.venus.annotations.Endpoint;
import com.meidusa.venus.annotations.Service;
import com.meidusa.venus.annotations.util.AnnotationUtil;
import com.meidusa.venus.exception.CodedException;
import com.meidusa.venus.metainfo.EndpointParameter;
import com.meidusa.venus.metainfo.EndpointParameterUtil;
import com.meidusa.venus.util.VenusAnnotationUtils;

/**
 * 
 * @author Struct
 * 
 */
public abstract class VenusInvocationHandler implements InvocationHandler {
    private static Logger logger = LoggerFactory.getLogger(VenusInvocationHandler.class);
    private static Logger exceptionLogger = LoggerFactory.getLogger("venus.client.exception");
    private Map<String, Object> singletonServiceMap = new HashMap<String, Object>();

    public VenusInvocationHandler() {
    }

    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Service service = null;
        Endpoint endpoint = null;
        try {
            endpoint = AnnotationUtil.getAnnotation(method.getAnnotations(), Endpoint.class);
            if (endpoint != null) {
                service = AnnotationUtil.getAnnotation(method.getDeclaringClass().getAnnotations(), Service.class);

                if (StringUtil.isEmpty(service.implement())) {
                    EndpointParameter[] params = EndpointParameterUtil.getPrameters(method);

                    return invokeRemoteService(service, endpoint, method, params, args);
                } else {
                    Object serviceImpl = null;
                    if (service.singleton()) {
                        serviceImpl = singletonServiceMap.get(service.implement());
                        if (serviceImpl == null) {
                            synchronized (singletonServiceMap) {

                                serviceImpl = singletonServiceMap.get(service.implement());
                                if (serviceImpl == null) {
                                    serviceImpl = Class.forName(service.implement(), true, Thread.currentThread().getContextClassLoader()).newInstance();
                                    singletonServiceMap.put(service.implement(), serviceImpl);
                                }
                            }
                        }
                    } else {
                        serviceImpl = Class.forName(service.implement(), true, Thread.currentThread().getContextClassLoader()).newInstance();
                    }

                    return method.invoke(serviceImpl, args);
                }
            } else {
                if (method.getDeclaringClass().equals(Object.class)) {
                    return method.invoke(this, args);
                }else{
                    logger.error("remote invoke error: endpoint annotation not declare on method=" + method.getName());
                    throw new IllegalAccessException("remote invoke error: endpoint annotation not declare on method=" + method.getName());
                }
            }
        } catch (Throwable e) {

            if (!(e instanceof CodedException)) {
                if (exceptionLogger.isInfoEnabled()) {
                    exceptionLogger.info("invoke service error,api=" + VenusAnnotationUtils.getApiname(method, service, endpoint), e);
                }
            } else {
                if (exceptionLogger.isDebugEnabled()) {
                    exceptionLogger.debug("invoke service error,api=" + VenusAnnotationUtils.getApiname(method, service, endpoint), e);
                }
            }

            throw e;
        }
    }

    protected abstract Object invokeRemoteService(Service service, Endpoint endpoint, Method method, EndpointParameter[] params, Object[] args)
            throws Exception;

}
