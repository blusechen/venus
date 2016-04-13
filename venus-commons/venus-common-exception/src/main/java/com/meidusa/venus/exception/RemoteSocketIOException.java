package com.meidusa.venus.exception;

import com.meidusa.venus.annotations.RemoteException;

@RemoteException(errorCode=VenusExceptionCodeConstant.REMOTE_SOCKET_IO_EXCEPTION)
public class RemoteSocketIOException extends AbstractVenusException {
	private static final long serialVersionUID = 1L;

	public RemoteSocketIOException(String message){
		super(message);
	}
	
	public RemoteSocketIOException(String message,Throwable e){
		super(message,e);
	}
	
	
    @Override
    public int getErrorCode() {
        return VenusExceptionCodeConstant.REMOTE_SOCKET_IO_EXCEPTION;
    }

}
