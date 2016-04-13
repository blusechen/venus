/**
 * 
 */
package com.meidusa.venus.convert;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.BeanUtilsBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.meidusa.toolkit.common.bean.PureJavaReflectionProvider;
import com.meidusa.toolkit.common.bean.ReflectionProvider;
import com.meidusa.venus.exception.ConvertException;

/**
 * @author Sun Ning
 * 
 * 
 * 
 */
public class CommonsBeanUtilConverter implements Converter {
    private ReflectionProvider reflectionProvider = PureJavaReflectionProvider.getInstance();
    private static final Logger logger = LoggerFactory.getLogger(CommonsBeanUtilConverter.class);

    @SuppressWarnings("unchecked")
    @Override
    public Object convert(Object value, Type type) throws ConvertException {
        Object object = null;
        try {
            if (type instanceof Class) {
                Class clazz = (Class) type;
                if (value instanceof Map && !clazz.isPrimitive()) {
                    object = new HashMap();
                    BeanUtils.populate(object, (Map) (value));
                }
                return BeanUtilsBean.getInstance().getConvertUtils().convert(value, clazz);
            } else if (type instanceof ParameterizedType) {
                ParameterizedType parameterizedType = (ParameterizedType) type;
                Class rawType = ((Class) parameterizedType.getRawType());
                if (Map.class.isAssignableFrom(rawType)) {
                    object = new HashMap();
                } else if (List.class.isAssignableFrom(rawType)) {
                    object = new LinkedList();
                } else if (rawType.isAssignableFrom(value.getClass()) && rawType.isInterface()) {
                    return value;
                } else {
                    object = reflectionProvider.newInstance(rawType);
                }
                if (value instanceof Map) {
                    BeanUtils.populate(object, (Map) (value));
                } else {
                    BeanUtils.copyProperties(object, value);
                }
            }
            return object;
        } catch (Exception e) {
            throw new ConvertException(value + " can not convert to " + type);
        }
    }

    @Override
    public Object convert(String name, Object value, Type type, Map<String, Object> context) {
        String beanName = name.split("\\.")[0];
        String propertyName = name.split("\\.")[1];

        if (!context.containsKey(beanName)) {
            Object object = null;
            try {
                if (type instanceof Class) {
                    Class clazz = (Class) type;
                    if (value instanceof Map && !clazz.isPrimitive()) {
                        object = reflectionProvider.newInstance(clazz);
                    }
                } else if (type instanceof ParameterizedType) {
                    ParameterizedType parameterizedType = (ParameterizedType) type;
                    object = reflectionProvider.newInstance(((Class) parameterizedType.getRawType()));
                }
            } catch (Exception e) {
                throw new ConvertException(value + " can not convert to " + type);
            }

            context.put(beanName, object);
        }

        Object bean = context.get(beanName);
        try {
            // BeanUtils.setProperty(bean, propertyName, this.convert(value,
            // bean.getClass().getField(propertyName).getClass()));
            BeanUtils.setProperty(bean, propertyName, value);
        } catch (IllegalAccessException e) {
            if (logger.isDebugEnabled()) {
                logger.debug(e.getMessage(), e);
            }
        } catch (InvocationTargetException e) {
            if (logger.isDebugEnabled()) {
                logger.debug(e.getMessage(), e);
            }
        } catch (SecurityException e) {
            if (logger.isDebugEnabled()) {
                logger.debug(e.getMessage(), e);
            }
            // } catch (NoSuchFieldException e) {
            // logger.debug(e);
        }
        return bean;
    }

    @Override
    public Object convert(Object value, Type type, String helper) throws ConvertException {
        return convert(value, type);
    }

    /**
     * huangcheng： 如果使用默认的构造函数，转换异常，将使用默认值 这里所有的转换异常将抛出
     * 
     */
    public CommonsBeanUtilConverter() {
        /*
         * ConvertUtilsBean b = new ConvertUtilsBean(); b.register(true, true, 0); BeanUtilsBean cb = new
         * BeanUtilsBean(b); BeanUtilsBean.setInstance(cb);
         */
    }
}
