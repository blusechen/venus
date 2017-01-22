package com.meidusa.venus.exception;

import com.meidusa.venus.annotations.RemoteException;
import com.meidusa.venus.annotations.RemoteException.Level;

@RemoteException(errorCode=VenusExceptionCodeConstant.SERVICE_DEFINITION_EXCEPTION,level=Level.ERROR)
public class ServiceDefinitionException extends AbstractVenusException {
    private static final long serialVersionUID = 1L;

    public ServiceDefinitionException(String msg) {
        super(msg);
    }

    @Override
    public int getErrorCode() {
        return VenusExceptionCodeConstant.SERVICE_DEFINITION_EXCEPTION;
    }
}
