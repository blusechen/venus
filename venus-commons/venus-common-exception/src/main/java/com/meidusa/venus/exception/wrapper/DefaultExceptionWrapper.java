/**
 * 
 */
package com.meidusa.venus.exception.wrapper;

/**
 * @author sunning
 * 
 */
public class DefaultExceptionWrapper implements HttpStatusCodeAwareExceptionWrapper {

    private String msg = "";
    private int status;
    private int code;

    // private int return_code; //apipool瑕佹眰
    // private String return_message="";

    public DefaultExceptionWrapper() {
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public void setStatus(int httpStatus) {
        this.status = httpStatus;
    }

    @Override
    public int getStatus() {
        return status;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
        // this.return_code = code;
    }

    public Integer getReturn_code() {
        return code;
    }

    public String getReturn_message() {
        if (msg == null) {
            return "";
        } else {
            return msg;
        }
    }

}
