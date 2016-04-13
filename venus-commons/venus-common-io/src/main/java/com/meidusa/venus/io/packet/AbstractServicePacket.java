package com.meidusa.venus.io.packet;

import java.io.UnsupportedEncodingException;

import com.meidusa.toolkit.net.packet.GenericIOPacketBuffer;

/**
 * 
 * @author Struct, Sun Ning
 */
public abstract class AbstractServicePacket extends AbstractVenusPacket implements PacketConstant {

    private static final long serialVersionUID = 1L;

    /**
     * 表示该数据包采用的序列化方式，如果该值为-1则采用握手时候的序列化方式，如果>=0则采用相应的值, 在client 与 Service 通讯中通常为-1, 而在 HSB 与 Service中通常>=0
     * 
     * @deprecated 目前Bus中的序列化方式 @see {@code VenusRouterPacket#serializeType}
     */
    public byte serializeType = -1;

    /**
     * 用于表示该数据包中的对象字段的特性，用二进制表示： 00000001 表示是否采用压缩, 00000010 表示是否采用加密
     */
    public byte flags;
    /**
     * 客户端ID
     */
    public int clientId;// 4

    /**
     * 客户Request ID
     */
    public long clientRequestId;// 8

    protected void afterPacketWritten(ServicePacketBuffer buffer) {
        super.afterPacketWritten(buffer);
        int position = buffer.getPosition();
        buffer.setPosition(11);
        buffer.writeByte(flags);
        buffer.setPosition(position);
    }

    @Override
    protected int calculatePacketSize() {
        return SERVICE_HEADER_SIZE;
    }

    @Override
    protected Class<ServicePacketBuffer> getPacketBufferClass() {
        return ServicePacketBuffer.class;
    }

    protected void writeHead(ServicePacketBuffer buffer) {
        super.writeHead(buffer);
        buffer.writeByte(serializeType);
        buffer.writeByte(flags);
        buffer.writeInt(clientId);
        buffer.writeLong(clientRequestId);

        buffer.setPosition(SERVICE_HEADER_SIZE);
    }

    @Override
    protected void initHead(ServicePacketBuffer buffer) {
        super.initHead(buffer);
        serializeType = buffer.readByte();
        flags = buffer.readByte();
        clientId = buffer.readInt();
        clientRequestId = buffer.readLong();
        buffer.setPosition(SERVICE_HEADER_SIZE);
    }

    protected void readBody(ServicePacketBuffer buffer) {

    }

    protected void writeBody(ServicePacketBuffer buffer) throws UnsupportedEncodingException {

    }

    public static long getPacketSequence(byte[] buf) {
        return GenericIOPacketBuffer.readLong(buf, SEQUENCE_POSITION);
    }

    public static int getClientID(byte[] buf) {
        return GenericIOPacketBuffer.readInt(buf, CLIENTID_POSITION);
    }

    public static void copyHead(AbstractServicePacket source, AbstractServicePacket target) {
        target.protocolVersion = source.protocolVersion;
        target.serializeType = source.serializeType;
        target.clientId = source.clientId;
        target.clientRequestId = source.clientRequestId;

    }
}
