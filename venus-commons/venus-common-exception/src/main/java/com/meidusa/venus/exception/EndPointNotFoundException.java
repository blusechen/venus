/**
 * 
 */
package com.meidusa.venus.exception;

import com.meidusa.venus.annotations.RemoteException;

/**
 * thrown when end point not found
 * 
 */
@RemoteException(errorCode=VenusExceptionCodeConstant.ENDPOINT_NOT_FOUND)
public class EndPointNotFoundException extends AbstractVenusException {
    private static final long serialVersionUID = 1L;

    public EndPointNotFoundException(String msg) {
        super("EndPointNotFoundException:" + msg);
    }

    @Override
    public int getErrorCode() {
        return VenusExceptionCodeConstant.ENDPOINT_NOT_FOUND;
    }
}
