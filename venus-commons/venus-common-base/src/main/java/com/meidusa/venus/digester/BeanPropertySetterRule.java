package com.meidusa.venus.digester;

import java.beans.PropertyDescriptor;

import org.apache.commons.beanutils.DynaBean;
import org.apache.commons.beanutils.DynaProperty;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.collections.ArrayStack;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xml.sax.Attributes;

import com.meidusa.toolkit.common.util.StringUtil;
import com.meidusa.venus.util.VenusBeanUtilsBean;

/**
 * 
 * @author Struct
 * 
 */
public class BeanPropertySetterRule extends org.apache.commons.digester.BeanPropertySetterRule {
    protected static Log log = LogFactory.getLog("org.apache.commons.digester.Digester");
    protected static ThreadLocal<ArrayStack> threadLocal = new ThreadLocal<ArrayStack>() {
        protected ArrayStack initialValue() {
            return new ArrayStack();
        }
    };

    private String attrname;
    private String attrvalue;

    public BeanPropertySetterRule() {
    }

    public BeanPropertySetterRule(String propertyName) {
        super(propertyName);
    }

    public BeanPropertySetterRule(String propertyName, String attrname) {
        super(propertyName);
        this.attrname = attrname;
    }

    public void begin(String namespace, String name, Attributes attributes) throws Exception {
        if (attrname != null) {
            attrvalue = attributes.getValue(attrname);
        }
        ArrayStack stack = threadLocal.get();
        stack.push(attrvalue);
        super.begin(namespace, name, attributes);
    }

    public void end(String namespace, String name) throws Exception {

        super.propertyName = attrvalue;

        end0(namespace, name);
        ArrayStack stack = threadLocal.get();
        stack.pop();
    }

    private void end0(String namespace, String name) throws Exception {

        String property = propertyName;
        boolean isBasic = false;

        if (property == null) {
            // If we don't have a specific property name,
            // use the element name.
            property = name;
        }

        // Get a reference to the top object
        Object top = digester.peek();

        // log some debugging information
        if (log.isDebugEnabled()) {
            log.debug("[BeanPropertySetterRule]{" + digester.getMatch() + "} Set " + top.getClass().getName() + " property " + property + " with text "
                    + bodyText);
        }

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
            Class type = desc.getPropertyType();
            isBasic = isBasicType(type);
        }
        if (isBasic) {
            // Set the property (with conversion as necessary)
            VenusBeanUtilsBean.getInstance().setProperty(top, property, bodyText);
        } else {
            if (!StringUtil.isEmpty(bodyText)) {
                VenusBeanUtilsBean.getInstance().setProperty(top, property, bodyText);
            }
        }

    }

    private Boolean isBasicType(Class clazz) {
        if (clazz.isPrimitive() || Number.class.isAssignableFrom(clazz) || String.class == clazz) {
            return true;
        }
        return false;

    }
}
