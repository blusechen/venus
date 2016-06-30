package com.meidusa.venus.client.simple;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.beanutils.BeanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.meidusa.venus.annotations.Endpoint;
import com.meidusa.venus.annotations.Service;
import com.meidusa.venus.client.InvocationListenerContainer;
import com.meidusa.venus.client.VenusInvocationHandler;
import com.meidusa.venus.exception.DefaultVenusException;
import com.meidusa.venus.exception.InvalidParameterException;
import com.meidusa.venus.exception.VenusExceptionFactory;
import com.meidusa.venus.io.authenticate.Authenticator;
import com.meidusa.venus.io.network.VenusBIOConnection;
import com.meidusa.venus.io.network.VenusBIOConnectionFactory;
import com.meidusa.venus.io.packet.AbstractServicePacket;
import com.meidusa.venus.io.packet.AbstractServiceRequestPacket;
import com.meidusa.venus.io.packet.AuthenPacket;
import com.meidusa.venus.io.packet.ErrorPacket;
import com.meidusa.venus.io.packet.HandshakePacket;
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
import com.meidusa.venus.util.Utils;
import com.meidusa.venus.util.VenusAnnotationUtils;

/**
 * 采用短连接的形式与远程服务通讯
 * 
 * @author structchen
 * 
 */
public class SimpleInvocationHandler extends VenusInvocationHandler {
    private static Logger logger = LoggerFactory.getLogger(SimpleInvocationHandler.class);

    @Autowired
    private VenusExceptionFactory venusExceptionFactory;
    private static AtomicLong sequenceId = new AtomicLong(1);
    private InvocationListenerContainer container = new InvocationListenerContainer();
    private VenusBIOConnectionFactory connFactory = new VenusBIOConnectionFactory();

    private Authenticator<HandshakePacket, AuthenPacket> authenticator = null;
    public SimpleInvocationHandler(String host, int port, int coTimeout, int soTimeout) {
        connFactory.setHost(host);
        connFactory.setPort(port);
        connFactory.setCoTimeout(coTimeout);
        connFactory.setSoTimeout(soTimeout);
    }

    public Authenticator<HandshakePacket, AuthenPacket> getAuthenticator() {
		return authenticator;
	}

	public void setAuthenticator(
			Authenticator<HandshakePacket, AuthenPacket> authenticator) {
		this.authenticator = authenticator;
		connFactory.setAuthenticator(authenticator);
	}

	public VenusExceptionFactory getVenusExceptionFactory() {
        return venusExceptionFactory;
    }

    public void setVenusExceptionFactory(VenusExceptionFactory venusExceptionFactory) {
        this.venusExceptionFactory = venusExceptionFactory;
    }

    @Override
    protected Object invokeRemoteService(Service service, Endpoint endpoint, Method method, EndpointParameter[] params, Object[] args) throws Exception {
        AbstractServiceRequestPacket serviceRequestPacket = null;

        Serializer serializer = SerializerFactory.getSerializer(connFactory.getAuthenticator().getSerializeType());
        serviceRequestPacket = new SerializeServiceRequestPacket(serializer, null);
        serviceRequestPacket.clientId = PacketConstant.VENUS_CLIENT_ID;
        serviceRequestPacket.clientRequestId = sequenceId.getAndIncrement();

        serviceRequestPacket.apiName = VenusAnnotationUtils.getApiname(method, service, endpoint);
        serviceRequestPacket.serviceVersion = service.version();
        serviceRequestPacket.parameterMap = new HashMap<String, Object>();
        if (params != null) {
            for (int i = 0; i < params.length; i++) {
                if (args[i] instanceof InvocationListener) {
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

        /*
         * PerformanceLevel pLevel = AnnotationUtil.getAnnotation(method.getAnnotations(), PerformanceLevel.class); long
         * start = TimeUtil.currentTimeMillis();
         */
        VenusBIOConnection conn = connFactory.makeObject();
        try {
            conn.write(serviceRequestPacket.toByteArray());
            if (endpoint.timeWait() > 0) {
                conn.setSoTimeout(endpoint.timeWait());
            }
            if (connFactory.getSoTimeout() > 0 && connFactory.getSoTimeout() != endpoint.timeWait()) {
                conn.setSoTimeout(connFactory.getSoTimeout());
            }
            byte[] bts = conn.read();

            int type = AbstractServicePacket.getType(bts);

            switch (type) {
                case PacketConstant.PACKET_TYPE_ERROR:
                    ErrorPacket error = new ErrorPacket();
                    error.init(bts);
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
                    return null;
                case PacketConstant.PACKET_TYPE_SERVICE_RESPONSE:
                    ServiceResponsePacket response = null;

                    response = new SerializeServiceResponsePacket(serializer, method.getGenericReturnType());

                    response.init(bts);
                    return response.result;
                default: {
                    logger.warn("unknow response type=" + type);
                    return null;
                }
            }

        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (Exception e) {
                }
            }
        }
    }
}
