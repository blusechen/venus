package com.meidusa.venus.validate.holder;

import ognl.OgnlException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.meidusa.venus.util.OgnlUtil;
import com.meidusa.venus.validate.exception.ValidationRuntimeException;

/**
 * A ValueHolder implemention which support
 * 
 * @author lichencheng.daisy
 * @since 1.0.0-SNAPSHOT
 */
public class OgnlBasicValueHolder implements ValueHolder {
    private static Logger logger = LoggerFactory.getLogger(OgnlBasicValueHolder.class);
    private Object root;

    public OgnlBasicValueHolder() {
        if (logger.isDebugEnabled()) {
            logger.debug("OgnlBasicValueHolder created.");
        }
    }

    public OgnlBasicValueHolder(Object root) {
        if (logger.isDebugEnabled()) {
            logger.debug("OgnlBasicValueHolder created with root " + root + " .");
        }
        this.root = root;
    }

    @Override
    public Object getRoot() {
        return root;
    }

    @Override
    public void setRoot(Object root) {
        this.root = root;
    }

    @Override
    public void setValue(String expr, Object value) {
        try {
            OgnlUtil.setValue(root, expr, value);
        } catch (OgnlException e) {
            logger.error("Can not set value:(" + value + ") for " + this.root + " .");
            throw new ValidationRuntimeException("Expression \"" + expr + "=" + value + "\"can't be set on value " + root + " .", e);
        }

    }

    @Override
    public String findString(String expr) {
        try {
            return OgnlUtil.findString(root, expr);
        } catch (OgnlException e) {
            logger.error("Expression \"" + expr + "\"can't be found on value " + root + " as string.");
            throw new ValidationRuntimeException("Expression \"" + expr + "\"can't be found on value " + root + " as string.", e);
        }
    }

    @Override
    public Object findValue(String expr) {
        try {
            return OgnlUtil.findValue(root, expr);
        } catch (OgnlException e) {
            logger.error("Expression \"" + expr + "\"can't be found on value " + root + " .");
            throw new ValidationRuntimeException("Expression \"" + expr + "\"can't be found on value " + root + " .", e);
        }
    }

    @Override
    public Object findValue(String expr, Class<?> asType) {
        try {
            return OgnlUtil.findValue(root, expr, asType);
        } catch (OgnlException e) {
            logger.error("Expression \"" + expr + "\"can't be found on value " + root + " using class " + asType.getCanonicalName() + " .");
            throw new ValidationRuntimeException("Expression \"" + expr + "\"can't be found on value " + root + " using class " + asType.getCanonicalName()
                    + " .", e);
        }
    }

}
