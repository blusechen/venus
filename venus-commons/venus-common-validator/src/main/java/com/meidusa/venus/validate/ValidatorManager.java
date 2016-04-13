package com.meidusa.venus.validate;

import java.lang.reflect.Method;

import com.meidusa.venus.validate.chain.ValidatorChain;

/**
 * ValidatorManager load validation chain for specified service and endpoint.
 * 
 * @author lichencheng.daisy
 * @since 1.0.0-SNAPSHOT
 * 
 */
public interface ValidatorManager {
    ValidatorChain getValidatorChain(Method method);

    void init();

}
