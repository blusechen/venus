package com.meidusa.venus.validate.chain;

import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.beanutils.PropertyUtils;

import com.meidusa.venus.validate.exception.ValidationException;
import com.meidusa.venus.validate.holder.OgnlBasicValueHolder;
import com.meidusa.venus.validate.holder.ValueHolder;
import com.meidusa.venus.validate.validator.FieldValidator;
import com.meidusa.venus.validate.validator.Validator;
import com.meidusa.venus.validate.validator.ValidatorSupport;

/**
 * ValidatorChain implemention
 * 
 * @author lichencheng.daisy
 * @since 1.0.0-SNAPSHOT
 * 
 */
public class VenusValidatorChain implements ValidatorChain {
    private List<Validator> expression;
    private Map<String, List<FieldValidator>> fieldValidators;

    public VenusValidatorChain() {
        this.expression = new ArrayList<Validator>();
        this.fieldValidators = new HashMap<String, List<FieldValidator>>();
    }

    @Override
    public void validate(Object params) throws ValidationException {
        ValueHolder paramsHolder = new OgnlBasicValueHolder(params);
        for (Entry<String, List<FieldValidator>> entry : fieldValidators.entrySet()) {
            try {
                for (FieldValidator validator : entry.getValue()) {
                    validator.validate(getProperty(params, entry.getKey()));
                }
            } catch (Exception e) {
                if (e instanceof ValidationException) {
                    throw (ValidationException) e;
                } else {
                    throw new ValidationException("can't get property " + entry.getKey(), e);
                }
            }
        }
        for (Iterator<Validator> validatorIter = expression.iterator(); validatorIter.hasNext();) {
            Validator validator = validatorIter.next();
            ValidatorSupport paramsValidator = (ValidatorSupport) validator;
            ((ValidatorSupport) validator).setValueHolder(paramsHolder);
            paramsValidator.validate(params);
        }
    }

    private Object getProperty(Object o, String propertyName) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        if (o.getClass().isArray()) {
            return Array.get(o, Integer.valueOf(propertyName.substring(5)));
        } else {
            return PropertyUtils.getProperty(o, propertyName);
        }
    }

    @Override
    public void addValidator(Validator validator) {
        if (validator instanceof FieldValidator) {
            List<FieldValidator> fieldValidatorList = this.fieldValidators.get(((FieldValidator) validator).getFieldName());
            if (fieldValidatorList == null) {
                fieldValidatorList = new LinkedList<FieldValidator>();
                this.fieldValidators.put(((FieldValidator) validator).getFieldName(), fieldValidatorList);
            }
            fieldValidatorList.add((FieldValidator) validator);
        } else {
            expression.add(validator);
        }

    }

}
