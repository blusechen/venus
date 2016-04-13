package com.meidusa.venus.validate.validator.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Validator(factory = AnnotationValidatorFactoryGroup.StringInFactory.class)
@Target({ ElementType.PARAMETER, ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
public @interface StringIn {
    String[] in() default {};

    boolean trim() default true;

    String message() default "#{*} must be in some options specied : @{inString}";

    String policy() default "";

}
