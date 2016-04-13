package com.meidusa.venus.io.serializer.json;

import java.lang.reflect.Type;
import java.util.Map;

import com.meidusa.fastjson.JSON;
import com.meidusa.fastjson.parser.DefaultExtJSONParser;
import com.meidusa.venus.io.packet.PacketConstant;
import com.meidusa.venus.io.packet.ServicePacketBuffer;
import com.meidusa.venus.io.serializer.AbstractSerializer;

public class JsonSerializer extends AbstractSerializer implements PacketConstant {

    public Object decode(ServicePacketBuffer buffer, Type type) {
        if (buffer.hasRemaining()) {
            byte[] bts = buffer.readLengthCodedBytes();
            return decode(bts, type);
        }
        return null;
    }

    public Map<String, Object> decode(ServicePacketBuffer buffer, Map<String, Type> typeMap) {
        if (buffer.hasRemaining()) {
            byte[] bts = buffer.readLengthCodedBytes();
            return decode(bts, typeMap);
        }
        return null;
    }

    public void encode(ServicePacketBuffer buffer, Object obj) {
        if (obj != null) {
            byte[] bts = encode(obj);
            if (bts == null) {
                buffer.writeInt(0);
            } else {
                buffer.writeLengthCodedBytes(bts);
            }
        }
    }

    @Override
    public byte[] encode(Object obj) {
        if (obj != null) {
            return JSON.toJSONString(obj).getBytes(PACKET_CHARSET);

        }
        return null;
    }

    @Override
    public Object decode(byte[] bts, Type type) {
        DefaultExtJSONParser parser;
        parser = new DefaultExtJSONParser(new String(bts, PACKET_CHARSET).trim());

        return parser.parseObject(type);

    }

    @Override
    public Map<String, Object> decode(byte[] bts, Map<String, Type> typeMap) {
        if (bts != null && bts.length > 0) {
            DefaultExtJSONParser parser = new DefaultExtJSONParser(new String(bts, PACKET_CHARSET).trim());
            return parser.parseObjectWithTypeMap(typeMap);
        }
        return null;
    }

}
