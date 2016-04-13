package com.meidusa.venus.validate.validator.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Validator(factory = AnnotationValidatorFactoryGroup.IntRangeFactory.class)
@Target({ ElementType.PARAMETER, ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
public @interface IntRange {
    int max() default Integer.MAX_VALUE;

    int min() default Integer.MIN_VALUE;

    String message() default "#{*} does not match the interger range, should between \"@{min}\" and \"@{max}\".";

    String policy() default "";
}
