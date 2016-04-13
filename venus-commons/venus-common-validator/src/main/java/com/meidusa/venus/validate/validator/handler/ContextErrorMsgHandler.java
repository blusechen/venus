package com.meidusa.venus.validate.validator.handler;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.meidusa.venus.validate.context.ValidationContext;
import com.meidusa.venus.validate.exception.ValidationException;

public class ContextErrorMsgHandler implements ErrorMsgHandler {
    private static Logger logger = LoggerFactory.getLogger(ValidationContext.class);
    public static Locale defaultLocale = Locale.ENGLISH;

    private static ThreadLocal<ContextErrorMsgHandler> context = new ThreadLocal<ContextErrorMsgHandler>();

    public static ContextErrorMsgHandler getContextErrorMsgHandler() {
        ContextErrorMsgHandler currentContext = context.get();
        if (currentContext == null) {
            currentContext = new ContextErrorMsgHandler();
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

    @Override
    public void handleValidationError(String message) throws ValidationException {
        logger.error("Validation error: " + message);
        if (this.validationErrors == null) {
            validationErrors = new LinkedList<String>();
        }
        validationErrors.add(message);

    }

    @Override
    public void handleValidationFieldError(String propertyName, String message) throws ValidationException {
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
