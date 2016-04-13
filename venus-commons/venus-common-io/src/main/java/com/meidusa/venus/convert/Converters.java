/**
 * 
 */
package com.meidusa.venus.convert;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Sun Ning
 * @since 2010-3-25
 */
public class Converters {

    /**
	 * 
	 */
    private Map<Type, Converter> convertDict = new HashMap<Type, Converter>();

    private Converter defaultConverter;

    /**
     * 
     * @param type
     * @param c
     */
    public void register(Type type, Converter c) {
        synchronized (convertDict) {
            convertDict.put(type, c);
        }
    }

    public Converter getConverter(Type type) {
        if (convertDict.containsKey(type)) {
            return convertDict.get(type);
        } else {
            return getDefaultConverter();
            // throw new UnsupportedOperationException(
            // "Does not support conversion between "
            // + type.getSimpleName());
        }
    }

    /**
     * @return the defaultConverter
     */
    public Converter getDefaultConverter() {
        return defaultConverter;
    }

    /**
     * @param defaultConverter the defaultConverter to set
     */
    public void setDefaultConverter(Converter defaultConverter) {
        this.defaultConverter = defaultConverter;
    }

}
