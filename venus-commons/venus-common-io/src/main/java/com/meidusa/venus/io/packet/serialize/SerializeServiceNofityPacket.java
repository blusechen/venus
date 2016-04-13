package com.meidusa.venus.io.packet.serialize;

import java.lang.reflect.Type;

import com.meidusa.venus.io.packet.PacketConstant;
import com.meidusa.venus.io.packet.ServiceNofityPacket;
import com.meidusa.venus.io.packet.ServicePacketBuffer;
import com.meidusa.venus.io.serializer.Serializer;
import com.meidusa.venus.io.utils.GZipUtil;

public class SerializeServiceNofityPacket extends ServiceNofityPacket {
    private static final long serialVersionUID = 1L;
    private transient Type javaType;
    private transient Serializer serializer;

    public SerializeServiceNofityPacket(Serializer serializer, Type javaType) {
        this.serializer = serializer;
        this.javaType = javaType;
    }

    @Override
    public Object readCallBackObject(ServicePacketBuffer buffer) {
        if (buffer.hasRemaining()) {
            byte f = (byte) (this.flags & CAPABILITY_GZIP);
            byte[] bts = buffer.readLengthCodedBytes();
            if (bts != null & bts.length > 0) {
                if (f == CAPABILITY_GZIP) {
                    bts = GZipUtil.decompress(bts);
                }
                return serializer.decode(bts, javaType);
            }
        }
        return null;
    }

    @Override
    public void writeCallBackObject(ServicePacketBuffer buffer, Object callbackObject) {
        if (callbackObject != null) {
            byte[] bts = serializer.encode(callbackObject);
            if (bts != null) {
                if (PacketConstant.AUTO_COMPRESS_SIZE > 0 && bts.length > PacketConstant.AUTO_COMPRESS_SIZE) {
                    buffer.writeLengthCodedBytes(GZipUtil.compress(bts));
                    this.flags = (byte) (this.flags | CAPABILITY_GZIP);
                } else {
                    buffer.writeLengthCodedBytes(bts);
                }
            } else {
                buffer.writeInt(0);
            }
        }
    }

}
