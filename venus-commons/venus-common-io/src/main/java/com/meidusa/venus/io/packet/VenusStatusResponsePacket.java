package com.meidusa.venus.io.packet;

import java.io.UnsupportedEncodingException;

public class VenusStatusResponsePacket extends AbstractServicePacket {

    private static final long serialVersionUID = 1L;

    public byte status;

    public VenusStatusResponsePacket() {
        this.type = PACKET_TYPE_VENUS_STATUS_RESPONSE;
    }

    @Override
    protected void readBody(ServicePacketBuffer buffer) {
        super.readBody(buffer);
        status = buffer.readByte();
    }

    @Override
    protected void writeBody(ServicePacketBuffer buffer) throws UnsupportedEncodingException {
        super.writeBody(buffer);
        buffer.writeByte(status);
    }

}
