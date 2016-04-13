package com.meidusa.venus.metainfo;

import java.lang.reflect.Type;

public class EndpointParameter {

    private String paramName;
    private Type type;
    private Object defaultValue;
    private boolean optional;

    /**
     * @return the paramName
     */
    public String getParamName() {
        return paramName;
    }

    /**
     * @param paramName the paramName to set
     */
    public void setParamName(String paramName) {
        this.paramName = paramName;
    }

    /**
     * @return the type
     */
    public Type getType() {
        return type;
    }

    /**
     * @param type the type to set
     */
    public void setType(Type type) {
        this.type = type;
    }

    /**
     * @return the defaultValue
     */
    public Object getDefaultValue() {
        return defaultValue;
    }

    /**
     * @param defaultValue the defaultValue to set
     */
    public void setDefaultValue(Object defaultValue) {
        this.defaultValue = defaultValue;
    }

    /**
     * @param optional the optional to set
     */
    public void setOptional(boolean optional) {
        this.optional = optional;
    }

    /**
     * @return the optional
     */
    public boolean isOptional() {
        return optional;
    }
}
