package com.meidusa.venus.validate.asm;

import com.meidusa.venus.validate.exception.ValidationException;
import com.meidusa.venus.validate.validator.AsmValidatorSupport;
import com.meidusa.venus.validate.validator.Validator;

public class ExampleValidator extends AsmValidatorSupport {

    private Validator validators_0;
    private Validator a_validators_0;
    private Validator b_validators_0;
    private Validator c_validators_0;

    public ExampleValidator() {

        validators_0 = super.createFromClass(Example.class, 0);
        a_validators_0 = super.createFromField(Example.class, "a", 0);
        b_validators_0 = super.createFromField(Example.class, "b", 0);
        c_validators_0 = super.createFromField(Example.class, "c", 0);
    }

    @Override
    public void validate(Object object) throws ValidationException {
        Example o = (Example) object;

        validators_0.validate(o);

        a_validators_0.validate(o.getA());

        b_validators_0.validate(o.getB());

        c_validators_0.validate(o.getC());
    }

    public static void main(String[] args) {
        System.out.println("aaa");
    }

}
