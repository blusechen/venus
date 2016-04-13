package com.meidusa.venus.validate.validator;

import java.util.HashMap;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.meidusa.venus.validate.exception.ValidationException;
import com.meidusa.venus.validate.service.ServiceExample;

public class TestDoubleRangeValidator extends BaseValidatorTest {

    @Before
    public void init() {
        try {
            this.initChain(ServiceExample.class, ServiceExample.class.getDeclaredMethod("testDoubleRange", double.class));
        } catch (Exception e) {
            // ignore here
        }

    }

    @Test
    public void testSuccess() {

        HashMap<String, Object> map = new HashMap<String, Object>();
        map.put("doubleValue", 1.359);
        try {
            this.validate(map);
        } catch (ValidationException e) {
            Assert.fail(e.getMessage());
        }

    }

    @Test
    public void testFailure() {
        boolean success = false;
        HashMap<String, Object> map = new HashMap<String, Object>();
        map.put("doubleValue", 1.369);
        try {
            this.validate(map);
        } catch (ValidationException e) {
            success = true;
        }
        Assert.assertTrue(success);
    }
}
