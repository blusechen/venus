/**
 * 
 */
package com.meidusa.venus.exception;

public class ServiceInvokeException extends RuntimeException {
    private static final long serialVersionUID = 5608272032200117814L;

    private Throwable targetException;

    public Throwable getTargetException() {
        return targetException;
    }

    public ServiceInvokeException(Throwable throwable) {
        super(throwable);
        this.targetException = throwable;
    }
}
