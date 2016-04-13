package com.meidusa.venus.exception;

public abstract class AbstractVenusException extends RuntimeException implements CodedException, VenusExceptionLevel {
    private static final long serialVersionUID = 1L;

    public AbstractVenusException(String msg) {
        super(msg);
    }

    public AbstractVenusException() {
    }

    public AbstractVenusException(Throwable throwable) {
        super(throwable);
    }

    public AbstractVenusException(String msg, Throwable throwable) {
        super(msg, throwable);
    }

    public ExceptionLevel getLevel() {
        return ExceptionLevel.ERROR;
    }

    public abstract int getErrorCode();

}
