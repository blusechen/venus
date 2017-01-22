package com.meidusa.venus.exception;

import com.meidusa.venus.annotations.RemoteException;
import com.meidusa.venus.annotations.RemoteException.Level;

@RemoteException(errorCode=VenusExceptionCodeConstant.UNKNOW_EXCEPTION,level=Level.ERROR)
public class DefaultVenusException extends AbstractVenusException {
    private static final long serialVersionUID = 1L;
    private int errorCode;

    public DefaultVenusException(int errorCode, String msg) {
        super(msg);
        this.errorCode = errorCode;
    }

    public DefaultVenusException(String msg, Throwable throwable) {
        super(msg, throwable);
    }

    public DefaultVenusException(int errorCode, String msg, Throwable throwable) {
        super(msg, throwable);
        this.errorCode = errorCode;
    }

    public String getMessage(){
    	return "errorCode="+this.getErrorCode()+", message="+super.getMessage();
    }
    
    @Override
    public int getErrorCode() {
        return errorCode;
    }
}
