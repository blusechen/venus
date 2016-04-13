package com.meidusa.venus.validate.validator.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Validator(factory = AnnotationValidatorFactoryGroup.RegexFactory.class)
@Target({ ElementType.PARAMETER, ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
public @interface Regex {
    String value();

    boolean caseSensitive() default true;

    boolean trim() default true;

    String message() default "string #{*} does not match the regex specified : \"@{expression}\".";

    String policy() default "";
}
