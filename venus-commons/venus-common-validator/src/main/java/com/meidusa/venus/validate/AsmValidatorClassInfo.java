package com.meidusa.venus.validate;

public class AsmValidatorClassInfo {

    private String validatorFieldName;

    private Class<?> clazz;

    private int annotionOrder;

    public Class<?> getClazz() {
        return clazz;
    }

    public void setClazz(Class<?> clazz) {
        this.clazz = clazz;
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
