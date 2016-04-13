package com.meidusa.venus.validate.exception;

/**
 * If validation failed a ValidationException with message will be thrown out.
 * 
 * @author lichencheng
 * 
 */
public class ValidationException extends Exception {

    /**
	 * 
	 */
    private static final long serialVersionUID = 1L;

    public ValidationException() {

    }

    public ValidationException(String s) {
        super(s);
    }

    public ValidationException(String s, Throwable cause) {
        super(s, cause);
    }

}
