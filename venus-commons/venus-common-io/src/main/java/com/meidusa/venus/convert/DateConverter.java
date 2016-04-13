/**
 * 
 */
package com.meidusa.venus.convert;

import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Map;

/**
 * @author Sun Ning
 * 
 */
public class DateConverter implements Converter {

    /*
     * (non-Javadoc)
     * @see com.meidusa.relation.servicegate.convert.Converter#convert(java.lang.String, java.lang.Class)
     */
    @Override
    public Object convert(Object value, Type type) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        if (type instanceof Class && value.getClass().isAssignableFrom((Class) type)) {
            return value;
        } else {
            try {
                return format.parse(value.toString());
            } catch (ParseException e) {
                return null;
            }
        }
    }

    /*
     * (non-Javadoc)
     * @see com.meidusa.relation.servicegate.convert.Converter#convert(java.lang.String, java.lang.Class,
     * java.lang.String)
     */
    @Override
    public Object convert(Object value, Type type, String helper) {
        throw new UnsupportedOperationException();
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
