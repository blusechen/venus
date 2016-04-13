package com.meidusa.venus.io.serializer;

import java.lang.reflect.Type;
import java.util.Map;

import com.meidusa.venus.io.packet.ServicePacketBuffer;

/**
 * 
 * @author struct
 * 
 */
public interface Serializer {

    public abstract void encode(ServicePacketBuffer buffer, Object obj);

    public abstract Object decode(ServicePacketBuffer buffer, Type type);

    public abstract Map<String, Object> decode(ServicePacketBuffer buffer, Map<String, Type> typeMap);

    public abstract byte[] encode(Object obj);

    public abstract Object decode(byte[] bts, Type type);

    public abstract Map<String, Object> decode(byte[] bts, Map<String, Type> typeMap);
}
