/**
 * 
 */
package com.meidusa.venus.exception;

import com.meidusa.venus.annotations.RemoteException;
import com.meidusa.venus.annotations.RemoteException.Level;

/**
 * thrown when service not available.
 * 
 * @author Sun Ning
 * @since 2010-3-16
 */
@RemoteException(errorCode=VenusExceptionCodeConstant.SERVICE_UNAVAILABLE_EXCEPTION,level=Level.ERROR)
public class ServiceUnavailableException extends AbstractVenusException {
    private static final long serialVersionUID = 1L;

    public ServiceUnavailableException(String msg) {
        super("ServiceUnavailableException:" + msg);
    }

    @Override
    public int getErrorCode() {
        return VenusExceptionCodeConstant.SERVICE_UNAVAILABLE_EXCEPTION;
    }
}
