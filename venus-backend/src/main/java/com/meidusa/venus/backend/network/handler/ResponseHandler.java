package com.meidusa.venus.backend.network.handler;

/**
 * Created by godzillahua on 7/4/16.
 */
import com.meidusa.toolkit.net.Connection;
import com.meidusa.venus.extension.athena.delegate.AthenaTransactionDelegate;
import com.meidusa.venus.io.packet.AbstractServicePacket;
import com.meidusa.venus.io.packet.VenusRouterPacket;

import java.nio.ByteBuffer;

public class ResponseHandler {

    public void postMessageBack(Connection conn, VenusRouterPacket routerPacket, AbstractServicePacket source, AbstractServicePacket result, boolean athenaFlag) {
        ByteBuffer byteBuffer;
        if (routerPacket == null) {
            byteBuffer = result.toByteBuffer();
            conn.write(byteBuffer);
        } else {
            routerPacket.data = result.toByteArray();
            byteBuffer = routerPacket.toByteBuffer();
            conn.write(byteBuffer);
        }

        if (athenaFlag) {
            AthenaTransactionDelegate.getDelegate().setServerOutputSize(byteBuffer.limit());
        }
    }
}
