package com.meidusa.venus.backend.network.handler;

import com.meidusa.toolkit.net.Connection;
import com.meidusa.venus.io.packet.AbstractServicePacket;
import com.meidusa.venus.io.packet.VenusRouterPacket;

/**
 * Created by huawei on 5/15/16.
 */
public class ResponseHandler {

    public void postMessageBack(Connection conn, VenusRouterPacket routerPacket, AbstractServicePacket source, AbstractServicePacket result) {
        if (routerPacket == null) {
            conn.write(result.toByteBuffer());
        } else {
            routerPacket.data = result.toByteArray();
            conn.write(routerPacket.toByteBuffer());
        }
    }
}
