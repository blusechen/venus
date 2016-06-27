/**
 * 
 */
package com.meidusa.venus.annotations;

import java.lang.annotation.*;

/**
 * @author structchen
 * 
 */
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface PerformanceLevel {
    int info() default 5 * 1000;

    int warn() default 10 * 1000;

    int error() default 20 * 1000;

    boolean printParams() default false;

    boolean printResult() default false;
}
