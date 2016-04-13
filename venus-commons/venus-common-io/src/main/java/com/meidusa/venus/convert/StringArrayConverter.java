/**
 * 
 */
package com.meidusa.venus.convert;

import java.lang.reflect.Type;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

/**
 * @author Sun Ning
 * 
 */
public class StringArrayConverter implements Converter {

    /*
     * (non-Javadoc)
     * @see com.meidusa.relation.servicegate.convert.Converter#convert(java.lang.String, java.lang.Class)
     */
    @Override
    public Object convert(Object value, Type type) {
        throw new UnsupportedOperationException();
    }

    /*
     * (non-Javadoc)
     * @see com.meidusa.relation.servicegate.convert.Converter#convert(java.lang.String, java.lang.Class,
     * java.lang.String)
     */
    @Override
    public Object convert(Object value, Type type, String helper) {
        if (value == null) {
            return null;
        }
        return StringUtils.split(value.toString(), helper);
    }

    /*
     * (non-Javadoc)
     * @see com.meidusa.relation.servicegate.convert.Converter#convert(java.lang.String, java.lang.String,
     * java.lang.Class, java.util.Map)
     */
    @Override
    public Object convert(String name, Object value, Type type, Map<String, Object> context) {
        throw new UnsupportedOperationException();
    }

}
