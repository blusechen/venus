package com.meidusa.venus.validate.validator;

import java.lang.annotation.Annotation;

import com.meidusa.venus.validate.exception.ValidationRuntimeException;
import com.meidusa.venus.validate.validator.annotation.AnnotationValidatorFactory;

public abstract class AsmValidatorSupport extends FieldValidatorSupport {

    public AsmValidatorSupport() {
        super();
    }

    private String fieldName;

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public Validator createFromField(Class<?> clazz, String givenFieldName, int annotationNo) {
        try {
            Annotation anno = clazz.getDeclaredField(givenFieldName).getAnnotations()[annotationNo];
            com.meidusa.venus.validate.validator.annotation.Validator validatorAnnotation = anno.annotationType().getAnnotation(
                    com.meidusa.venus.validate.validator.annotation.Validator.class);
            AnnotationValidatorFactory factory;
            try {
                factory = validatorAnnotation.factory().newInstance();
            } catch (Exception e) {
                throw new ValidationRuntimeException(e);
            }
            return factory.createValidator(anno, givenFieldName);
        } catch (Exception e) {
            throw new RuntimeException("this should not happened");
        }
    }

    public Validator createFromClass(Class<?> clazz, int annotationNo) {
        Annotation anno = clazz.getAnnotations()[annotationNo];
        com.meidusa.venus.validate.validator.annotation.Validator validatorAnnotation = anno.annotationType().getAnnotation(
                com.meidusa.venus.validate.validator.annotation.Validator.class);
        AnnotationValidatorFactory factory;
        try {
            factory = validatorAnnotation.factory().newInstance();
        } catch (Exception e) {
            throw new ValidationRuntimeException(e);
        }
        return factory.createValidator(anno, this.getFieldName());
    }
}
