/**
 * 
 */
package com.meidusa.venus.convert;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.InitializingBean;

import com.meidusa.venus.exception.ConvertException;

/**
 * 
 * 
 * @author Sun Ning
 * @since 2010-3-5
 */
public class DefaultConvertService implements ConvertService, InitializingBean {

    private Converters converters = new Converters();

    public DefaultConvertService() {
        this.setUpConvert();
    }

    @Override
    public Map<String, Object> convert(Map<String, Object> raw, Map<String, Type> typeDict) throws ConvertException {
        Map<String, Object> context = new HashMap<String, Object>();

       Set<Map.Entry<String,Object>> entries = raw.entrySet();
        for (Map.Entry<String,Object> entry : entries) {
            String key = entry.getKey();
            Object obj = entry.getValue();
            if (isComplexType(key)) {
                String beanName = getBeanName(key);
                handleComplexType(key, obj, typeDict.get(beanName), context);
            } else {
                handleTrivialType(key, raw, typeDict.get(key), context);
            }
        }

        return context;
    }

    /**
     * complex type, for example: bean.prop=2
     * 
     * @param key
     * @param strValue
     * @param type
     * @param context
     * @throws ConvertException
     */
    private void handleComplexType(String key, Object strValue, Type type, Map<String, Object> context) throws ConvertException {
        // to prevent null pointer
        if (type != null) {
            Converter converter = converters.getConverter(type);
            Object value = converter.convert(key, strValue, type, context);
            context.put(getObjectKey(key), value);
        }
    }

    /**
     * 
     * @param key
     * @param raw
     * @param type
     * @param context
     * @throws ConvertException
     */
    private void handleTrivialType(String key, Map<String, Object> raw, Type type, Map<String, Object> context) throws ConvertException {
        // to prevent null pointer
        if (type != null) {
            Object value;
            // special handle for String[].class
            if (type.equals(String[].class)) {
                value = converters.getConverter(String[].class).convert(raw.get(key), type, null);
            } else {
                value = converters.getConverter(type).convert(raw.get(key), type);
            }
            context.put(key, value);
        }
    }

    private String getObjectKey(String key) {
        return key.split("\\.")[0];
    }

    @Override
    public boolean isComplexType(String key) {
        return key.contains(".");
    }

    @Override
    public String getBeanName(String key) {
        return getObjectKey(key);
    }

    public void setUpConvert() {
        Converter beanUtilConverter = new CommonsBeanUtilConverter();

        converters.register(Integer.TYPE, beanUtilConverter);
        converters.register(Integer.class, beanUtilConverter);
        converters.register(Integer[].class, beanUtilConverter);
        converters.register(int[].class, beanUtilConverter);
        converters.register(int.class, beanUtilConverter);

        converters.register(Short.TYPE, beanUtilConverter);
        converters.register(Short.class, beanUtilConverter);
        converters.register(Short[].class, beanUtilConverter);
        converters.register(short[].class, beanUtilConverter);
        converters.register(short.class, beanUtilConverter);

        converters.register(Long.TYPE, beanUtilConverter);
        converters.register(Long.class, beanUtilConverter);
        converters.register(Long[].class, beanUtilConverter);
        converters.register(long[].class, beanUtilConverter);
        converters.register(long.class, beanUtilConverter);

        converters.register(Float.class, beanUtilConverter);
        converters.register(Float.TYPE, beanUtilConverter);
        converters.register(Float[].class, beanUtilConverter);
        converters.register(float[].class, beanUtilConverter);
        converters.register(float.class, beanUtilConverter);

        converters.register(Double.TYPE, beanUtilConverter);
        converters.register(Double.class, beanUtilConverter);
        converters.register(Double[].class, beanUtilConverter);
        converters.register(double[].class, beanUtilConverter);
        converters.register(double.class, beanUtilConverter);

        converters.register(Byte.TYPE, beanUtilConverter);
        converters.register(Byte.class, beanUtilConverter);
        converters.register(Byte[].class, beanUtilConverter);
        converters.register(byte[].class, beanUtilConverter);
        converters.register(byte.class, beanUtilConverter);

        converters.register(Boolean.TYPE, beanUtilConverter);
        converters.register(Boolean.class, beanUtilConverter);
        converters.register(Boolean[].class, beanUtilConverter);
        converters.register(boolean[].class, beanUtilConverter);
        converters.register(boolean.class, beanUtilConverter);

        converters.register(Character.TYPE, beanUtilConverter);
        converters.register(Character.class, beanUtilConverter);
        converters.register(Character[].class, beanUtilConverter);
        converters.register(char[].class, beanUtilConverter);
        converters.register(char.class, beanUtilConverter);

        converters.register(BigDecimal.class, beanUtilConverter);
        converters.register(BigDecimal[].class, beanUtilConverter);

        converters.register(String.class, beanUtilConverter);
        converters.register(String[].class, new StringArrayConverter());

        converters.register(Date.class, new DateConverter());

        converters.setDefaultConverter(beanUtilConverter);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        setUpConvert();
    }

    @Override
    public Object convert(String value, Type type) throws ConvertException {
        if (converters.getConverter(type) == null) {
            throw new UnsupportedOperationException("No convert found on type " + type);
        }
        return converters.getConverter(type).convert(value, type);
    }
}
