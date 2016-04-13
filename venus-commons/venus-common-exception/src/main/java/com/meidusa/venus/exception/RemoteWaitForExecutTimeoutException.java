package com.meidusa.venus.exception;

import com.meidusa.venus.annotations.RemoteException;

@RemoteException(errorCode=VenusExceptionCodeConstant.INVOCATION_ABORT_WAIT_TIMEOUT)
public class RemoteWaitForExecutTimeoutException extends AbstractVenusException {
	private static final long serialVersionUID = 1L;

	public RemoteWaitForExecutTimeoutException(String message){
		super(message);
	}
	
    @Override
    public int getErrorCode() {
        return VenusExceptionCodeConstant.INVOCATION_ABORT_WAIT_TIMEOUT;
    }

}
