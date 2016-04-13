package com.meidusa.venus.io.serializer.bson;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.ArrayUtils;

import com.meidusa.fastbson.FastBsonSerializer;
import com.meidusa.fastbson.parse.BSONScanner;
import com.meidusa.fastbson.parse.BSONWriter;
import com.meidusa.fastbson.parse.ByteArrayBSONScanner;
import com.meidusa.fastbson.util.BSON;
import com.meidusa.venus.io.packet.ServicePacketBuffer;
import com.meidusa.venus.io.serializer.AbstractSerializer;
import com.meidusa.venus.notify.InvocationListener;
import com.meidusa.venus.notify.ReferenceInvocationListener;

public class FastBsonSerializerWrapper extends AbstractSerializer {
    static {
        FastBsonSerializer.registerReplace(InvocationListener.class, ReferenceInvocationListener.class);
    }
    private FastBsonSerializer serializer = new FastBsonSerializer();

    public void encode(ServicePacketBuffer buffer, Object obj) {
        BSONWriter writer = serializer.encode(obj);
        buffer.writeInt(writer.getLength());
        buffer.writeBytes(writer.getBuffer(), 0, writer.getLength());
    }

    public Object decode(ServicePacketBuffer buffer, Type type) {
        return decode(buffer.readLengthCodedBytes(), type);
    }

    public Map<String, Object> decode(ServicePacketBuffer buffer, Map<String, Type> typeMap) {
        return decode(buffer.readLengthCodedBytes(), typeMap);
    }

    public Map<String, Object> decode(byte[] buffer, Map<String, Type> typeMap) {
        BSONScanner scanner = new ByteArrayBSONScanner(buffer);
        scanner.skip(4);
        HashMap<String, Object> returnMap = new HashMap<String, Object>();
        while (scanner.readType() != BSON.EOO) {
            String field = scanner.readCString();
            Type type = typeMap.get(field);
            if (type != null) {
                returnMap.put(field, serializer.decode(scanner, type));
            } else {
                scanner.skipValue();
            }

        }
        return returnMap;
    }

    @Override
    public byte[] encode(Object obj) {
        BSONWriter writer = serializer.encode(obj);
        return ArrayUtils.subarray(writer.getBuffer(), 0, writer.getLength());
    }

    @Override
    public Object decode(byte[] bts, Type type) {
        BSONScanner scanner = new ByteArrayBSONScanner(bts);
        return serializer.decode(scanner, type);
    }

}
