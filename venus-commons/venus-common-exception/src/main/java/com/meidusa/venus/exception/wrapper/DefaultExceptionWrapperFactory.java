/**
 * 
 */
package com.meidusa.venus.exception.wrapper;

/**
 * @author Sun Ning
 * 
 */
public class DefaultExceptionWrapperFactory implements HttpStatusCodeAwareExceptionWrapperFactory {

    /*
     * (non-Javadoc)
     * @seecom.meidusa.commons.servicegate.exception.wrapper.
     * HttpStatusCodeAwareExceptionWrapperFactory#getExceptionWrapper()
     */
    @Override
    public HttpStatusCodeAwareExceptionWrapper getExceptionWrapper(int status, Throwable e) {
        DefaultExceptionWrapper wrapper = new DefaultExceptionWrapper();
        wrapper.setMsg(e.getMessage());
        wrapper.setStatus(status);
        return wrapper;
    }
}
