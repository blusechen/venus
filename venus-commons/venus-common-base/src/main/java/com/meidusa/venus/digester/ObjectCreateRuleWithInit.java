package com.meidusa.venus.digester;

import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.DynaBean;
import org.apache.commons.beanutils.DynaProperty;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.collections.ArrayStack;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xml.sax.Attributes;

import com.meidusa.toolkit.common.bean.BeanContextBean;
import com.meidusa.toolkit.common.bean.config.ConfigUtil;
import com.meidusa.toolkit.common.bean.util.Initialisable;
import com.meidusa.toolkit.common.bean.util.InitialisationException;

public class ObjectCreateRuleWithInit extends org.apache.commons.digester.ObjectCreateRule {
    protected static Log log = LogFactory.getLog("org.apache.commons.digester.Digester");

    public ObjectCreateRuleWithInit(Class clazz) {
        super(clazz);
    }

    public ObjectCreateRuleWithInit(String className) {
        super(className);
    }

    public ObjectCreateRuleWithInit(String className, String attrName) {
        super(className, attrName);
    }

    public void begin(Attributes attributes) throws Exception {

        // Identify the name of the class to instantiate
        String realClassName = className;
        if (attributeName != null) {
            String value = attributes.getValue(attributeName);
            if (value != null) {
                realClassName = value;
            }
        }
        if (log.isDebugEnabled()) {
            log.debug("[ObjectCreateRule]{" + digester.getMatch() + "}New " + realClassName);
        }
        Object object = null;
        realClassName = realClassName.trim();
        // Instantiate the new object and push it on the context stack
        if (realClassName.startsWith("${") && realClassName.endsWith("}")) {
            object = ConfigUtil.filter(realClassName);
        } else {
            Class clazz = digester.getClassLoader().loadClass(realClassName);
            object = BeanContextBean.getInstance().createBean(clazz);
            if (object == null) {
                object = clazz.newInstance();
            }
        }
        digester.push(object);
    }

    /**
     * Process the end of this element.
     */
    public void end() throws Exception {
        int size = this.digester.getCount();
        if (size >= 2) {
            Object top = this.digester.peek(1);
            Object object = this.digester.peek();
            ArrayStack stack = (ArrayStack) BeanPropertySetterRule.threadLocal.get();
            if (stack.size() > 0) {
                String property = (String) stack.peek();
                // Force an exception if the property does not exist
                // (BeanUtils.setProperty() silently returns in this case)
                if (top instanceof DynaBean) {
                    DynaProperty desc = ((DynaBean) top).getDynaClass().getDynaProperty(property);
                    if (desc == null) {
                        throw new NoSuchMethodException("Bean has no property named " + property);
                    }
                } else /* this is a standard JavaBean */{
                    PropertyDescriptor desc = PropertyUtils.getPropertyDescriptor(top, property);
                    if (desc == null) {
                        throw new NoSuchMethodException("Bean has no property named " + property);
                    }
                }

                // Set the property (with conversion as necessary)
                BeanUtils.setProperty(top, property, object);
            }

            if (object instanceof Initialisable) {
                Initialisable initialisable = (Initialisable) object;
                try {
                    initialisable.init();
                } catch (InitialisationException e) {
                    throw new InvocationTargetException(e);
                }
            }
        }

        super.end();
    }

}
