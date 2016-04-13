/**
 * 
 */
package com.meidusa.venus.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

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
