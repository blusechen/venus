/**
 * 
 */
package com.meidusa.venus.backend.services;

/**
 * System parameter, starts with '@', used for parsing trivial params
 * 
 * @author Sun Ning
 * @since 2010-3-24
 */
public class SystemParameter extends Parameter {

    /**
     * delimiter, as separator of a string array
     */
    public static final String SYSTEM_PARAMETER_DELIMITER = "@delimiter";

    /**
     * test whether a parameter name is a system parameter or not
     * 
     * @param parameterName
     * @return
     */
    public static boolean isSystemParameterName(String parameterName) {
        if (parameterName == null) {
            return false;
        }

        return parameterName.startsWith("@");
    }
}
