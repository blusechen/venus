package com.meidusa.venus.exception;

import com.meidusa.venus.annotations.RemoteException;

@RemoteException(errorCode=VenusExceptionCodeConstant.AUTHEN_EXCEPTION)
public class AuthenticateException extends AbstractVenusException {
	private static final long serialVersionUID = 1L;

	public AuthenticateException(String msg) {
        super("authenticate error:" + msg);
    }

    @Override
    public int getErrorCode() {
        return VenusExceptionCodeConstant.AUTHEN_EXCEPTION;
    }

}
