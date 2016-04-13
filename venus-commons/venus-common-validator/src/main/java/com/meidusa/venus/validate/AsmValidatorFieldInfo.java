package com.meidusa.venus.validate;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class AsmValidatorFieldInfo {

    private String validatorFieldName;

    private Field field;
    private Method getMethod;

    private int annotionOrder;

    public Field getField() {
        return field;
    }

    public void setField(Field field) {
        this.field = field;
    }

    public Method getGetMethod() {
        return getMethod;
    }

    public void setGetMethod(Method getMethod) {
        this.getMethod = getMethod;
    }

    public int getAnnotionOrder() {
        return annotionOrder;
    }

    public void setAnnotionOrder(int annotionOrder) {
        this.annotionOrder = annotionOrder;
    }

    public String getValidatorFieldName() {
        return validatorFieldName;
    }

    public void setValidatorFieldName(String validatorFieldName) {
        this.validatorFieldName = validatorFieldName;
    }

}
