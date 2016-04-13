/**
 * 
 */
package com.meidusa.venus.backend.view;

/**
 * Wraps exception
 * 
 * @author Sun Ning
 * @since 2010-3-8
 */
@Deprecated
public class ExceptionWrapper {

    private int httpCode;

    private String msg;

    public ExceptionWrapper() {
    }

    public ExceptionWrapper(int httpCode, Throwable e) {
        this.httpCode = httpCode;
        if (e != null) {
            this.msg = e.getMessage();
        }

    }

    public ExceptionWrapper(int httpCode, String msg) {
        this.httpCode = httpCode;
        this.msg = msg;
    }

    /**
     * @return the msg
     */
    public String getMsg() {
        return msg;
    }

    /**
     * @param msg the msg to set
     */
    public void setMsg(String msg) {
        this.msg = msg;
    }

    /**
     * @return the httpCode
     */
    public int getHttpCode() {
        return httpCode;
    }

    /**
     * @param httpCode the httpCode to set
     */
    public void setHttpCode(int httpCode) {
        this.httpCode = httpCode;
    }

}
