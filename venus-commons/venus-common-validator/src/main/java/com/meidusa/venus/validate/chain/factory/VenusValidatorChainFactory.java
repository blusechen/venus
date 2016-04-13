package com.meidusa.venus.validate.chain.factory;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import com.meidusa.venus.annotations.Param;
import com.meidusa.venus.validate.chain.ValidatorChain;
import com.meidusa.venus.validate.exception.ValidationRuntimeException;
import com.meidusa.venus.validate.file.DefaultValidationFileParser;
import com.meidusa.venus.validate.file.ValidationFileInfo;
import com.meidusa.venus.validate.file.ValidationFileParser;
import com.meidusa.venus.validate.validator.ExpressionValidator;
import com.meidusa.venus.validate.validator.annotation.AnnotationValidatorFactory;
import com.meidusa.venus.validate.validator.annotation.Expression;
import com.meidusa.venus.validate.validator.annotation.Validator;

public class VenusValidatorChainFactory implements ValidatorChainFactory {
    private ValidationFileParser validationFileParser;
    private String suffix = "";
    public static Map<Method, String[]> paramNameMapping = new HashMap<Method, String[]>();

    public String getSuffix() {
        return suffix;
    }

    public void setSuffix(String suffix) {
        this.suffix = suffix;
    }

    public VenusValidatorChainFactory() {
        super();
        this.validationFileParser = new DefaultValidationFileParser();
    }

    public ValidationFileParser getValidationFileParser() {
        return validationFileParser;
    }

    public void setValidationFileParser(ValidationFileParser validationFileParser) {
        this.validationFileParser = validationFileParser;
    }

    @Override
    public ValidatorChain createValidatorChain(Method method) {
        ValidationFileInfo info = new ValidationFileInfo(method.getDeclaringClass(), method.getName(), this.getSuffix());
        ValidatorChain chain = validationFileParser.parseValidationConfigs(info);
        this.calculateChainByAnnotation(chain, method);
        return chain;
    }

    private void calculateChainByAnnotation(ValidatorChain chain, Method method) {
        String[] paramsNames = new String[method.getParameterTypes().length];

        Annotation[] methodAnnotation = method.getAnnotations();
        if (methodAnnotation != null) {
            for (int i = 0; i < methodAnnotation.length; i++) {
                if (methodAnnotation[i].annotationType() == Expression.class) {
                    ExpressionValidator validator = new ExpressionValidator();
                    validator.setExpression(((Expression) methodAnnotation[i]).value());
                    validator.setMessage(((Expression) methodAnnotation[i]).message());
                    validator.setName(method.toString() + "'s param");
                    chain.addValidator(validator);
                }
            }
        }
        Annotation[][] fieldAnnotations = method.getParameterAnnotations();
        for (int i = 0; i < fieldAnnotations.length; i++) {
            String fieldName = null;
            boolean fieldNameSet = false;
            for (int j = 0; j < fieldAnnotations[i].length; j++) {
                if (fieldAnnotations[i][j] instanceof Param) {
                    Param param = (Param) fieldAnnotations[i][j];
                    fieldName = param.name();
                    fieldNameSet = true;
                }
            }
            if (!fieldNameSet) {
                fieldName = new StringBuilder("param_").append(String.valueOf(i)).toString();
            }
            paramsNames[i] = fieldName;
            for (int j = 0; j < fieldAnnotations[i].length; j++) {
                Annotation fieldAnnotation = fieldAnnotations[i][j];
                Validator validatorAnnotation = fieldAnnotation.annotationType().getAnnotation(Validator.class);
                if (validatorAnnotation != null) {
                    Class<? extends AnnotationValidatorFactory> factoryClass = validatorAnnotation.factory();
                    AnnotationValidatorFactory factory;
                    try {
                        factory = factoryClass.newInstance();
                    } catch (Exception e) {
                        throw new ValidationRuntimeException(e);
                    }
                    com.meidusa.venus.validate.validator.Validator validator = factory.createValidator(fieldAnnotation, fieldName);
                    if (validator != null) {
                        chain.addValidator(validator);
                    }
                }

            }
            synchronized (paramNameMapping) {
                paramNameMapping.put(method, paramsNames);
            }
        }
    }
}
