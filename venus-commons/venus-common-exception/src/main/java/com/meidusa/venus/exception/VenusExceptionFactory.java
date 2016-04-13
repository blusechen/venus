package com.meidusa.venus.exception;

public interface VenusExceptionFactory {
    Exception getException(int errcode, String message);

    void addException(Class<? extends CodedException> clazz);

    int getErrorCode(Class<? extends Throwable> clazz);
}
