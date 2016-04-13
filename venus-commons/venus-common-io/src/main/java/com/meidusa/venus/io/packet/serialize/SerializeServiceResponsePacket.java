package com.meidusa.venus.io.packet.serialize;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.meidusa.venus.io.packet.PacketConstant;
import com.meidusa.venus.io.packet.ServicePacketBuffer;
import com.meidusa.venus.io.packet.ServiceResponsePacket;
import com.meidusa.venus.io.serializer.Serializer;
import com.meidusa.venus.io.utils.GZipUtil;

public class SerializeServiceResponsePacket extends ServiceResponsePacket {
    private static Logger logger = LoggerFactory.getLogger(SerializeServiceResponsePacket.class);
    private static final long serialVersionUID = 1L;
    private transient Type javaType;
    private Serializer serializer;

    /**
     * 用于跟踪请求的标记
     */
    public byte[] traceId;

    public SerializeServiceResponsePacket(Serializer serializer, Type javaType) {
        this.serializer = serializer;
        this.javaType = javaType;
    }

    protected void readBody(ServicePacketBuffer buffer) {
        super.readBody(buffer);
        if (buffer.hasRemaining()) {
            byte f = (byte) (this.flags & CAPABILITY_GZIP);
            byte[] bts = buffer.readLengthCodedBytes();
            if (bts != null & bts.length > 0) {
                if (f == CAPABILITY_GZIP) {
                    bts = GZipUtil.decompress(bts);
                }
                result = serializer.decode(bts, javaType);
            }
        }

        // 兼容3.0.1之前的版本,3.0.2与之后的版本将支持traceID
        if (buffer.hasRemaining()) {
            traceId = new byte[16];
            buffer.readBytes(traceId);
        }
    }

    @Override
    protected void writeBody(ServicePacketBuffer buffer) throws UnsupportedEncodingException {
        super.writeBody(buffer);
        byte[] bts = null;
        if (result != null) {
            try {
                bts = serializer.encode(result);
            } catch (RuntimeException e) {
                logger.error("encode error, class=" + result.getClass() + ", clientRequestId=" + clientRequestId + ", clientID=" + clientId, e);
                throw e;
            }
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
        } else {
            buffer.writeInt(0);
        }

        // 兼容3.0.1之前的版本,3.0.2与之后的版本将支持traceID
        if (traceId == null) {
            traceId = EMPTY_TRACE_ID;
        }
        buffer.writeBytes(traceId);
    }

}
