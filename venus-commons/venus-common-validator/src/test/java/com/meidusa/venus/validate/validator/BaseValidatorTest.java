package com.meidusa.venus.validate.validator;

import java.lang.reflect.Method;
import java.util.Map;

import org.junit.BeforeClass;
import org.junit.Test;

import com.meidusa.venus.validate.ValidatorManager;
import com.meidusa.venus.validate.VenusValidatorManager;
import com.meidusa.venus.validate.chain.ValidatorChain;
import com.meidusa.venus.validate.exception.ValidationException;

public class BaseValidatorTest {

    private ValidatorChain chain;
    private static ValidatorManager manager;

    @BeforeClass
    public static void initValidatorManager() {
        manager = new VenusValidatorManager();
        manager.init();
    }

    protected void validate(Map<String, Object> parameterMap) throws ValidationException {
        chain.validate(parameterMap);
    }

    public void initChain(Class<?> service, Method endpoint) {
        chain = manager.getValidatorChain(endpoint);
    }

    @Test
    public void testEmpty() {

    }

}
