/*
 * Copyright 2002-2006,2009 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.meidusa.venus.validate.validator;

import com.meidusa.venus.validate.exception.ValidationException;

/**
 * Base class for range based validators.
 * 
 * @author Jason Carreira
 * @author Cameron Braid
 */
public abstract class AbstractRangeValidator extends FieldValidatorSupport {

    @Override
    public void validate(Object object) throws ValidationException {
        Comparable value = (Comparable) object;

        // if there is no value - don't do comparison
        // if a value is required, a required validator should be added to the
        // field
        if (value == null) {
            return;
        }

        // only check for a minimum value if the min parameter is set
        if ((getMinComparatorValue() != null) && (value.compareTo(getMinComparatorValue()) < 0)) {
            addFieldValidationError(getFieldName(), object);
        }

        // only check for a maximum value if the max parameter is set
        if ((getMaxComparatorValue() != null) && (value.compareTo(getMaxComparatorValue()) > 0)) {
            addFieldValidationError(getFieldName(), object);
        }
    }

    protected abstract Comparable getMaxComparatorValue();

    protected abstract Comparable getMinComparatorValue();

}
