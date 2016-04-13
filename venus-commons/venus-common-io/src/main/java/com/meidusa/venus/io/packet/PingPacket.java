package com.meidusa.venus.io.packet;

import java.io.UnsupportedEncodingException;

/**
 * 
 * @author Struct
 * 
 */
public class PingPacket extends AbstractServicePacket {

    private static final long serialVersionUID = 1L;

    public PingPacket() {
        this.type = PacketConstant.PACKET_TYPE_PING;
    }

    @Override
    protected void readBody(ServicePacketBuffer buffer) {
    }

    @Override
    protected void writeBody(ServicePacketBuffer buffer) throws UnsupportedEncodingException {
    }
}
