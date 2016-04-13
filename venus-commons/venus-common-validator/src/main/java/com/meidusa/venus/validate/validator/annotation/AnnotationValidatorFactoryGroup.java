package com.meidusa.venus.validate.validator.annotation;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.HashMap;

import com.meidusa.venus.validate.exception.ValidationRuntimeException;
import com.meidusa.venus.validate.validator.DateRangeFieldValidator;
import com.meidusa.venus.validate.validator.DoubleRangeFieldValidator;
import com.meidusa.venus.validate.validator.EmailValidator;
import com.meidusa.venus.validate.validator.FieldExpressionValidator;
import com.meidusa.venus.validate.validator.IntRangeFieldValidator;
import com.meidusa.venus.validate.validator.LongRangeFieldValidator;
import com.meidusa.venus.validate.validator.RegexFieldValidator;
import com.meidusa.venus.validate.validator.RequiredFieldValidator;
import com.meidusa.venus.validate.validator.StringDateFormatValidator;
import com.meidusa.venus.validate.validator.StringInValidator;
import com.meidusa.venus.validate.validator.StringLengthFieldValidator;
import com.meidusa.venus.validate.validator.StringNotEmptyValidator;
import com.meidusa.venus.validate.validator.URLValidator;
import com.meidusa.venus.validate.validator.VisitorFieldValidator;

public class AnnotationValidatorFactoryGroup {

    public static class DateRangeFactory implements AnnotationValidatorFactory<DateRange, DateRangeFieldValidator> {
        private HashMap<String, DateFormat> patternMap = new HashMap<String, DateFormat>();

        @Override
        public DateRangeFieldValidator createValidator(DateRange anno, String fieldName) {
            DateRangeFieldValidator validator = new DateRangeFieldValidator();

            DateFormat format = patternMap.get(anno.format());
            if (format == null) {
                synchronized (patternMap) {
                    format = patternMap.get(anno.format());
                    if (format == null) {
                        format = new SimpleDateFormat(anno.format());
                        patternMap.put(anno.format(), format);
                    }
                }
            }

            try {
                validator.setMin(format.parse(anno.min()));
                validator.setMax(format.parse(anno.max()));
            } catch (ParseException e) {
                throw new ValidationRuntimeException("can't parse date, format should be set", e);
            }
            validator.setMessage(anno.message());
            validator.setFieldName(fieldName);
            return validator;
        }

    }

    public static class DoubleRangeFactory implements AnnotationValidatorFactory<DoubleRange, DoubleRangeFieldValidator> {

        @Override
        public DoubleRangeFieldValidator createValidator(DoubleRange anno, String fieldName) {
            DoubleRangeFieldValidator validator = new DoubleRangeFieldValidator();
            validator.setMaxExclusiveValue(anno.maxExclusive());
            validator.setMaxInclusiveValue(anno.maxInclusive());
            validator.setMinExclusiveValue(anno.minExclusive());
            validator.setMinInclusiveValue(anno.minInclusive());
            validator.setFromAnnotation(true);
            validator.setMessage(anno.message());
            validator.setFieldName(fieldName);
            return validator;
        }

    }

    public static class EmailFactory implements AnnotationValidatorFactory<Email, EmailValidator> {

        @Override
        public EmailValidator createValidator(Email anno, String fieldName) {
            EmailValidator validator = new EmailValidator();
            validator.setMessage(anno.message());
            validator.setTrim(anno.trim());
            validator.setCaseSensitive(anno.caseSensitive());
            validator.setFieldName(fieldName);
            return validator;
        }
    }

    public static class FieldExpressionFactory implements AnnotationValidatorFactory<Expression, FieldExpressionValidator> {

        @Override
        public FieldExpressionValidator createValidator(Expression anno, String fieldName) {
            FieldExpressionValidator validator = new FieldExpressionValidator();
            validator.setExpression(anno.value());
            validator.setMessage(anno.message());
            validator.setFieldName(fieldName);
            return validator;
        }

    }

    public static class IntRangeFactory implements AnnotationValidatorFactory<IntRange, IntRangeFieldValidator> {

        @Override
        public IntRangeFieldValidator createValidator(IntRange anno, String fieldName) {
            IntRangeFieldValidator validator = new IntRangeFieldValidator();
            validator.setMin(anno.min());
            validator.setMax(anno.max());
            validator.setMessage(anno.message());
            validator.setFieldName(fieldName);
            return validator;
        }
    }

    public static class LongRangeFactory implements AnnotationValidatorFactory<LongRange, LongRangeFieldValidator> {

        @Override
        public LongRangeFieldValidator createValidator(LongRange anno, String fieldName) {
            LongRangeFieldValidator validator = new LongRangeFieldValidator();
            validator.setMin(anno.min());
            validator.setMax(anno.max());
            validator.setMessage(anno.message());
            validator.setFieldName(fieldName);
            return validator;
        }
    }

    public static class RegexFactory implements AnnotationValidatorFactory<Regex, RegexFieldValidator> {

        @Override
        public RegexFieldValidator createValidator(Regex anno, String fieldName) {
            RegexFieldValidator validator = new RegexFieldValidator();
            validator.setExpression(anno.value());
            validator.setTrim(anno.trim());
            validator.setCaseSensitive(anno.caseSensitive());
            validator.setMessage(anno.message());
            validator.setFieldName(fieldName);
            return validator;
        }
    }

    public static class RequiredFactory implements AnnotationValidatorFactory<Required, RequiredFieldValidator> {

        @Override
        public RequiredFieldValidator createValidator(Required anno, String fieldName) {
            RequiredFieldValidator validator = new RequiredFieldValidator();
            validator.setMessage(anno.message());
            validator.setFieldName(fieldName);
            return validator;
        }
    }

    public static class StringNotEmptyFactory implements AnnotationValidatorFactory<StringNotEmpty, StringNotEmptyValidator> {

        @Override
        public StringNotEmptyValidator createValidator(StringNotEmpty anno, String fieldName) {
            StringNotEmptyValidator validator = new StringNotEmptyValidator();
            validator.setMessage(anno.message());
            validator.setFieldName(fieldName);
            validator.setTrim(anno.trim());
            return validator;
        }
    }

    public static class StringLengthFactory implements AnnotationValidatorFactory<StringLength, StringLengthFieldValidator> {

        @Override
        public StringLengthFieldValidator createValidator(StringLength anno, String fieldName) {
            StringLengthFieldValidator validator = new StringLengthFieldValidator();
            validator.setMinLength(anno.minLength());
            validator.setMaxLength(anno.maxLength());
            validator.setMessage(anno.message());
            validator.setFieldName(fieldName);
            validator.setTrim(anno.trim());
            return validator;
        }
    }

    public static class URLFactory implements AnnotationValidatorFactory<URL, URLValidator> {

        @Override
        public URLValidator createValidator(URL anno, String fieldName) {
            URLValidator validator = new URLValidator();
            validator.setMessage(anno.message());
            validator.setFieldName(fieldName);
            return validator;
        }
    }

    public static class VisitorFactory implements AnnotationValidatorFactory<Visitor, VisitorFieldValidator> {
        @Override
        public VisitorFieldValidator createValidator(Visitor annotation, String fieldName) {
            VisitorFieldValidator validator = new VisitorFieldValidator();
            Visitor anno = (Visitor) annotation;
            validator.setMessage(anno.message());
            validator.setFieldName(fieldName);
            validator.setPolicy(anno.policy());
            return validator;
        }
    }

    public static class StringInFactory implements AnnotationValidatorFactory<StringIn, StringInValidator> {

        @Override
        public StringInValidator createValidator(StringIn anno, String fieldName) {
            StringInValidator validator = new StringInValidator();
            validator.setMessage(anno.message());
            validator.setFieldName(fieldName);
            validator.setTrim(anno.trim());
            validator.setInString(Arrays.asList(anno.in()));
            return validator;
        }
    }

    public static class StringDateFormatFactory implements AnnotationValidatorFactory<StringDateFormat, StringDateFormatValidator> {

        @Override
        public StringDateFormatValidator createValidator(StringDateFormat annotation, String fieldName) {
            StringDateFormatValidator validator = new StringDateFormatValidator();
            validator.setFieldName(fieldName);
            validator.setMessage(annotation.message());
            validator.setDateFormat(annotation.value());
            return validator;
        }

    }

}
