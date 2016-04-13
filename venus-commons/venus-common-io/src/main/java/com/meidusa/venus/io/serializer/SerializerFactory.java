package com.meidusa.venus.io.serializer;

import com.meidusa.venus.io.extension.ExtensionResolver;
import com.meidusa.venus.io.packet.PacketConstant;
import com.meidusa.venus.io.serializer.bson.FastBsonSerializerWrapper;
import com.meidusa.venus.io.serializer.java.JavaSerializer;
import com.meidusa.venus.io.serializer.json.JsonSerializer;
import com.meidusa.venus.notify.ReferenceInvocationListener;

public class SerializerFactory {
    static {
        ExtensionResolver.doScanExtension();
    }
    private static JsonSerializer json = new JsonSerializer();
    private static FastBsonSerializerWrapper bson = new FastBsonSerializerWrapper();
    private static JavaSerializer java = new JavaSerializer();

    public static Serializer getSerializer(short type) {
        switch (type) {
            case PacketConstant.CONTENT_TYPE_JSON:
                return json;
            case PacketConstant.CONTENT_TYPE_BSON:
                return bson;
            case PacketConstant.CONTENT_TYPE_OBJECT:
                return java;
            default:
                return json;
        }

    }
    
    public static void main(String[] args){
    	SerializerFactory.bson.encode(new ReferenceInvocationListener());
    }
}
