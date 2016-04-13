package com.meidusa.venus.io.serializer.decode.writer;

import java.util.List;

public class ListPropertyWriter implements PropertyWriter {
    public void writeProperty(Object src, String name, Object param) {
        try {
            ((List) src).add(param);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
