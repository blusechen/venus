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

import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.pool.ObjectPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.ObjectFactory;

import com.meidusa.fastjson.JSON;
import com.meidusa.fastmark.feature.SerializerFeature;
import com.meidusa.toolkit.common.bean.util.InitialisationException;
import com.meidusa.toolkit.net.BackendConnection;
import com.meidusa.toolkit.net.BackendConnectionPool;
import com.meidusa.toolkit.util.TimeUtil;
import com.meidusa.venus.annotations.Endpoint;
import com.meidusa.venus.annotations.ExceptionCode;
import com.meidusa.venus.annotations.PerformanceLevel;
import com.meidusa.venus.annotations.RemoteException;
import com.meidusa.venus.annotations.Service;
import com.meidusa.venus.annotations.util.AnnotationUtil;
import com.meidusa.venus.client.xml.bean.EndpointConfig;
import com.meidusa.venus.client.xml.bean.ServiceConfig;
import com.meidusa.venus.exception.CodedException;
import com.meidusa.venus.exception.DefaultVenusException;
import com.meidusa.venus.exception.InvalidParameterException;
import com.meidusa.venus.exception.RemoteSocketIOException;
import com.meidusa.venus.exception.VenusConfigException;
import com.meidusa.venus.exception.VenusExceptionFactory;
import com.meidusa.venus.extension.athena.AthenaTransactionId;
import com.meidusa.venus.extension.athena.delegate.AthenaTransactionDelegate;
import com.meidusa.venus.io.network.AbstractBIOConnection;
import com.meidusa.venus.io.packet.AbstractServicePacket;
import com.meidusa.venus.io.packet.ErrorPacket;
import com.meidusa.venus.io.packet.OKPacket;
import com.meidusa.venus.io.packet.PacketConstant;
import com.meidusa.venus.io.packet.ServicePacketBuffer;
import com.meidusa.venus.io.packet.ServiceResponsePacket;
import com.meidusa.venus.io.packet.serialize.SerializeServiceRequestPacket;
import com.meidusa.venus.io.packet.serialize.SerializeServiceResponsePacket;
import com.meidusa.venus.io.serializer.Serializer;
import com.meidusa.venus.io.serializer.SerializerFactory;
import com.meidusa.venus.metainfo.EndpointParameter;
import com.meidusa.venus.notify.InvocationListener;
import com.meidusa.venus.notify.ReferenceInvocationListener;
import com.meidusa.venus.poolable.RequestLoadbalanceObjectPool;
import com.meidusa.venus.util.UUID;
import com.meidusa.venus.util.Utils;
import com.meidusa.venus.util.VenusAnnotationUtils;
import com.meidusa.venus.util.VenusTracerUtil;

/**
 * 
 * @author Struct
 * 
 */
public class RemotingInvocationHandler extends VenusInvocationHandler{
	private static SerializerFeature[] JSON_FEATURE = new SerializerFeature[]{SerializerFeature.ShortString,SerializerFeature.IgnoreNonFieldGetter,SerializerFeature.SkipTransientField};
    private static Logger logger = LoggerFactory.getLogger(RemotingInvocationHandler.class);
    private static Logger performanceLogger = LoggerFactory.getLogger("venus.client.performance");
    private InvocationListenerContainer container;
    private ObjectPool bioConnPool;
    private BackendConnectionPool nioConnPool;
    private static AtomicLong sequenceId = new AtomicLong(1);
    private VenusServiceFactory serviceFactory;
    private VenusExceptionFactory venusExceptionFactory;
    private boolean enableAsync = true;
    private byte serializeType = PacketConstant.CONTENT_TYPE_JSON;

    public boolean isEnableAsync() {
        return enableAsync;
    }

    public void setEnableAsync(boolean enableAsync) {
        this.enableAsync = enableAsync;
    }

    public VenusExceptionFactory getVenusExceptionFactory() {
        return venusExceptionFactory;
    }

    public void setVenusExceptionFactory(VenusExceptionFactory venusExceptionFactory) {
        this.venusExceptionFactory = venusExceptionFactory;
    }

    public void setServiceFactory(VenusServiceFactory serviceFactory) {
        this.serviceFactory = serviceFactory;
    }

    public short getSerializeType() {
        return serializeType;
    }

    public void setSerializeType(byte serializeType) {
        this.serializeType = serializeType;
    }

    public InvocationListenerContainer getContainer() {
        return container;
    }

    public void setContainer(InvocationListenerContainer container) {
        this.container = container;
    }

    public ObjectPool getBioConnPool() {
        return bioConnPool;
    }

    public void setBioConnPool(ObjectPool bioConnPool) {
        this.bioConnPool = bioConnPool;
    }

    public BackendConnectionPool getNioConnPool() {
        return nioConnPool;
    }

    public void setNioConnPool(BackendConnectionPool nioConnPool) {
        this.nioConnPool = nioConnPool;
    }

    protected Object invokeRemoteService(Service service, Endpoint endpoint, Method method, EndpointParameter[] params, Object[] args) throws Exception {
        String apiName = VenusAnnotationUtils.getApiname(method, service, endpoint);


        AthenaTransactionId athenaTransactionId = null;
        if (service.athenaFlag()) {
            athenaTransactionId = AthenaTransactionDelegate.getDelegate().startClientTransaction(apiName);
        }
        boolean async = false;

        if (endpoint.async()) {
            async = true;
        }

        byte[] traceID = VenusTracerUtil.getTracerID();

        if (traceID == null) {
            traceID = VenusTracerUtil.randomTracerID();
        }        

        Serializer serializer = SerializerFactory.getSerializer(serializeType);

        SerializeServiceRequestPacket serviceRequestPacket = new SerializeServiceRequestPacket(serializer, null);

        serviceRequestPacket.clientId = PacketConstant.VENUS_CLIENT_ID;
        serviceRequestPacket.clientRequestId = sequenceId.getAndIncrement();
        serviceRequestPacket.traceId = traceID;
        serviceRequestPacket.apiName = apiName;
        serviceRequestPacket.serviceVersion = service.version();
        serviceRequestPacket.parameterMap = new HashMap<String, Object>();

        if (params != null) {
            for (int i = 0; i < params.length; i++) {
                if (args[i] instanceof InvocationListener) {
                    async = true;
                    ReferenceInvocationListener listener = new ReferenceInvocationListener();
                    ServicePacketBuffer buffer = new ServicePacketBuffer(16);
                    buffer.writeLengthCodedString(args[i].getClass().getName(), "utf-8");
                    buffer.writeInt(System.identityHashCode(args[i]));
                    listener.setIdentityData(buffer.toByteBuffer().array());
                    Type type = method.getGenericParameterTypes()[i];
                    if (type instanceof ParameterizedType) {
                        ParameterizedType genericType = ((ParameterizedType) type);
                        container.putInvocationListener((InvocationListener) args[i], genericType.getActualTypeArguments()[0]);
                    } else {
                        throw new InvalidParameterException("invocationListener is not generic");
                    }

                    serviceRequestPacket.parameterMap.put(params[i].getParamName(), listener);
                } else {
                    serviceRequestPacket.parameterMap.put(params[i].getParamName(), args[i]);
                }

            }
        }
        setTransactionId(serviceRequestPacket, athenaTransactionId);

        PerformanceLevel pLevel = AnnotationUtil.getAnnotation(method.getAnnotations(), PerformanceLevel.class);
        long start = TimeUtil.currentTimeMillis();
        long borrowed = start;

        if (async) {
            if (!this.isEnableAsync()) {
                throw new VenusConfigException("service async call disabled");
            }

            BackendConnection conn = null;
            try {

                if (nioConnPool instanceof RequestLoadbalanceObjectPool) {
                    conn = (BackendConnection) ((RequestLoadbalanceObjectPool) nioConnPool).borrowObject(serviceRequestPacket.parameterMap, endpoint);
                } else {
                    conn = nioConnPool.borrowObject();
                }
                borrowed = TimeUtil.currentTimeMillis();
                ByteBuffer buffer = serviceRequestPacket.toByteBuffer();
                if(service.athenaFlag()) {
                    AthenaTransactionDelegate.getDelegate().setClientOutputSize(buffer.limit());
                }
                conn.write(buffer);
                VenusTracerUtil.logRequest(traceID, serviceRequestPacket.apiName, JSON.toJSONString(serviceRequestPacket.parameterMap,JSON_FEATURE));
                return null;
            } finally {
                if (service.athenaFlag()) {
                    AthenaTransactionDelegate.getDelegate().completeClientTransaction();
                }
                if (performanceLogger.isDebugEnabled()) {
                    long end = TimeUtil.currentTimeMillis();
                    long time = end - borrowed;
                    StringBuffer buffer = new StringBuffer();
                    buffer.append("[").append(borrowed - start).append(",").append(time).append("]ms (client-async) traceID=").append(UUID.toString(traceID)).append(", api=").append(serviceRequestPacket.apiName);

                    performanceLogger.debug(buffer.toString());
                }

                if (conn != null) {
                    nioConnPool.returnObject(conn);
                }
            }
        } else {
            AbstractBIOConnection conn = null;
            int soTimeout = 0;
            int oldTimeout = 0;
            boolean success = true;
            int errorCode = 0;
            AbstractServicePacket packet = null;
            String remoteAddress = null;
            boolean invalided = false;
            boolean nullForSystemException = false;
            try {
                if (bioConnPool instanceof RequestLoadbalanceObjectPool) {
                    conn = (AbstractBIOConnection) ((RequestLoadbalanceObjectPool) bioConnPool).borrowObject(serviceRequestPacket.parameterMap, endpoint);
                } else {
                    conn = (AbstractBIOConnection) bioConnPool.borrowObject();
                }
                remoteAddress =  conn.getRemoteAddress();
                borrowed = TimeUtil.currentTimeMillis();
                ServiceConfig config = this.serviceFactory.getServiceConfig(method.getDeclaringClass());

                oldTimeout = conn.getSoTimeout();
                if (config != null) {
                    EndpointConfig endpointConfig = config.getEndpointConfig(endpoint.name());
                    if (endpointConfig != null) {
                        int eTimeOut = endpointConfig.getTimeWait();
                        if (eTimeOut > 0) {
                            soTimeout = eTimeOut;
                        }
                    } else {
                        if (config.getTimeWait() > 0) {
                            soTimeout = config.getTimeWait();
                        } else {
                            if (endpoint.timeWait() > 0) {
                                soTimeout = endpoint.timeWait();
                            }
                        }
                    }

                } else {

                    if (endpoint.timeWait() > 0) {
                        soTimeout = endpoint.timeWait();
                    }
                }
                
                byte[] bts;
                
                try {
                
                	if (soTimeout > 0) {
                		conn.setSoTimeout(soTimeout);
                	}

                    byte[] buff = serviceRequestPacket.toByteArray();
                    if(service.athenaFlag() && buff != null) {
                        AthenaTransactionDelegate.getDelegate().setClientOutputSize(buff.length);
                    }
                    conn.write(buff);
                    VenusTracerUtil.logRequest(traceID, serviceRequestPacket.apiName, JSON.toJSONString(serviceRequestPacket.parameterMap,JSON_FEATURE));
                    bts = conn.read();
                    if(service.athenaFlag() && bts != null) {
                        AthenaTransactionDelegate.getDelegate().setClientInputSize(bts.length);
                    }
                }catch(IOException e){
                	try {
                        conn.close();
                    } catch (Exception e1) {
                        // ignore
                    }
                    
                	bioConnPool.invalidateObject(conn);
                	invalided = true;
                	Class<?>[] eClass = method.getExceptionTypes();
                	
                	if(eClass != null && eClass.length > 0){
	                	for(Class<?> clazz : eClass){
	                		if(e.getClass().isAssignableFrom(clazz)){
	                			throw e;
	                		}
	                	}
                	}
                	
                    throw new RemoteSocketIOException("api="+serviceRequestPacket.apiName+ ", remoteIp=" + conn.getRemoteAddress()+",("+e.getMessage() + ")",e);
                }

                int type = AbstractServicePacket.getType(bts);
                switch (type) {
                    case PacketConstant.PACKET_TYPE_ERROR:
                        ErrorPacket error = new ErrorPacket();
                        error.init(bts);
                        packet = error;
                        Exception e = venusExceptionFactory.getException(error.errorCode, error.message);
                        if (e == null) {
                            throw new DefaultVenusException(error.errorCode, error.message);
                        } else {
                            if (error.additionalData != null) {
                                Map<String, Type> tmap = Utils.getBeanFieldType(e.getClass(), Exception.class);
                                if (tmap != null && tmap.size() > 0) {
                                    Object obj = serializer.decode(error.additionalData, tmap);
                                    BeanUtils.copyProperties(e, obj);
                                }
                            }
                            throw e;
                        }
                    case PacketConstant.PACKET_TYPE_OK:
                        OKPacket ok = new OKPacket();
                        ok.init(bts);
                        packet = ok;
                        return null;
                    case PacketConstant.PACKET_TYPE_SERVICE_RESPONSE:
                        ServiceResponsePacket response = new SerializeServiceResponsePacket(serializer, method.getGenericReturnType());
                        response.init(bts);
                        packet = response;
                        return response.result;
                    default: {
                        logger.warn("unknow response type=" + type);
                        success = false;
                        return null;
                    }
                }
            } catch (Exception e) {
                success = false;
                
                if (e instanceof CodedException || (errorCode = venusExceptionFactory.getErrorCode(e.getClass())) != 0 || e instanceof RuntimeException) {
                    if(e instanceof CodedException){
                        errorCode = ((CodedException) e).getErrorCode();
                    }
                    throw e;
                } else {
                    RemoteException code = e.getClass().getAnnotation(RemoteException.class);
                    if(code != null){
                        errorCode = code.errorCode();
                        throw e;
                    }else{
                        ExceptionCode eCode =   e.getClass().getAnnotation(ExceptionCode.class);
                        if(eCode != null){
                            errorCode = eCode.errorCode();
                            throw e;
                        }else{
                            errorCode = -1;
                            if (conn == null) {
                                throw new DefaultVenusException(e.getMessage(), e);
                            } else {
                                throw new DefaultVenusException(e.getMessage() + ". remoteAddress=" + conn.getRemoteAddress(), e);
                            }
                        }
                    }
                }
            } finally {
                if (service.athenaFlag()) {
                    AthenaTransactionDelegate.getDelegate().completeClientTransaction();
                }
                long end = TimeUtil.currentTimeMillis();
                long time = end - borrowed;
                StringBuffer buffer = new StringBuffer();
                buffer.append("[").append(borrowed - start).append(",").append(time).append("]ms (client-sync) traceID=").append(UUID.toString(traceID)).append(", api=").append(serviceRequestPacket.apiName);
                if (remoteAddress != null) {
                    buffer.append(", remote=").append(remoteAddress);
                }else{
                	buffer.append(", pool=").append(bioConnPool.toString());
                }
                buffer.append(", clientID=").append(PacketConstant.VENUS_CLIENT_ID).append(", requestID=").append(serviceRequestPacket.clientRequestId);

                if(packet != null){
                	if(packet instanceof ErrorPacket){
                		buffer.append(", errorCode=").append(((ErrorPacket) packet).errorCode);
                		buffer.append(", message=").append(((ErrorPacket) packet).message);
                	}else {
                		buffer.append(", errorCode=0");
                	}
                }
                
                if (pLevel != null) {

                    if (pLevel.printParams()) {
                        buffer.append(", params=");
                        buffer.append(JSON.toJSONString(serviceRequestPacket.parameterMap,JSON_FEATURE));
                    }

                    if (time > pLevel.error() && pLevel.error() > 0) {
                        if (performanceLogger.isErrorEnabled()) {
                            performanceLogger.error(buffer.toString());
                        }
                    } else if (time > pLevel.warn() && pLevel.warn() > 0) {
                        if (performanceLogger.isWarnEnabled()) {
                            performanceLogger.warn(buffer.toString());
                        }
                    } else if (time > pLevel.info() && pLevel.info() > 0) {
                        if (performanceLogger.isInfoEnabled()) {
                            performanceLogger.info(buffer.toString());
                        }
                    } else {
                        if (performanceLogger.isDebugEnabled()) {
                            performanceLogger.debug(buffer.toString());
                        }
                    }
                } else {
            		buffer.append(", params=");
            		buffer.append(JSON.toJSONString(serviceRequestPacket.parameterMap,JSON_FEATURE));
                	
                    if (time >= 30 * 1000) {
                        if (performanceLogger.isErrorEnabled()) {
                            performanceLogger.error(buffer.toString());
                        }
                    } else if (time >= 10 * 1000) {
                        if (performanceLogger.isWarnEnabled()) {
                            performanceLogger.warn(buffer.toString());
                        }
                    } else if (time >= 5 * 1000) {
                        if (performanceLogger.isInfoEnabled()) {
                            performanceLogger.info(buffer.toString());
                        }
                    } else {
                        if (performanceLogger.isDebugEnabled()) {
                            performanceLogger.debug(buffer.toString());
                        }
                    }
                }

                if (conn != null && !invalided) {
                    if (!conn.isClosed() && soTimeout > 0) {
                        conn.setSoTimeout(oldTimeout);
                    }
                    bioConnPool.returnObject(conn);
                }

            }
        }
    }

    private void setTransactionId(SerializeServiceRequestPacket serviceRequestPacket, AthenaTransactionId athenaTransactionId) {
        if (athenaTransactionId != null) {
            if (athenaTransactionId.getRootId() != null) {
                serviceRequestPacket.rootId = athenaTransactionId.getRootId().getBytes();
            }

            if (athenaTransactionId.getParentId() != null) {
                serviceRequestPacket.parentId = athenaTransactionId.getParentId().getBytes();
            }

            if (athenaTransactionId.getMessageId() != null) {
                serviceRequestPacket.messageId = athenaTransactionId.getMessageId().getBytes();
            }
        }
    }

    public void init() throws InitialisationException {

    }
}
