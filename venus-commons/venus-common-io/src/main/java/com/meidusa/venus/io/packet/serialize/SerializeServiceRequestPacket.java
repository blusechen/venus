package com.meidusa.venus.io.packet.serialize;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.util.Map;

import com.meidusa.venus.io.packet.AbstractServiceRequestPacket;
import com.meidusa.venus.io.packet.PacketConstant;
import com.meidusa.venus.io.packet.ServicePacketBuffer;
import com.meidusa.venus.io.serializer.Serializer;
import com.meidusa.venus.io.utils.GZipUtil;

public class SerializeServiceRequestPacket extends AbstractServiceRequestPacket {
    private static final long serialVersionUID = 1L;
    private Map<String, Type> typeMap;
    private Serializer serializer;

    public byte[] traceId;
    public byte[] rootId;
    public byte[] parentId;
    public byte[] messageId;

    public SerializeServiceRequestPacket(Serializer serializer, Map<String, Type> typeMap) {
        super();
        this.typeMap = typeMap;
        this.serializer = serializer;
    }

    protected void readBody(ServicePacketBuffer buffer) {
        super.readBody(buffer);
        readParams(buffer);

        // 兼容3.0.1之前的版本,3.0.2与之后的版本将支持traceID
        if (buffer.hasRemaining()) {
            traceId = new byte[16];
            buffer.readBytes(traceId, 0, 16);
        } else {
            traceId = PacketConstant.EMPTY_TRACE_ID;
        }

        // 从3.2.16版本协议增加调用链ID
        if (buffer.hasRemaining()) {
            rootId = readId(buffer);
            parentId = readId(buffer);
            messageId = readId(buffer);
        }


    }

    protected void writeBody(ServicePacketBuffer buffer) throws UnsupportedEncodingException {
        super.writeBody(buffer);
        writeParams(buffer);

        // 兼容3.0.1之前的版本,3.0.2与之后的版本将支持traceID
        if (traceId == null) {
            traceId = EMPTY_TRACE_ID;
        }
        buffer.writeBytes(traceId);

        // 从3.2.16版本协议增加调用链ID
        writeId(buffer, rootId);
        writeId(buffer, parentId);
        writeId(buffer, messageId);
    }

    protected void writeParams(ServicePacketBuffer buffer) {
        if (parameterMap != null) {
            byte[] bts = serializer.encode(parameterMap);
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
        }else{
            buffer.writeInt(0);
        }
    }

    protected void readParams(ServicePacketBuffer buffer) {
        if (buffer.hasRemaining()) {
            byte[] bts = buffer.readLengthCodedBytes();
            byte f = (byte) (this.flags & CAPABILITY_GZIP);
            if (bts != null & bts.length > 0) {
                if (f == CAPABILITY_GZIP) {
                    bts = GZipUtil.decompress(bts);
                }
                parameterMap = serializer.decode(bts, typeMap);
            }

        }
        if(parameterMap == null){
            parameterMap = EMP_MAP;
        }
    }

    private void writeId(ServicePacketBuffer buffer, byte[] id) {
        if (id != null) {
            buffer.writeLengthCodedBytes(id);
        }else {
            buffer.writeInt(0);
        }
    }

    private byte[] readId(ServicePacketBuffer buffer) {
        int length = buffer.readInt();
        if (length <= 0) {
            return null;
        }
        byte[] b = new byte[length];
        buffer.readBytes(b, 0, length);
        return b;
    }

}
