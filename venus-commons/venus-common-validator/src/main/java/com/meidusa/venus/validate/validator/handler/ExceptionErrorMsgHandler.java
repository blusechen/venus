package com.meidusa.venus.validate.validator.handler;

import com.meidusa.venus.validate.exception.FieldValidationException;
import com.meidusa.venus.validate.exception.ValidationException;

public class ExceptionErrorMsgHandler implements ErrorMsgHandler {

    @Override
    public void handleValidationFieldError(String field, String msg) throws ValidationException {
        throw new FieldValidationException(field, msg);

    }

    @Override
    public void handleValidationError(String msg) throws ValidationException {
        throw new ValidationException(msg);

    }

}
