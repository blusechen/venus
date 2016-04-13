package com.meidusa.venus.io.serializer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public abstract class AbstractSerializer implements Serializer {
    static Map<Class, Class> implMap = new HashMap<Class, Class>();
    static {
        register(Map.class, HashMap.class);
        register(List.class, ArrayList.class);
        register(Set.class, HashSet.class);
    }

    public static void register(Class key, Class value) {
        implMap.put(key, value);
    }

    public Class findImplClass(Class clazz) {
        Class value = implMap.get(clazz);
        if (value == null) {
            if (clazz.isInterface()) {
                return null;
            } else {
                return clazz;
            }
        } else {
            return value;
        }
    }

}
