package com.meidusa.venus.client.nio;

import com.meidusa.venus.io.packet.AbstractServicePacket;
import com.meidusa.venus.io.packet.ServicePacketBuffer;
import com.meidusa.venus.io.packet.serialize.SerializeServiceResponsePacket;
import com.meidusa.venus.io.serializer.Serializer;
import com.meidusa.venus.io.serializer.SerializerFactory;
import com.meidusa.venus.io.utils.GZipUtil;

import java.lang.reflect.Type;

/**
 * Created by huawei on 5/20/16.
 */
public class NioSerializeServiceResponsePacket extends SerializeServiceResponsePacket {

    private Serializer serializer;
    private Type javaType;
    private byte[] traceId;

    public NioSerializeServiceResponsePacket(Serializer serializer, Type javaType) {
        super(serializer, javaType);
    }

    @Override
    protected void readBody(ServicePacketBuffer buffer) {
        serializer = SerializerFactory.getSerializer(serializeType);
        if (buffer.hasRemaining()) {
            byte f = (byte) (this.flags & CAPABILITY_GZIP);
            byte[] bts = buffer.readLengthCodedBytes();
            if (bts != null & bts.length > 0) {
                if (f == CAPABILITY_GZIP) {
                    bts = GZipUtil.decompress(bts);
                }
                System.out.println("return request id:" + clientRequestId);
                NioPacketWaitTask task = NioInvocationContainer.getInstance().get(clientRequestId);
                System.out.println(task);
                System.out.println(serializer);
                System.out.println("returnType:" + task.getReturnType());
                result = serializer.decode(bts, task.getReturnType());
                task.setResult(result);
                task.setComplete(true);
            }
        }

        // 兼容3.0.1之前的版本,3.0.2与之后的版本将支持traceID
        if (buffer.hasRemaining()) {
            traceId = new byte[16];
            buffer.readBytes(traceId);
        }
    }
}
