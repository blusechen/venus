package com.meidusa.venus.validate.validator.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Validator(factory = AnnotationValidatorFactoryGroup.DateRangeFactory.class)
@Target({ ElementType.PARAMETER, ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
public @interface DateRange {
    String min();

    String max();

    String format() default "yyyy-MM-dd HH:mm:ss";

    String message() default "#{*} does not match the date range, should between \"@{min}\" and \"@{max}\".";

    String policy() default "";
}
