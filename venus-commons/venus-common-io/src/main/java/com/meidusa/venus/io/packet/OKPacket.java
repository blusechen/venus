package com.meidusa.venus.io.packet;

public class OKPacket extends AbstractServicePacket {
    private static final long serialVersionUID = 1L;

    public OKPacket() {
        this.type = PacketConstant.PACKET_TYPE_OK;

    }

}
