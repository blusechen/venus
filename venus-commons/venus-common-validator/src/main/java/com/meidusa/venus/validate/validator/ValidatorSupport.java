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

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.beanutils.PropertyUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.meidusa.venus.validate.exception.ValidationException;
import com.meidusa.venus.validate.holder.ValueHolder;
import com.meidusa.venus.validate.validator.handler.ErrorMsgHandler;
import com.meidusa.venus.validate.validator.handler.ExceptionErrorMsgHandler;

public abstract class ValidatorSupport implements Validator {

    private static Pattern pattern = Pattern.compile("\\#\\{([^\\}]+)\\}");
    private static String STAND_FOR_NAME = "*";
    private static String STAND_FOR_VALUE = "&";

    private static Pattern validatorPattern = Pattern.compile("\\@\\{([^\\}]+)\\}");

    protected final Logger logger = LoggerFactory.getLogger(this.getClass());
    protected String message;
    private String type;
    private String[] messageParameters;
    private ValueHolder holder;
    private ErrorMsgHandler errHandler;

    public ValidatorSupport() {
        super();
        errHandler = new ExceptionErrorMsgHandler();
    }

    public void setValueHolder(ValueHolder stack) {
        this.holder = stack;
    }

    public void setMessage(String message) {
        List<String> messageParams = new ArrayList<String>();
        Matcher matcher = pattern.matcher(message);
        while (matcher.find()) {
            messageParams.add(matcher.group(1));
        }
        this.messageParameters = (String[]) messageParams.toArray(new String[] {});
        this.message = matcher.replaceAll("%s");
    }

    public String getMessage(Object value) {
        if (message == null) {
            return "";
        }
        String result = null;
        if (messageParameters == null || messageParameters.length == 0) {
            result = message;
        } else {
            Object[] parsedParameters = new Object[messageParameters.length];
            for (int i = 0; i < this.messageParameters.length; i++) {
                if (STAND_FOR_NAME.equals(messageParameters[i])) {
                    parsedParameters[i] = this.describeValidateName();
                } else if (STAND_FOR_VALUE.equals(messageParameters[i])) {
                    if (value != null) {
                        parsedParameters[i] = value.toString();
                    } else {
                        parsedParameters[i] = "null";

                    }
                } else {
                    parsedParameters[i] = holder.findString(messageParameters[i]);
                }
            }
            result = String.format(message, parsedParameters);
        }
        Matcher matcher = validatorPattern.matcher(result);
        StringBuffer sb = new StringBuffer();
        Object obj = null;
        while (matcher.find()) {
            try {
                obj = PropertyUtils.getProperty(this, matcher.group(1));
            } catch (Exception e) {
                obj = "null";
            }
            if (obj == null) {
                obj = "null";
            }
            matcher.appendReplacement(sb, obj.toString());
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    public abstract String describeValidateName();

    public String[] getMessageParameters() {
        return this.messageParameters;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    // /**
    // * Parse <code>expression</code> passed in against value stack. Only parse
    // * when 'parse' param is set to true, else just returns the expression
    // unparsed.
    // *
    // * @param expression
    // * @return Object
    // */
    // protected Object conditionalParse(String expression) {
    // if (parse) {
    // return TextParseUtil.translateVariables('$', expression, stack);
    // }
    // return expression;
    // }

    /**
     * Return the field value named <code>name</code> from <code>object</code>, <code>object</code> should have the
     * appropriate getter/setter.
     * 
     * @param name
     * @param object
     * @return Object as field value
     * @throws ValidationException
     */
    protected Object getFieldValue(String name) throws ValidationException {

        Object retVal = holder.findValue(name);
        return retVal;
    }

    protected void addValidationError(Object o) throws ValidationException {
        errHandler.handleValidationError(getMessage(o));
    }

    protected void addFieldValidationError(String propertyName, Object o) throws ValidationException {
        errHandler.handleValidationFieldError(propertyName, getMessage(o));
    }

}
