package com.meidusa.venus.io.decoder;

import java.util.Date;
import java.util.HashMap;

import org.junit.Before;
import org.junit.Test;

import com.meidusa.venus.io.packet.ServicePacketBuffer;
import com.meidusa.venus.io.serializer.Serializer;
import com.meidusa.venus.io.serializer.bson.FastBsonSerializerWrapper;

public class SimpleObjectSerialTest {
    private SimpleObject obj;
    private Serializer serializer;

    @Before
    public void constructClass() {
        serializer = new FastBsonSerializerWrapper();
        obj = new SimpleObject();
        obj.setA(1);
        obj.setB("1");
        obj.setC(new Date(System.currentTimeMillis()));
        obj.setD(1l);
        obj.setE(new HashMap<String, Integer>());
        obj.getE().put("1", 123);
        obj.getE().put("2", 234);
    }

    @Test
    public void simpleSerialize() {

        long begin = System.currentTimeMillis();
        ServicePacketBuffer buffer = new ServicePacketBuffer(1024);
        serializer.encode(buffer, obj);
        buffer.setPosition(0);
        Object o = serializer.decode(buffer, obj.getClass());
    }
}
