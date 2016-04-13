package com.meidusa.venus.validate.validator.annotation;

import java.lang.annotation.Annotation;

public interface AnnotationValidatorFactory<A extends Annotation, V extends com.meidusa.venus.validate.validator.Validator> {

    V createValidator(A annotation, String fieldName);
}
