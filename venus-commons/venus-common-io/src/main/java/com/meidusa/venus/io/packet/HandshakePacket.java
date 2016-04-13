package com.meidusa.venus.io.packet;

import java.io.UnsupportedEncodingException;

public class HandshakePacket extends AbstractServicePacket {
    private static final long serialVersionUID = 1L;
    public int capabilities = CAPABILITIES;
    public int supportAuthenMethod = PacketConstant.AUTH_CURRENT_SUPPORT;
    public String challenge;
    public String version;

    public HandshakePacket() {
        this.type = PACKET_TYPE_HANDSHAKE;
    }

    @Override
    protected void writeBody(ServicePacketBuffer buffer) throws UnsupportedEncodingException {
        super.writeBody(buffer);
        buffer.writeInt(capabilities);
        buffer.writeInt(supportAuthenMethod);
        buffer.writeLengthCodedString(challenge, PACKET_CHARSET);
        buffer.writeLengthCodedString(version, PACKET_CHARSET);
    }

    protected void readBody(ServicePacketBuffer buffer) {
        super.readBody(buffer);
        capabilities = buffer.readInt();
        supportAuthenMethod = buffer.readInt();
        challenge = buffer.readLengthCodedString(PACKET_CHARSET);
        version = buffer.readLengthCodedString(PACKET_CHARSET);
    }

}
