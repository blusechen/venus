package com.meidusa.venus.validate.validator.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Validator(factory = AnnotationValidatorFactoryGroup.LongRangeFactory.class)
@Target({ ElementType.PARAMETER, ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
public @interface LongRange {
    long min() default Long.MIN_VALUE;

    long max() default Long.MAX_VALUE;

    String message() default "#{*} does not match the long range, should between \"@{min}\" and \"@{max}\".";

    String policy() default "";
}
