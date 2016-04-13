package com.meidusa.venus.io.packet;

import java.nio.ByteOrder;

import com.meidusa.toolkit.net.packet.GenericIOPacketBuffer;

/**
 * 
 * @author Struct
 * 
 */
public class ServicePacketBuffer extends GenericIOPacketBuffer implements PacketConstant {
    public static ServicePacketBuffer BUFFER = new ServicePacketBuffer(0);

    public ServicePacketBuffer(byte[] buf) {
        super(buf);
    }

    public ServicePacketBuffer(int size) {
        super(size);
    }

    public static String readApiName(byte[] bts) {
        if (bts != null & bts.length > PacketConstant.SERVICE_HEADER_SIZE) {

        }
        return null;
    }

    public ByteOrder getByteOrder() {
        return ByteOrder.BIG_ENDIAN;
    }

    @Override
    public int getHeadSize() {
        return SERVICE_HEADER_SIZE;
    }

    public int readFieldLength() {
        return this.readInt();
    }

    @Override
    public void writeFieldLength(int length) {
        this.writeInt(length);
    }
}
