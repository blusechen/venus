package com.meidusa.venus.io.decoder;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.builder.ToStringBuilder;

import com.meidusa.venus.io.packet.ServicePacketBuffer;
import com.meidusa.venus.io.serializer.bson.FastBsonSerializerWrapper;

public class Snippet {

    public static class Hello implements Serializable {
        private static final long serialVersionUID = 1L;
        private String name;
        private String greeting;
        private int age;
        private double cost;
        private Map<String, Long> map;

        public Map<String, Long> getMap() {
            return map;
        }

        public void setMap(Map<String, Long> map) {
            this.map = map;
        }

        public String getName() {
            return name;
        }

        public int getAge() {
            return age;
        }

        public void setAge(int age) {
            this.age = age;
        }

        public double getCost() {
            return cost;
        }

        public void setCost(double cost) {
            this.cost = cost;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getGreeting() {
            return greeting;
        }

        public void setGreeting(String greeting) {
            this.greeting = greeting;
        }

        public String toString() {
            return ToStringBuilder.reflectionToString(this);
        }

    }

    public static void main(String[] args) {
        FastBsonSerializerWrapper serializer = new FastBsonSerializerWrapper();
        Hello hello = new Hello();
        hello.setName("jack1");
        hello.setAge(13);
        hello.setGreeting("111asdfasdfqwer阿桑的发生的发");
        Map<String, Long> map = new HashMap<String, Long>();
        hello.setMap(map);
        map.put("1", 1L);
        map.put("2", new Long(2));
        map.put("3", 3L);

        Hello hello2 = new Hello();
        hello2.setName("jack2");
        hello2.setAge(13);
        hello2.setGreeting("222asdfasdfqwer阿桑的发生的发");

        Map<String, Hello> newmap = new HashMap<String, Hello>();
        newmap.put("name", hello2);
        newmap.put("hello", hello);

        Map<String, Type> typeMap = new HashMap<String, Type>();
        typeMap.put("name", Hello.class);
        typeMap.put("hello", Hello.class);
        long start = System.currentTimeMillis();
        ServicePacketBuffer buffer = new ServicePacketBuffer(1024);
        for (int i = 0; i < 1000000; i++) {
            serializer.encode(buffer, newmap);
            buffer.setPosition(0);
            serializer.decode(buffer, typeMap);

            // Object myObj = serializer.decode(bts, type);
        }

        System.out.println(System.currentTimeMillis() - start);
    }
}
