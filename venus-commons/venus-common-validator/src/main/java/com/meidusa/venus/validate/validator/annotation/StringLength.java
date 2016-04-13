package com.meidusa.venus.validate.validator.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Validator(factory = AnnotationValidatorFactoryGroup.StringLengthFactory.class)
@Target({ ElementType.PARAMETER, ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
public @interface StringLength {
    boolean trim() default true;

    int minLength() default Integer.MIN_VALUE;

    int maxLength() default Integer.MAX_VALUE;

    String message() default "#{*} does not match string range : from @{minLength} to @{maxLength}.";

    String policy() default "";
}
