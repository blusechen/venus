package com.meidusa.venus.cache.trans;

import java.lang.reflect.Type;

public class ObjectWithType {

    private Object object;

    private Type objectType;

    public Object getObject() {
        return object;
    }

    public void setObject(Object object) {
        this.object = object;
    }

    public Type getObjectType() {
        return objectType;
    }

    public void setObjectType(Type objectType) {
        this.objectType = objectType;
    }

}
