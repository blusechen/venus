package com.meidusa.venus.backend.services.xml;

import com.meidusa.toolkit.common.bean.BeanContext;
import com.meidusa.toolkit.common.bean.config.ConfigUtil;
import org.apache.commons.beanutils.BeanUtilsBean;
import org.apache.commons.beanutils.ConvertUtilsBean;
import org.apache.commons.beanutils.PropertyUtilsBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;

/**
 * Created by GodzillaHua on 7/3/16.
 */
public class BackendBeanUtilsBean extends BeanUtilsBean {

    private static Logger logger = LoggerFactory.getLogger(BackendBeanUtilsBean.class);

    private BeanContext beanContext;

    public BackendBeanUtilsBean(ConvertUtilsBean convertUtilsBean, PropertyUtilsBean propertyUtilsBean, BeanContext beanContext) {
        super(convertUtilsBean, propertyUtilsBean);
        this.beanContext = beanContext;
    }

    public void setProperty(Object bean, String name, Object value) throws IllegalAccessException, InvocationTargetException {
        if (value instanceof String) {
            PropertyDescriptor descriptor = null;
            try {
                descriptor = getPropertyUtils().getPropertyDescriptor(bean, name);
                if (descriptor == null) {
                    return; // Skip this property setter
                } else {
                    if (descriptor.getPropertyType().isEnum()) {
                        Class<Enum> clazz = (Class<Enum>) descriptor.getPropertyType();
                        value = Enum.valueOf(clazz, (String) value);
                    } else {
                        Object temp = null;
                        try {
                            temp = ConfigUtil.filter((String) value, beanContext);
                        } catch (Exception e) {
                            logger.error("config filter " + value + " error", e);
                        }
                        if (temp == null) {
                            temp = ConfigUtil.filter((String) value);
                        }
                        value = temp;
                    }
                }
            } catch (NoSuchMethodException e) {
                return; // Skip this property setter
            }
        }
        super.setProperty(bean, name, value);
    }
}
