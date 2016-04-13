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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.meidusa.venus.validate.exception.ValidationException;

/**
 * <!-- START SNIPPET: javadoc --> Validates a string field using a regular expression. <!-- END SNIPPET: javadoc -->
 * <p/>
 * 
 * 
 * <!-- START SNIPPET: parameters -->
 * <ul>
 * <li>fieldName - The field name this validator is validating. Required if using Plain-Validator Syntax otherwise not
 * required</li>
 * <li>expression - The RegExp expression REQUIRED</li>
 * <li>caseSensitive - Boolean (Optional). Sets whether the expression should be matched against in a case-sensitive
 * way. Default is <code>true</code>.</li>
 * <li>trim - Boolean (Optional). Sets whether the expression should be trimed before matching. Default is
 * <code>true</code>.</li>
 * </ul>
 * <!-- END SNIPPET: parameters -->
 * 
 * 
 * <pre>
 * <!-- START SNIPPET: example -->
 *    &lt;validators&gt;
 *        &lt;!-- Plain Validator Syntax --&gt;
 *        &lt;validator type="regex"&gt;
 *            &lt;param name="fieldName"&gt;myStrangePostcode&lt;/param&gt;
 *            &lt;param name="expression"&gt;&lt;![CDATA[([aAbBcCdD][123][eEfFgG][456])]]&lt;&gt;/param&gt;
 *        &lt;/validator&gt;
 *    
 *        &lt;!-- Field Validator Syntax --&gt;
 *        &lt;field name="myStrangePostcode"&gt;
 *            &lt;field-validator type="regex"&gt;
 *               &lt;param name="expression"&gt;&lt;![CDATA[([aAbBcCdD][123][eEfFgG][456])]]&gt;&lt;/param&gt;
 *            &lt;/field-validator&gt;
 *        &lt;/field&gt;
 *    &lt;/validators&gt;
 * <!-- END SNIPPET: example -->
 * </pre>
 * 
 * @author Quake Wang
 * @version $Date: 2009-12-27 19:18:29 +0100 (Sun, 27 Dec 2009) $ $Revision: 894090 $
 */
public class RegexFieldValidator extends FieldValidatorSupport {

    private String expression;
    private boolean caseSensitive = true;
    private boolean trim = true;
    private Pattern pattern;

    public void initPattern() {
        if (expression == null) {
            return;
        }
        // match against expression
        if (isCaseSensitive()) {
            pattern = Pattern.compile(expression);
        } else {
            pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE);
        }
    }

    public void validate(Object object) throws ValidationException {
        if (expression != null && pattern == null) {
            this.initPattern();
        }
        String fieldName = getFieldName();
        // if there is no value - don't do comparison
        // if a value is required, a required validator should be added to the field
        if (object == null || expression == null) {
            return;
        }

        // XW-375 - must be a string
        if (!(object instanceof String)) {
            return;
        }

        // string must not be empty
        String str = ((String) object).trim();
        if (str.length() == 0) {
            return;
        }

        String compare = (String) object;
        if (trim) {
            compare = compare.trim();
        }
        Matcher matcher = pattern.matcher(compare);

        if (!matcher.matches()) {
            addFieldValidationError(fieldName, object);
        }
    }

    /**
     * @return Returns the regular expression to be matched.
     */
    public String getExpression() {
        return expression;
    }

    /**
     * Sets the regular expression to be matched.
     */
    public void setExpression(String expression) {
        this.expression = expression;
    }

    /**
     * @return Returns whether the expression should be matched against in a case-sensitive way. Default is
     *         <code>true</code>.
     */
    public boolean isCaseSensitive() {
        return caseSensitive;
    }

    /**
     * Sets whether the expression should be matched against in a case-sensitive way. Default is <code>true</code>.
     */
    public void setCaseSensitive(boolean caseSensitive) {
        this.caseSensitive = caseSensitive;
    }

    /**
     * @return Returns whether the expression should be trimed before matching. Default is <code>true</code>.
     */
    public boolean isTrimed() {
        return trim;
    }

    /**
     * Sets whether the expression should be trimed before matching. Default is <code>true</code>.
     */
    public void setTrim(boolean trim) {
        this.trim = trim;
    }

}
