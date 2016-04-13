/**
 * 
 */
package com.meidusa.venus.exception.wrapper;

/**
 * @author Sun Ning
 * 
 */
public interface HttpStatusCodeAwareExceptionWrapperFactory {

    public HttpStatusCodeAwareExceptionWrapper getExceptionWrapper(int status, Throwable e);

}
