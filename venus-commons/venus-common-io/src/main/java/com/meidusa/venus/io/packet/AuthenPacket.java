package com.meidusa.venus.io.packet;

import java.io.UnsupportedEncodingException;

import com.meidusa.venus.VenusMetaInfo;

public abstract class AuthenPacket extends AbstractServicePacket {
    private static final long serialVersionUID = 1L;
    public byte authType;
    public int capabilities = CAPABILITY_GZIP;

    /**
     * 内容序列化格式（支持：json、xml、bson）bson = 1 ,json=2 ,java =4
     */
    public byte shakeSerializeType;

    public String client = VENUS_CLIENT;
    public String version = VenusMetaInfo.VENUS_VERSION;
    public String username;

    public AuthenPacket() {
        this.type = PacketConstant.PACKET_TYPE_AUTHEN;
    }

    @Override
    protected void writeBody(ServicePacketBuffer buffer) throws UnsupportedEncodingException {
        super.writeBody(buffer);
        buffer.writeByte(authType);
        buffer.writeInt(capabilities);
        buffer.writeByte(shakeSerializeType);
        buffer.writeLengthCodedString(client, PACKET_CHARSET);
        buffer.writeLengthCodedString(version, PACKET_CHARSET);
        buffer.writeLengthCodedString(username, PACKET_CHARSET);

    }

    protected void readBody(ServicePacketBuffer buffer) {
        super.readBody(buffer);
        authType = buffer.readByte();
        capabilities = buffer.readInt();
        shakeSerializeType = buffer.readByte();
        client = buffer.readLengthCodedString(PACKET_CHARSET);
        version = buffer.readLengthCodedString(PACKET_CHARSET);
        username = buffer.readLengthCodedString(PACKET_CHARSET);
    }

    /**
     * 数据包头后的第一个字节
     * 
     * @param message
     * @return
     */
    public static byte getAuthenType(byte[] message) {
        return (byte) message[SERVICE_HEADER_SIZE];
    }
}
