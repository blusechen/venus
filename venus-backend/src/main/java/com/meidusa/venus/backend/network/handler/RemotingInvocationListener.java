package com.meidusa.venus.backend.network.handler;

import java.beans.PropertyDescriptor;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.meidusa.venus.exception.CodedException;
import com.meidusa.venus.exception.VenusExceptionCodeConstant;
import com.meidusa.venus.io.network.VenusFrontendConnection;
import com.meidusa.venus.io.packet.AbstractServicePacket;
import com.meidusa.venus.io.packet.ServiceNofityPacket;
import com.meidusa.venus.io.packet.VenusRouterPacket;
import com.meidusa.venus.io.packet.serialize.SerializeServiceNofityPacket;
import com.meidusa.venus.io.packet.serialize.SerializeServiceRequestPacket;
import com.meidusa.venus.io.serializer.Serializer;
import com.meidusa.venus.io.serializer.SerializerFactory;
import com.meidusa.venus.notify.InvocationListener;
import com.meidusa.venus.notify.ReferenceInvocationListener;
import com.meidusa.venus.util.ThreadLocalMap;
import com.meidusa.venus.util.Utils;
import com.meidusa.venus.util.VenusTracerUtil;

public class RemotingInvocationListener<T> implements InvocationListener<T> {
    private static Logger logger = LoggerFactory.getLogger(RemotingInvocationListener.class);
    private VenusFrontendConnection conn;
    private ReferenceInvocationListener<T> source;
    private boolean isResponsed = false;
    private SerializeServiceRequestPacket request;
    private VenusRouterPacket routerPacket;

    public boolean isResponsed() {
        return isResponsed;
    }

    public RemotingInvocationListener(VenusFrontendConnection conn, ReferenceInvocationListener<T> source, SerializeServiceRequestPacket request,
            VenusRouterPacket routerPacket) {
        this.conn = conn;
        this.source = source;
        this.request = request;
        this.routerPacket = routerPacket;
    }

    @Override
    public void callback(T object) {
        Serializer serializer = SerializerFactory.getSerializer(conn.getSerializeType());
        ServiceNofityPacket response = new SerializeServiceNofityPacket(serializer, null);
        AbstractServicePacket.copyHead(request, response);
        response.callbackObject = object;
        response.apiName = request.apiName;
        response.identityData = source.getIdentityData();

        byte[] traceID = (byte[]) ThreadLocalMap.get(VenusTracerUtil.REQUEST_TRACE_ID);

        if (traceID == null) {
            traceID = VenusTracerUtil.randomUUID();
            ThreadLocalMap.put(VenusTracerUtil.REQUEST_TRACE_ID, traceID);
        }

        response.traceId = traceID;

        if (routerPacket != null) {
            routerPacket.data = response.toByteArray();
            conn.write(routerPacket.toByteBuffer());
        } else {
            conn.write(response.toByteBuffer());
        }
        isResponsed = true;
    }

    @Override
    public void onException(Exception e) {
        Serializer serializer = SerializerFactory.getSerializer(conn.getSerializeType());
        ServiceNofityPacket response = new SerializeServiceNofityPacket(serializer, null);
        AbstractServicePacket.copyHead(request, response);
        if (e instanceof CodedException) {
            CodedException codedException = (CodedException) e;
            response.errorCode = codedException.getErrorCode();
        } else {
            response.errorCode = VenusExceptionCodeConstant.UNKNOW_EXCEPTION;
        }

        if (e != null) {
            Map<String, PropertyDescriptor> mpd = Utils.getBeanPropertyDescriptor(e.getClass());
            Map<String, Object> additionalData = new HashMap<String, Object>();

            for (Map.Entry<String, PropertyDescriptor> entry : mpd.entrySet()) {
                try {
                    additionalData.put(entry.getKey(), entry.getValue().getReadMethod().invoke(e));
                } catch (Exception e1) {
                    logger.error("read bean properpty error", e1);
                }
            }
            response.additionalData = serializer.encode(additionalData);
            response.errorMessage = e.getMessage();
        }

        response.identityData = source.getIdentityData();

        response.apiName = request.apiName;

        byte[] traceID = VenusTracerUtil.getTracerID();

        if (traceID == null) {
            traceID = VenusTracerUtil.randomTracerID();
        }

        response.traceId = traceID;

        if (routerPacket != null) {
            routerPacket.data = response.toByteArray();
            conn.write(routerPacket.toByteBuffer());
        } else {
            conn.write(response.toByteBuffer());
        }

        isResponsed = true;
    }

}
