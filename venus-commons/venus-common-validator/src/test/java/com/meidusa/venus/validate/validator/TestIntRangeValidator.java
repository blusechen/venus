package com.meidusa.venus.validate.validator;

import java.util.HashMap;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.meidusa.venus.validate.exception.ValidationException;
import com.meidusa.venus.validate.service.ServiceExample;

public class TestIntRangeValidator extends BaseValidatorTest {

    @Before
    public void init() {
        try {
            this.initChain(ServiceExample.class, ServiceExample.class.getDeclaredMethod("testIntRange", int.class));
        } catch (Exception e) {
            // ignore here
        }

    }

    @Test
    public void testSuccess() {

        HashMap<String, Object> map = new HashMap<String, Object>();
        map.put("intValue", 1);
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
        map.put("intValue", 2000);
        try {
            this.validate(map);
        } catch (ValidationException e) {
            success = true;
        }
        Assert.assertTrue(success);
    }
}
