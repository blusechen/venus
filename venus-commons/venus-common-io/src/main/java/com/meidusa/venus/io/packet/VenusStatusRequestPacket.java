package com.meidusa.venus.io.packet;

import java.io.UnsupportedEncodingException;

public class VenusStatusRequestPacket extends AbstractServicePacket {

    private static final long serialVersionUID = 1L;
    public byte newStatus = 0;
    
    public VenusStatusRequestPacket() {
        this.type = PACKET_TYPE_VENUS_STATUS_REQUEST;
    }
    
    protected void readBody(ServicePacketBuffer buffer) {
    	super.readBody(buffer);
    	if(buffer.hasRemaining()){
    		newStatus = buffer.readByte();
    	}
    }

    protected void writeBody(ServicePacketBuffer buffer) throws UnsupportedEncodingException {
    	super.writeBody(buffer);
    	buffer.writeByte(newStatus);
    }
}
