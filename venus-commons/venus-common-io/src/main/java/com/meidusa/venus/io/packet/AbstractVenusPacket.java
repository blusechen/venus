package com.meidusa.venus.io.packet;

import com.meidusa.toolkit.net.packet.AbstractPacket;
import com.meidusa.toolkit.net.packet.GenericIOPacketBuffer;

public abstract class AbstractVenusPacket extends AbstractPacket<ServicePacketBuffer> implements PacketConstant {
    private static final long serialVersionUID = 1L;
    public final static int VENUS_HEADER_LENGTH = 10;
    /**
     * 整个逻辑包长度（包括包头）
     */
    protected int packetLength;// 4

    public short protocolVersion = PROTOCOL_VERSION;// 2

    /**
     * 该数据包类型,命令类型
     */
    public int type;// 4

    protected void writeHead(ServicePacketBuffer buffer) {
        buffer.setPosition(0);
        buffer.writeInt(0);
        buffer.writeShort(protocolVersion);
        buffer.writeInt(type);
    }

    protected ServicePacketBuffer constractorBuffer(byte[] buffer) {
        return new ServicePacketBuffer(buffer);
    }

    protected ServicePacketBuffer constractorBuffer(int bufferSize) {
        return new ServicePacketBuffer(bufferSize);
    }

    @Override
    protected void initHead(ServicePacketBuffer buffer) {
        packetLength = buffer.readInt();
        protocolVersion = buffer.readShort();
        type = buffer.readInt();
    }

    @Override
    protected void afterPacketWritten(ServicePacketBuffer buffer) {
        int position = buffer.getPosition();
        packetLength = position;
        buffer.setPosition(0);
        buffer.writeInt(packetLength);
        buffer.setPosition(position);
    }

    public static int getType(byte[] buf) {
        return GenericIOPacketBuffer.readInt(buf, TYPE_POSITION);
    }
}
