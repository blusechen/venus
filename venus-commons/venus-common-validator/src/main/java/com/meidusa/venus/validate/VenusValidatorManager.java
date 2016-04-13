package com.meidusa.venus.validate;

import java.lang.reflect.Method;
import java.util.HashMap;

import com.meidusa.venus.validate.chain.ValidatorChain;
import com.meidusa.venus.validate.chain.factory.ValidatorChainFactory;
import com.meidusa.venus.validate.chain.factory.VenusValidatorChainFactory;

/**
 * ValidatorManager impelmentation. Load validatorChain while being used.
 * 
 * @author lichencheng.daisy
 * @since 1.0.0-SNAPSHOT
 * 
 */
public class VenusValidatorManager implements ValidatorManager {
    private ValidatorChainFactory chainFactory;

    private HashMap<Method, ValidatorChain> validatorChainMapping;

    public ValidatorChainFactory getChainFactory() {
        return chainFactory;
    }

    public void setChainFactory(ValidatorChainFactory chainFactory) {
        this.chainFactory = chainFactory;
    }

    @Override
    public ValidatorChain getValidatorChain(Method method) {

        ValidatorChain endpointChains = this.validatorChainMapping.get(method);
        if (endpointChains == null) {
            synchronized (validatorChainMapping) {
                endpointChains = chainFactory.createValidatorChain(method);
                validatorChainMapping.put(method, endpointChains);
            }
        }
        return endpointChains;
    }

    @Override
    public void init() {
        this.validatorChainMapping = new HashMap<Method, ValidatorChain>();
        this.chainFactory = new VenusValidatorChainFactory();

    }

}
