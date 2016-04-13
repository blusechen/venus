/**
 * 
 */
package com.meidusa.venus.util;

/**
 * 
 * 
 * @author Sun Ning
 * @since 2010-3-4
 */
public class InvalidParameterException extends RuntimeException {
    private static final long serialVersionUID = -1583661559860123414L;

    public InvalidParameterException(String msg) {
        super("InvalidParameterException:" + msg);
    }
}
