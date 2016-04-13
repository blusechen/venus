package com.meidusa.venus.io.asm.test;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.junit.Test;

import com.meidusa.fastjson.JSON;
import com.meidusa.fastjson.JSONObject;
import com.meidusa.venus.util.ParameterizedTypeImpl;

public class TestJson {

    @Test
    public void testGenericArrayType(){
        Map<String,Set<Hello>> map = new HashMap<String,Set<Hello>>();
        Set<Hello> set = new HashSet<Hello>();
        Hello hello = new Hello();
        hello.setName("jack");
        hello.setAge(11);
        set.add(hello);
        
        
        map.put("1", set);
        ParameterizedType typeSet = ParameterizedTypeImpl.make(Set.class, new Type[] { Hello.class }, HashSet.class);
        ParameterizedType type = ParameterizedTypeImpl.make(Map.class, new Type[] { String.class, typeSet }, HashMap.class);
        String str= JSON.toJSONString(map);
        System.out.println(str);
        Map<String,Set<Hello>> map1 = JSONObject.parseObject(str,type);
        Set<Hello> l = map1.get("1");
        System.out.println(l.iterator().next());
    }
    
    @Test
    public void testArrayType(){
        Map<String,Set<Hello>> map = new HashMap<String,Set<Hello>>();
        Set<Hello> set = new HashSet<Hello>();
        Hello hello = new Hello();
        hello.setName("jack");
        hello.setAge(11);
        Map<String,Long> testMap = new HashMap<String,Long>();
        testMap.put("2",11L);
        hello.setMap(testMap);
        set.add(hello);
        
        map.put("1", set);
        map.put("1", set);
        ParameterizedType typeSet = ParameterizedTypeImpl.make(Set.class, new Type[] { Hello.class }, HashSet.class);
        ParameterizedType type = ParameterizedTypeImpl.make(Map.class, new Type[] { String.class, typeSet }, HashMap.class);
        Map<String,Set<Hello>> map1 = JSONObject.parseObject(JSON.toJSONString(map),type);
        Set<Hello> l = map1.get("1");
        System.out.println(l.iterator().next());
    }
    
    public static void main(String[] args) {
        Map<String,Hello[]> map = new HashMap<String,Hello[]>();
        Hello hello = new Hello();
        hello.setName("jack");
        hello.setAge(11);
        
        map.put("1", new Hello[]{hello,hello});
        map.put("2", new Hello[]{hello,hello});
        ParameterizedType type = ParameterizedTypeImpl.make(Map.class, new Type[] { String.class, Hello[].class }, HashMap.class);
        Map<String,Hello[]> map1 = JSONObject.parseObject(JSON.toJSONString(map),type);
        Hello[] l = map1.get("1");
        System.out.println(l[1]);
    }

}
