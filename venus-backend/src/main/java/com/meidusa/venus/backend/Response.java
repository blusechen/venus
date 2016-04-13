/**
 * 
 */
package com.meidusa.venus.backend;

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

}
