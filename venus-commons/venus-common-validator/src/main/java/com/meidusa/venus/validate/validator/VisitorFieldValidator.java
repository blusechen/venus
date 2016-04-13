package com.meidusa.venus.validate.validator;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang.builder.HashCodeBuilder;

import com.meidusa.venus.validate.AsmVisitorValidatorFactory;
import com.meidusa.venus.validate.exception.ValidationException;

public class VisitorFieldValidator extends FieldValidatorSupport {

    public static class ClassPolicy {
        private Class<?> clazz;
        private String policy;

        public ClassPolicy(Class<?> clazz, String policy) {
            super();
            this.clazz = clazz;
            this.policy = policy;
        }

        public Class<?> getClazz() {
            return clazz;
        }

        public void setClazz(Class<?> clazz) {
            this.clazz = clazz;
        }

        public String getPolicy() {
            return policy;
        }

        public void setPolicy(String policy) {
            this.policy = policy;
        }

        /**
         * @see java.lang.Object#hashCode()
         */
        public int hashCode() {
            return new HashCodeBuilder(780293071, -917577685).append(this.policy).append(this.clazz).toHashCode();
        }

    }

    private static AsmVisitorValidatorFactory factory = new AsmVisitorValidatorFactory();
    private static Map<ClassPolicy, Validator> internalValidatorChainMapping = new HashMap<ClassPolicy, Validator>();

    private String path;
    private String policy;
    private Validator internalValidatorChain = null;

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getPolicy() {
        return policy;
    }

    public void setPolicy(String policy) {
        this.policy = policy;
    }

    public Validator getInternalValidatorChain() {
        return internalValidatorChain;
    }

    public void setInternalValidatorChain(Validator internalValidatorChain) {
        this.internalValidatorChain = internalValidatorChain;
    }

    @Override
    public void validate(Object object) throws ValidationException {
        if (object == null) {
            return;
        }
        if (object instanceof Map) {
            Set<Map.Entry> entries = ((Map) object).entrySet();
            for (Entry element : entries) {
                validateObject(element.getValue());
            }
        } else if (object instanceof Collection) {
            for (Object element : (Collection) object) {
                validateObject(element);
            }
        } else if (object.getClass().isArray()) {
            for (int i = 0; i < Array.getLength(object); i++) {
                validateObject(Array.get(object, i));
            }
        } else {
            validateObject(object);
        }

    }

    private void validateObject(Object object) throws ValidationException {
        ClassPolicy validatorClassPolicy = new ClassPolicy(object.getClass(), policy);
        Validator chain = internalValidatorChainMapping.get(validatorClassPolicy);
        if (chain == null) {
            synchronized (internalValidatorChainMapping) {
                chain = internalValidatorChainMapping.get(validatorClassPolicy);
                if (chain == null) {
                    chain = factory.createAsmVistorValidator(policy, object.getClass(), this.getFieldName());
                    this.internalValidatorChainMapping.put(validatorClassPolicy, chain);
                }
            }
        }
        chain.validate(object);
        if (internalValidatorChain != null) {
            internalValidatorChain.validate(object);
        }
    }

}
