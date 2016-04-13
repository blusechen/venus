package com.meidusa.venus.io.serializer.decode.writer;

public interface PropertyWriter {
    void writeProperty(Object src, String name, Object param);
}
