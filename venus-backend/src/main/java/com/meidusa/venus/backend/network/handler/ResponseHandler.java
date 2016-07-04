package com.meidusa.venus.backend.network.handler;

/**
 * Created by godzillahua on 7/4/16.
 */
import com.meidusa.toolkit.net.Connection;
import com.meidusa.venus.io.packet.AbstractServicePacket;
import com.meidusa.venus.io.packet.VenusRouterPacket;

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
