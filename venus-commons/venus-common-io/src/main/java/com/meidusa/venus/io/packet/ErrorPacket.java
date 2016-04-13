package com.meidusa.venus.io.packet;

import java.io.UnsupportedEncodingException;

public class ErrorPacket extends AbstractServicePacket {

    /**
	 * 
	 */
    private static final long serialVersionUID = 1L;
    public int errorCode;
    public String message;

    public byte[] additionalData;

    public ErrorPacket() {
        this.type = PacketConstant.PACKET_TYPE_ERROR;

    }

    @Override
    protected void writeBody(ServicePacketBuffer buffer) throws UnsupportedEncodingException {
        super.writeBody(buffer);
        buffer.writeInt(errorCode);
        buffer.writeLengthCodedString(message, PACKET_CHARSET);
        if (additionalData != null) {
            buffer.writeLengthCodedBytes(additionalData);
        }
    }

    @Override
    protected void readBody(ServicePacketBuffer buffer) {
        super.readBody(buffer);
        errorCode = buffer.readInt();
        message = buffer.readLengthCodedString(PACKET_CHARSET);
        if (buffer.hasRemaining()) {
            additionalData = buffer.readLengthCodedBytes();
        }
    }

    @Override
    public String toString() {
        return "E" + errorCode + ": " + message;
    }

}
