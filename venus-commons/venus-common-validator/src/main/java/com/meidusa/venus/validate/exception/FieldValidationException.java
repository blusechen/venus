package com.meidusa.venus.validate.exception;

public class FieldValidationException extends ValidationException {

    /**
	 * 
	 */
    private static final long serialVersionUID = 1L;
    private String fieldName;
    private String fieldErrorMsg;

    public FieldValidationException(String fieldName, String fieldErrorMsg) {
        super();
        this.fieldName = fieldName;
        this.fieldErrorMsg = fieldErrorMsg;
    }

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public String getFieldErrorMsg() {
        return fieldErrorMsg;
    }

    public void setFieldErrorMsg(String fieldErrorMsg) {
        this.fieldErrorMsg = fieldErrorMsg;
    }

    @Override
    public String getMessage() {
        return this.fieldName + " : " + this.fieldErrorMsg;
    }

}
