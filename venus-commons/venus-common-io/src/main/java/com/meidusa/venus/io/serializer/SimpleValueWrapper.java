package com.meidusa.venus.io.serializer;

public class SimpleValueWrapper {

    private Object value;

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public SimpleValueWrapper(Object value) {
        super();
        this.value = value;
    }

    public SimpleValueWrapper() {

    }

}
