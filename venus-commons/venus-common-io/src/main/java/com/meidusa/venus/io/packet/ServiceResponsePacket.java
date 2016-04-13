package com.meidusa.venus.io.packet;

import java.io.UnsupportedEncodingException;

/**
 * 
 * @author Struct
 * 
 */
public abstract class ServiceResponsePacket extends AbstractServicePacket {

    private static final long serialVersionUID = 1L;
    public Object result;

    public ServiceResponsePacket() {
        this.type = PACKET_TYPE_SERVICE_RESPONSE;
    }

    protected void readBody(ServicePacketBuffer buffer) {
        super.readBody(buffer);
    }

    protected void writeBody(ServicePacketBuffer buffer) throws UnsupportedEncodingException {
        super.writeBody(buffer);
    }
}
