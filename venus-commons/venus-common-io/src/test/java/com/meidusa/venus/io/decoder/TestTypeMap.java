package com.meidusa.venus.io.decoder;

import java.util.HashMap;
import java.util.Map;

import com.meidusa.fastbson.util.HexDump;
import com.meidusa.venus.io.serializer.bson.FastBsonSerializerWrapper;

public class TestTypeMap {

    public static class A {
        private String aa;
        private String bb;

        public A() {
        };

        public A(String a, String b) {
            aa = a;
            bb = b;
        }

        public String getAa() {
            return aa;
        }

        public void setAa(String aa) {
            this.aa = aa;
        }

        public String getBb() {
            return bb;
        }

        public void setBb(String bb) {
            this.bb = bb;
        }

        @Override
        public String toString() {
            return "A [aa=" + aa + ", bb=" + bb + "]";
        }

    }

    public static void main(String[] args) {
        FastBsonSerializerWrapper wrapper = new FastBsonSerializerWrapper();
        // BSONWriter writer = new ByteArrayBSONWriter();
        HashMap map = new HashMap();
        map.put("o1", new A("A", "A"));
        map.put("o2", new A("A", "A"));
        map.put("o3", new A("A", "A"));
        Map typeMap = new HashMap();
        typeMap.put("o1", A.class);
        typeMap.put("o3", A.class);
        byte[] bts = wrapper.encode(map);
        System.out.println(HexDump.dumpHexData("a", bts, bts.length));
        Map mapReturn = wrapper.decode(bts, typeMap);

        System.out.println(mapReturn);
    }

}
