/**
 * 
 */
package com.meidusa.venus.exception;

import com.meidusa.venus.annotations.RemoteException;

/**
 * @author Sun Ning
 * 
 */
@RemoteException(errorCode=VenusExceptionCodeConstant.PARAMETER_OMITTED_EXCEPTION)
public class SystemParameterRequiredException extends AbstractVenusException {
    private static final long serialVersionUID = 1L;

    public SystemParameterRequiredException(String msg) {
        super("SystemParameterRequiredException:" + msg);
    }

    @Override
    public int getErrorCode() {
        return VenusExceptionCodeConstant.PARAMETER_OMITTED_EXCEPTION;
    }

}
