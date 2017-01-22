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
@RemoteException(errorCode=VenusExceptionCodeConstant.SERVICE_INACTIVE_EXCEPTION,level=Level.ERROR)
public class ServiceInactiveException extends AbstractVenusException {
    private static final long serialVersionUID = 1L;

    public ServiceInactiveException(String msg) {
        super("ServiceUnavailableException:" + msg);
    }

    @Override
    public int getErrorCode() {
        return VenusExceptionCodeConstant.SERVICE_INACTIVE_EXCEPTION;
    }

}
