/**
 * 
 */
package com.meidusa.venus.convert;

import java.lang.reflect.Type;
import java.util.Map;

import com.meidusa.venus.exception.ConvertException;

/**
 * 
 * @author Sun Ning
 * @since 2010-3-5
 */
public interface ConvertService {

    /**
     * 
     * @param raw
     * @param typeDict
     * @return
     * @throws ConvertException
     */
    Map<String, Object> convert(Map<String, Object> raw, Map<String, Type> typeDict) throws ConvertException;

    /**
     * 
     * @param key
     * @return
     */
    boolean isComplexType(String key);

    /**
     * 
     * @param key
     * @return
     */
    String getBeanName(String key);

    Object convert(String value, Type type) throws ConvertException;

}
