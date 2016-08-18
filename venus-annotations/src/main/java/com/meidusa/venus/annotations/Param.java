/**
 * 
 */
package com.meidusa.venus.annotations;

import java.lang.annotation.*;

/**
 * provider meta information about paramter of endpoint
 * 
 * @author Sun Ning
 * @since 2010-3-5
 */
@Documented
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface Param {

    String name();

    String defaultValue() default "";

    boolean optional() default false;
}
