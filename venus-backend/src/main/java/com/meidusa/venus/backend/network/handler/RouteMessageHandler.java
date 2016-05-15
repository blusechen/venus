package com.meidusa.venus.backend.network.handler;

import com.meidusa.toolkit.net.util.InetAddressUtil;
import com.meidusa.venus.io.packet.AbstractServicePacket;
import com.meidusa.venus.io.packet.VenusRouterPacket;

/**
 * Created by huawei on 5/15/16.
 */
public class RouteMessageHandler {

    private byte[] message;
    private VenusRouterPacket routerPacket;

    public RouteMessageHandler(byte[] message) {
        this.message = message;
    }

    public RouteMessageHandler init(){
        routerPacket = new VenusRouterPacket();
        routerPacket.original = message;
        routerPacket.init(message);
        return this;
    }

    public VenusRouterPacket getRouterPacket() {
        return routerPacket;
    }

    public int getType(){
        return AbstractServicePacket.getType(routerPacket.data);
    }

    public String getSourceIp(){
        return InetAddressUtil.intToAddress(routerPacket.srcIP);
    }

    public byte[] getMessage(){
        return routerPacket.data;
    }

    public byte getSerializeType(){
        return routerPacket.serializeType;
    }

}
