package com.meidusa.venus.validate.chain.factory;

import java.lang.reflect.Method;

import com.meidusa.venus.validate.chain.ValidatorChain;

/**
 * Create validatorChain using service name and endpoint name
 * 
 * @author lichencheng.daisy
 * @since 1.0.0-SNAPSHOT
 * 
 */
public interface ValidatorChainFactory {

    ValidatorChain createValidatorChain(Method endpoint);

}
