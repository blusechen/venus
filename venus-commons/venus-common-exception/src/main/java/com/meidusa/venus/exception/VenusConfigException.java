package com.meidusa.venus.exception;

import com.meidusa.venus.annotations.RemoteException;
import com.meidusa.venus.annotations.RemoteException.Level;

@RemoteException(errorCode=VenusExceptionCodeConstant.VENUS_CONFIG_EXCEPTION,level=Level.ERROR)
public class VenusConfigException extends AbstractVenusException implements VenusExceptionCodeConstant {
    private static final long serialVersionUID = 1L;

    public VenusConfigException(String msg) {
        super(msg);
    }

    @Override
    public int getErrorCode() {
        return VENUS_CONFIG_EXCEPTION;
    }

}
