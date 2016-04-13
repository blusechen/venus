package com.meidusa.venus.validate.validator;

import java.util.HashMap;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.meidusa.venus.validate.exception.ValidationException;
import com.meidusa.venus.validate.service.ServiceExample;

public class TestStringDateFormate extends BaseValidatorTest {

    @Before
    public void init() {
        try {
            this.initChain(ServiceExample.class, ServiceExample.class.getDeclaredMethod("testStringDateFormat", String.class));
        } catch (Exception e) {
            // ignore here
        }

    }

    @Test
    public void testSuccess() {

        HashMap<String, Object> map = new HashMap<String, Object>();
        map.put("requestTime", "19230606231111");
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
        map.put("requestTime", "1923-06-06-231111");
        try {
            this.validate(map);
        } catch (ValidationException e) {
            success = true;
        }
        Assert.assertTrue(success);
    }
}
