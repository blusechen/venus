package com.meidusa.venus.validate.validator;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;

import com.meidusa.venus.validate.exception.ValidationException;
import com.meidusa.venus.validate.holder.OgnlBasicValueHolder;
import com.meidusa.venus.validate.holder.ValueHolder;

public class TestValidatorSupport {
    private ValidatorSupport support;
    private ValueHolder valueHolder;

    @Before
    public void initValidatorSupport() {
        support = new ValidatorSupport() {

            @Override
            public void validate(Object object) throws ValidationException {
                System.out.println("now it is doing validation");

            }

            @Override
            public String describeValidateName() {
                return "for test";
            }
        };
        valueHolder = new OgnlBasicValueHolder();
        valueHolder.setRoot(Complicated.getDefault());

        support.setValueHolder(valueHolder);
        support.setMessage("#{map.a} lalala #{map.b} lalala #{map.c} lalala");

    }

    @Test
    public void testValueStackGet() {
        Assert.assertEquals("A", valueHolder.findString("map.a"));
        System.out.println("map.a = " + valueHolder.findString("map.a"));
    }

    @Test
    public void messageGeneration() {
        System.out.println(support.getMessageParameters());
        Assert.assertEquals(support.getMessage(null), "A lalala B lalala C lalala");
        System.out.println(support.getMessage(null));
    }

}
