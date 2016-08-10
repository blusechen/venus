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
 * @author structchen
 * 
 */
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface PerformanceLevel {
    int info() default 1 * 1000;

    int warn() default 3 * 1000;

    int error() default 5 * 1000;

    boolean printParams() default false;

    boolean printResult() default false;
}
