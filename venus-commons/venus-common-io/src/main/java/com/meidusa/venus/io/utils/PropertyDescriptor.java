package com.meidusa.venus.io.utils;

import java.beans.IntrospectionException;
import java.lang.reflect.Method;

public class PropertyDescriptor extends java.beans.PropertyDescriptor {
    private Method writeMethod = null;
    private Method readMethod = null;

    public PropertyDescriptor(String propertyName, Class<?> beanClass) throws IntrospectionException {
        super(propertyName, beanClass);
    }

    public Method getWriteMethod() {
        if (writeMethod == null) {
            writeMethod = super.getWriteMethod();
        }

        return writeMethod;
    }

    public Method getReadMethod() {
        if (readMethod == null) {
            readMethod = super.getReadMethod();
        }

        return readMethod;
    }
}
