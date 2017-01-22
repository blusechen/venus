package com.meidusa.venus.exception;

import com.meidusa.venus.annotations.RemoteException;
import com.meidusa.venus.annotations.RemoteException.Level;

@RemoteException(errorCode=VenusExceptionCodeConstant.SERVICE_NO_CALLBACK_EXCEPTION,level=Level.ERROR)
public class ServiceNotCallbackException extends AbstractVenusException {
    private static final long serialVersionUID = 1L;

    public ServiceNotCallbackException(String message) {
        super(message);
    }

    @Override
    public int getErrorCode() {
        return VenusExceptionCodeConstant.SERVICE_NO_CALLBACK_EXCEPTION;
    }

}
