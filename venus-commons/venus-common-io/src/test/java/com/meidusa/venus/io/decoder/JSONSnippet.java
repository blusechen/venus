package com.meidusa.venus.io.decoder;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.builder.ToStringBuilder;

import com.meidusa.fastjson.JSON;
import com.meidusa.fastjson.parser.DefaultExtJSONParser;
import com.meidusa.venus.util.ParameterizedTypeImpl;

public class JSONSnippet {

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
        // FastBsonSerializerWrapper serializer = new FastBsonSerializerWrapper();
        Hello hello = new Hello();
        hello.setName("jack1");
        hello.setAge(13);
        hello.setGreeting("111asdfasdfqwerqqqqqq");
        Map<String, Long> map = new HashMap<String, Long>();
        hello.setMap(map);
        map.put("1", 1L);
        map.put("2", new Long(2));
        map.put("3", 3L);

        Hello hello2 = new Hello();
        hello2.setName("jack2");
        hello2.setAge(13);
        hello2.setGreeting("222asdfasdfqwerqqq");

        Map<String, Hello> newmap = new HashMap<String, Hello>();
        newmap.put("name", hello2);
        newmap.put("hello", hello);

        Type type = ParameterizedTypeImpl.make(Map.class, new Type[] { String.class, Hello.class }, null);
        Map<String, Type> typeMap = new HashMap<String, Type>();
        typeMap.put("name", Hello.class);
        typeMap.put("hello", Hello.class);
        long start = System.currentTimeMillis();
        for (int i = 0; i < 1000000; i++) {
            String str = JSON.toJSONString(newmap, true);
            DefaultExtJSONParser parser = new DefaultExtJSONParser(str.trim());
            // System.out.println(str);
            Object object = parser.parseObjectWithTypeMap(typeMap);
            // Map<String, Object> result = serializer.decode(bts, typeMap);
            // System.out.println(str);
            // System.out.println(object);
        }

        System.out.println(System.currentTimeMillis() - start);
    }
}
