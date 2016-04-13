/**
 * 
 */
package com.meidusa.venus.convert;

import java.lang.reflect.Type;
import java.util.Map;

import com.meidusa.venus.exception.ConvertException;

/**
 * @author sunning
 * 
 */
public interface Converter {

    /**
     * for primitive types
     * 
     * @param value
     * @param type
     * @return
     * @throws InvalidParameterException
     */
    Object convert(Object value, Type type) throws ConvertException;

    /**
     * 
     * @param value
     * @param type
     * @param helper
     * @return
     * @throws InvalidParameterException
     */
    Object convert(Object value, Type type, String helper) throws ConvertException;

    /**
     * for pojo
     * 
     * @param props
     * @param type
     * @return
     */
    Object convert(String name, Object value, Type type, Map<String, Object> context) throws ConvertException;

}
