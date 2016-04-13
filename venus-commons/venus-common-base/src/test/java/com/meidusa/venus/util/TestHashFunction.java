package com.meidusa.venus.util;

import java.util.SortedMap;
import java.util.TreeMap;

public class TestHashFunction {
    private SortedMap<Long, Object> circle = new TreeMap<Long, Object>();
    HashFunction function = new HashFunction();

    static class HashFunction {

        public long hash(Object key) {

            if (key == null) {
                return 0L;
            } else {
                return Math.abs(key.hashCode()) % 2;
            }
        }
    }

    public Object getConsistenthashPool(Object key) {
        if (circle.isEmpty()) {
            return null;
        }
        long hash = function.hash(key != null ? key.toString() : "");
        System.out.println("hash=" + hash);
        if (!circle.containsKey(hash)) {
            SortedMap<Long, Object> tailMap = circle.tailMap(hash);
            hash = tailMap.isEmpty() ? circle.firstKey() : tailMap.firstKey();
        }
        return circle.get(hash);
    }

    void putValue(Object value) {
        System.out.println("local=" + function.hash(value));
        circle.put(function.hash(value), value);
    }

    public static void main(String[] args) {
        System.out.println(System.currentTimeMillis());
        TestHashFunction test = new TestHashFunction();
        test.putValue("10.136.20.42:16800");
        test.putValue("10.136.20.43:16800");

        System.out.println(test.getConsistenthashPool(System.currentTimeMillis()));

    }

}
