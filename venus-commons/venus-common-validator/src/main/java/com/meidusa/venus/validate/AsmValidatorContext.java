package com.meidusa.venus.validate;

import java.util.List;

public class AsmValidatorContext {
    String validatorClassName;
    Class<?> vistorClass;
    List<AsmValidatorClassInfo> classInfoList;
    List<AsmValidatorFieldInfo> fieldInfoList;

    public String getValidatorClassName() {
        return validatorClassName;
    }

    public void setValidatorClassName(String validatorClassName) {
        this.validatorClassName = validatorClassName;
    }

    public Class<?> getVistorClass() {
        return vistorClass;
    }

    public void setVistorClass(Class<?> vistorClass) {
        this.vistorClass = vistorClass;
    }

    public List<AsmValidatorClassInfo> getClassInfoList() {
        return classInfoList;
    }

    public void setClassInfoList(List<AsmValidatorClassInfo> classInfoList) {
        this.classInfoList = classInfoList;
    }

    public List<AsmValidatorFieldInfo> getFieldInfoList() {
        return fieldInfoList;
    }

    public void setFieldInfoList(List<AsmValidatorFieldInfo> fieldInfoList) {
        this.fieldInfoList = fieldInfoList;
    }

}
