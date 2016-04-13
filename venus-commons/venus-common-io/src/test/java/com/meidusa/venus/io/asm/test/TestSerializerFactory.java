package com.meidusa.venus.io.asm.test;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Ignore;

import com.meidusa.fastbson.ASMSerializerFactory;
import com.meidusa.fastbson.parse.ByteArrayBSONWriter;
import com.meidusa.fastbson.serializer.ObjectSerializer;

@Ignore
public class TestSerializerFactory {
    public static void main(String[] args) {
        ObjectSerializer serializer = ASMSerializerFactory.getSerializer(Hello.class);
        Hello hello = new Hello();
        hello.setName("aaa");
        hello.setGreeting("gree");
        hello.setAge(1);
        hello.setPpp(1.5f);
        hello.setCost(1.6d);
        Map<String, Long> map = new HashMap<String, Long>();
        map.put("aaa", 1l);
        hello.setMap(map);
        List<String> arrayList = new ArrayList<String>();
        arrayList.add("123");
        hello.setList(arrayList);
        hello.setToday(new Date());
        hello.setNumbers(new int[] { 1, 2, 3 });
        Map<String, Inner> innermap = new HashMap<String, Inner>();
        innermap.put("a", new Inner("aaa", 1));
        hello.setInnerMap(innermap);
        hello.setInner(new Inner("bbb", 1));
        ByteArrayBSONWriter writer = new ByteArrayBSONWriter();
        long a = System.currentTimeMillis();
        // for(int i = 0 ; i < 1000000; i ++) {
        serializer.serialize(writer, hello, null, 0);
        // writer.clear();
        // }
        System.out.println(writer.toString());
        // long b = System.currentTimeMillis();
        // BSONScanner scanner = new BSONScanner(writer.getBuffer());
        // long c = System.currentTimeMillis();
        // for(int i = 0 ; i < 1000000; i ++) {
        // Hello temp = (Hello) serializer.deserialize(scanner, null, 0);
        // scanner.reset();
        // }
        // long d = System.currentTimeMillis();
        // // BSONEncoder encoder = new BSONEncoder();
        // // byte[] array = encoder.encode(hello);
        // // }
        // System.out.println(b-a);
        // System.out.println(d-c);
        // // System.out.println(writer.toString());
        // // System.out.println(HexDump.dumpHexData("array", array, array.length));

    }
}
