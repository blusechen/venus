package com.meidusa.venus.io.packet;

import java.io.UnsupportedEncodingException;

public class ServiceAPIPacket extends AbstractServiceRequestPacket {
	public byte[] traceId;
	public byte[] params;
    public ServiceAPIPacket() {
    }

    private static final long serialVersionUID = 1L;

    protected void writeHead(ServicePacketBuffer buffer) {
        buffer.setPosition(SERVICE_HEADER_SIZE);
    }
    
    protected void writeBody(ServicePacketBuffer buffer) throws UnsupportedEncodingException {
        super.writeBody(buffer);
        buffer.writeLengthCodedBytes(params);
        //兼容3.0.1之前的版本,3.0.2与之后的版本将支持traceID
        if (traceId == null) {
            traceId = EMPTY_TRACE_ID;
        }
        buffer.writeBytes(traceId);
    }

    @Override
    protected void readBody(ServicePacketBuffer buffer) {
        super.readBody(buffer);
        params = buffer.readLengthCodedBytes();
        
        // 兼容3.0.1之前的版本,3.0.2与之后的版本将支持traceID
        if (buffer.hasRemaining()) {
            traceId = new byte[16];
            buffer.readBytes(traceId, 0, 16);
        } else {
            traceId = PacketConstant.EMPTY_TRACE_ID;
        }
    }
}
