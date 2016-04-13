package com.meidusa.venus.backend.interceptor;

import com.meidusa.venus.backend.EndpointInvocation;
import com.meidusa.venus.backend.services.Endpoint;
import com.meidusa.venus.exception.InvalidParameterException;
import com.meidusa.venus.validate.ValidatorManager;
import com.meidusa.venus.validate.VenusValidatorManager;
import com.meidusa.venus.validate.exception.ValidationException;
import com.meidusa.venus.validate.validator.Validator;

public class ValidatorInterceptor extends AbstractInterceptor {
    private static ValidatorManager validatorManager = new VenusValidatorManager();
    static {
        validatorManager.init();
    }

    @Override
    public Object intercept(EndpointInvocation invocation) {
        Endpoint endpoint = invocation.getEndpoint();
        Validator chain = validatorManager.getValidatorChain(endpoint.getMethod());
        try {
            chain.validate(invocation.getContext().getParameters());
            return invocation.invoke();
        } catch (ValidationException e) {
            throw new InvalidParameterException(e.getMessage());
        }
    }

}
