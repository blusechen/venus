package com.meidusa.venus.io.packet;

import java.io.UnsupportedEncodingException;

/**
 * 
 * @author Struct
 * 
 */
public class PongPacket extends AbstractServicePacket {

    private static final long serialVersionUID = 1L;

    public PongPacket() {
        this.type = PacketConstant.PACKET_TYPE_PONG;
    }

    @Override
    protected void readBody(ServicePacketBuffer buffer) {
    }

    @Override
    protected void writeBody(ServicePacketBuffer buffer) throws UnsupportedEncodingException {
    }
}
