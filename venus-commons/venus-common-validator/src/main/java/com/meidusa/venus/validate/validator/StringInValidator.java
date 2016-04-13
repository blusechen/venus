package com.meidusa.venus.validate.validator;

import java.util.List;

import com.meidusa.venus.validate.exception.ValidationException;

public class StringInValidator extends FieldValidatorSupport {

    private boolean trim = true;
    private List<String> inString;

    public void setTrim(boolean trim) {
        this.trim = trim;
    }

    public boolean getTrim() {
        return trim;
    }

    public List<String> getInString() {
        return inString;
    }

    public void setInString(List<String> inString) {
        this.inString = inString;
    }

    public void validate(Object object) throws ValidationException {
        String fieldName = getFieldName();

        if (object == null) {
            return;
        }
        if (!(object instanceof String)) {
            addFieldValidationError(fieldName, object);
        } else {
            String s = (String) object;

            if (trim) {
                s = s.trim();
            }
            for (String str : inString) {
                if (s.equals(str)) {
                    return;
                }
            }
            addFieldValidationError(fieldName, object);
        }
    }
}
