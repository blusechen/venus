package com.meidusa.venus.io.serializer.decode.writer;

public class WriterFactory {
    private static PropertyWriter listPropertyWriter = new ListPropertyWriter();
    private static PropertyWriter objectPropertyWriter = new ObjectPropertyWriter();
    private static PropertyWriter mapPropertyWriter = new MapPropertyWriter();

    public static PropertyWriter getListPropertyWriter() {
        return listPropertyWriter;
    }

    public static PropertyWriter getObjectPropertyWriter() {
        return objectPropertyWriter;
    }

    public static PropertyWriter getMapPropertyWriter() {
        return mapPropertyWriter;
    }
}
