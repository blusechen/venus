package com.meidusa.venus.backend.network.handler;

import com.meidusa.toolkit.net.Connection;
import com.meidusa.toolkit.net.util.InetAddressUtil;
import com.meidusa.venus.backend.RequestInfo;
import com.meidusa.venus.backend.context.RequestContext;
import com.meidusa.venus.backend.services.Endpoint;
import com.meidusa.venus.backend.view.MediaTypes;
import com.meidusa.venus.io.network.VenusFrontendConnection;
import com.meidusa.venus.io.packet.AbstractServiceRequestPacket;
import com.meidusa.venus.io.packet.PacketConstant;
import com.meidusa.venus.io.packet.VenusRouterPacket;

import java.util.Map;

/**
 * Created by huawei on 5/15/16.
 */
public class RequestHandler {

    public RequestInfo getRequestInfo(byte packetSerializeType, VenusFrontendConnection conn, VenusRouterPacket routerPacket) {
        RequestInfo info = new RequestInfo();
        if (routerPacket != null) {
            info.setRemoteIp(InetAddressUtil.intToAddress(routerPacket.srcIP));
        } else {
            info.setRemoteIp(conn.getHost());
        }
        info.setProtocol(RequestInfo.Protocol.SOCKET);
        info.setClientId(conn.getClientId());
        if (packetSerializeType == PacketConstant.CONTENT_TYPE_JSON) {
            info.setAccept(MediaTypes.APPLICATION_JSON);
        } else if (packetSerializeType == PacketConstant.CONTENT_TYPE_BSON) {
            // info.setAccept(MediaTypes.APPLICATION_XML);
        } else if (packetSerializeType == PacketConstant.CONTENT_TYPE_OBJECT) {
            info.setAccept(MediaTypes.APPLICATION_XML);
        }

        return info;
    }

    public RequestContext createContext(RequestInfo info, Connection conn, Endpoint endpoint, Map<String, Object> parameters) {
        RequestContext context = new RequestContext();
        context.setParameters(parameters);
        context.setEndPointer(endpoint);
        context.setRequestInfo(info);
        return context;
    }
}
