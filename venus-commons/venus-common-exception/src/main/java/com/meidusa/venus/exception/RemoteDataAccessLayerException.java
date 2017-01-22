package com.meidusa.venus.exception;

import com.meidusa.venus.annotations.RemoteException;
import com.meidusa.venus.annotations.RemoteException.Level;

/**
 * 
 * @author structchen
 *
 */
@RemoteException(errorCode=VenusExceptionCodeConstant.DAL_LAYER_ACCESS_EXCEPTION,level=Level.ERROR)
public class RemoteDataAccessLayerException extends AbstractVenusException {
    private static final long serialVersionUID = 1L;

    public RemoteDataAccessLayerException(String msg) {
        super(msg);
    }

    public ExceptionLevel getLevel() {
        return ExceptionLevel.INFO;
    }

    @Override
    public int getErrorCode() {
        return VenusExceptionCodeConstant.DAL_LAYER_ACCESS_EXCEPTION;
    }
}
