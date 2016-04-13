package com.meidusa.venus.io.serializer.decode.writer;

import java.util.Map;

public class MapPropertyWriter implements PropertyWriter {
    @SuppressWarnings("unchecked")
    public void writeProperty(Object src, String name, Object param) {
        ((Map) src).put(name, param);
    }

}
