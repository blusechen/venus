package com.meidusa.venus.validate.validator.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Validator(factory = AnnotationValidatorFactoryGroup.DoubleRangeFactory.class)
@Target({ ElementType.PARAMETER, ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
public @interface DoubleRange {
    double maxInclusive() default Double.MAX_VALUE;

    double minInclusive() default Double.MIN_VALUE;

    double minExclusive() default Double.MIN_VALUE;

    double maxExclusive() default Double.MAX_VALUE;

    String message() default "#{*} does not match the double range, maxInclusive : @{maxInclusive}, "
            + "minInclusive : @{minInclusive}, minExclusive : @{minExclusive}, maxExclusive : @{maxExclusive}.";

    String policy() default "";
}
