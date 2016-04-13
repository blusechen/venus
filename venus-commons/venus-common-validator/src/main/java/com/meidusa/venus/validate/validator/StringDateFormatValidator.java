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

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import com.meidusa.venus.validate.exception.ValidationException;

/**
 * String should match dateformat and length is explictly correct.
 * 
 * @author daisy
 * 
 */
public class StringDateFormatValidator extends FieldValidatorSupport {

    String dateFormat = "yyyyMMddHHmmss";
    DateFormat simpleDateFormat;

    public String getDateFormat() {
        return dateFormat;
    }

    public void setDateFormat(String dateFormat) {
        this.dateFormat = dateFormat;
    }

    public void validate(Object object) throws ValidationException {
        String fieldName = getFieldName();
        if (object != null) {
            if (simpleDateFormat == null) {
                simpleDateFormat = new SimpleDateFormat(dateFormat);
            }
            String date = object.toString();
            if (date.length() != dateFormat.length()) {
                addFieldValidationError(fieldName, object);
            }

        }
    }

}
