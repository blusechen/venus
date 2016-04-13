package com.meidusa.venus.validate.context;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ValidationContext hold infomations about validation state, like errors.
 * 
 * @author lichencheng.daisy
 * @since 1.0.0-SNAPSHOT
 * 
 */
public class ValidationContext {
    private static Logger logger = LoggerFactory.getLogger(ValidationContext.class);
    public static Locale defaultLocale = Locale.ENGLISH;

    private static ThreadLocal<ValidationContext> context = new ThreadLocal<ValidationContext>();

    public static ValidationContext getContext() {
        ValidationContext currentContext = context.get();
        if (currentContext == null) {
            currentContext = new ValidationContext();
            context.set(currentContext);
        }
        return currentContext;

    }

    private Locale locale;
    private List<String> validationErrors;
    private Map<String, List<String>> fieldValidationErrors;

    public Locale getLocale() {
        return locale;
    }

    public void setLocale(Locale locale) {
        this.locale = locale;
    }

    public void addValidationError(String message) {
        logger.error("Validation error: " + message);
        if (this.validationErrors == null) {
            validationErrors = new LinkedList<String>();
        }
        validationErrors.add(message);

    }

    public void addFieldValidationError(String propertyName, String message) {
        logger.error("Validation error for " + propertyName + ":" + message);
        if (this.fieldValidationErrors == null) {
            fieldValidationErrors = new HashMap<String, List<String>>();
        }
        List<String> messageList = fieldValidationErrors.get(propertyName);
        if (messageList == null) {
            messageList = new LinkedList<String>();
            messageList.add(message);
            fieldValidationErrors.put(propertyName, messageList);
        }

    }

    public List<String> getValidationErrors() {
        return validationErrors;
    }

    public Map<String, List<String>> getFieldValidationErrors() {
        return fieldValidationErrors;
    }

}
