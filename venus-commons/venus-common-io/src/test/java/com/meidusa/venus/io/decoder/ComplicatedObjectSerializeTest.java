package com.meidusa.venus.io.decoder;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.meidusa.venus.io.packet.ServicePacketBuffer;
import com.meidusa.venus.io.serializer.Serializer;
import com.meidusa.venus.io.serializer.bson.FastBsonSerializerWrapper;

public class ComplicatedObjectSerializeTest {
    private ComplicatedObject obj;
    private Serializer serializer;

    @Before
    public void constructClass() {
        serializer = new FastBsonSerializerWrapper();
        obj = new ComplicatedObject();
        obj.setE(new BigDecimal("1.2598673"));
        obj.setA(new int[] { 1, 2, 3 });
        HashMap<String, ComplicatedComponentObject> map = new HashMap<String, ComplicatedComponentObject>();
        map.put("1~~", new ComplicatedComponentObject("1", 12345));
        map.put("2~~", new ComplicatedComponentObject("2", 23456));
        obj.setB(map);
        obj.setC("ccc");
        List<Long> list = new LinkedList<Long>();
        list.add(1L);
        list.add(2L);
        list.add(3L);
        obj.setD(list);
    }

    @Test
    public void testSerialize() {
        System.out.println(obj);
        long begin = System.currentTimeMillis();
        long encodeTime = 0;
        long decodeTime = 0;
        for (int i = 0; i < 100000; i++) {
            Object o = null;
            // System.out.println(HexDump.dumpHexData("ComplicatedObject",
            // serializedObj, serializedObj.length));
            long encodeBegin = System.currentTimeMillis();
            ServicePacketBuffer buffer = new ServicePacketBuffer(1024);
            serializer.encode(buffer, obj);
            encodeTime += System.currentTimeMillis() - encodeBegin;
            long decodeBegin = System.currentTimeMillis();
            buffer.setPosition(0);
            o = serializer.decode(buffer, obj.getClass());
            decodeTime += System.currentTimeMillis() - decodeBegin;
        }
        System.out.println(System.currentTimeMillis() - begin);
        System.out.println(encodeTime);
        System.out.println(decodeTime);
    }
}
