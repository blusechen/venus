package com.meidusa.venus.validate.validator.handler;

import com.meidusa.venus.validate.exception.ValidationException;

/**
 * If thereis something wrong with validation ErrorHandler will handle the error message.
 * 
 * @author lichencheng.daisy
 * 
 */
public interface ErrorMsgHandler {
    public void handleValidationFieldError(String field, String msg) throws ValidationException;

    public void handleValidationError(String msg) throws ValidationException;
}
