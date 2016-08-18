/**
 * 
 */
package com.meidusa.venus.backend;

import com.meidusa.venus.exception.CodedException;
import com.meidusa.venus.exception.VenusExceptionCodeConstant;
import com.meidusa.venus.exception.VenusExceptionFactory;

public class Response {

    private Object result;

    private int errorCode;
    private String errorMessage;

    private Exception exception;

    public Exception getException() {
        return exception;
    }

    public void setException(Exception exception) {
        this.exception = exception;
    }

    /**
     * @return the result
     */
    public Object getResult() {
        return result;
    }

    /**
     * @param result the result to set
     */
    public void setResult(Object result) {
        this.result = result;
    }

    /**
     * @return the status
     */
    public int getErrorCode() {
        return errorCode;
    }

    /**
     * @param status the status to set
     */
    public void setErrorCode(int status) {
        this.errorCode = status;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public void setError(Throwable cause, VenusExceptionFactory venusExceptionFactory){
        if (cause instanceof CodedException) {
            setErrorCode(((CodedException) cause).getErrorCode());
            setErrorMessage(cause.getMessage());
        } else {
            int errorCode = 0;
            if (venusExceptionFactory != null) {
                errorCode = venusExceptionFactory.getErrorCode(cause.getClass());
                if (errorCode != 0) {
                    setErrorCode(errorCode);
                } else {
                    // unknowable exception
                    setErrorCode(VenusExceptionCodeConstant.UNKNOW_EXCEPTION);
                }
            } else {
                // unknowable exception
                setErrorCode(VenusExceptionCodeConstant.UNKNOW_EXCEPTION);
            }

            if (cause instanceof NullPointerException && cause.getMessage() == null) {
                setErrorMessage("Server Side error caused by NullPointerException");
            } else {
                setErrorMessage(cause.getMessage());
            }
        }
    }

}
