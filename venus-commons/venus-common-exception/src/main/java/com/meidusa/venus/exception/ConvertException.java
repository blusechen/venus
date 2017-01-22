/**
 * 
 */
package com.meidusa.venus.exception;

import com.meidusa.venus.annotations.RemoteException;
import com.meidusa.venus.annotations.RemoteException.Level;

/**
 * 
 * 
 * @author Sun Ning
 * @since 2010-3-4
 */
@RemoteException(errorCode=VenusExceptionCodeConstant.PARAMETER_CONVERT_EXCEPTION,level=Level.ERROR)
public class ConvertException extends AbstractVenusException {
    private static final long serialVersionUID = -1583661559860123414L;

    public ConvertException(String msg) {
        super("ConverterException:" + msg);
    }

    public ConvertException(String msg, Throwable e) {
        super(msg, e);
    }

    public ConvertException() {

    }

    @Override
    public int getErrorCode() {
        return VenusExceptionCodeConstant.PARAMETER_CONVERT_EXCEPTION;
    }
}
