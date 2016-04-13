package com.meidusa.venus.validate.chain;

import com.meidusa.venus.validate.validator.Validator;

/**
 * A special Validator which contains several Validator.
 * 
 * @author lichencheng.daisy
 * @since 1.0.0-SNAPSHOT
 * 
 */
public interface ValidatorChain extends Validator {
    void addValidator(Validator validator);

}
