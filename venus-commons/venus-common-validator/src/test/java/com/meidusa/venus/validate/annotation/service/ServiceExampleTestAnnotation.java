package com.meidusa.venus.validate.annotation.service;

import java.util.Date;
import java.util.Map;

import com.meidusa.venus.annotations.Endpoint;
import com.meidusa.venus.annotations.Param;
import com.meidusa.venus.annotations.Service;
import com.meidusa.venus.validate.annotation.service.domain.AccountTestAnnotation;
import com.meidusa.venus.validate.validator.annotation.DateRange;
import com.meidusa.venus.validate.validator.annotation.DoubleRange;
import com.meidusa.venus.validate.validator.annotation.Email;
import com.meidusa.venus.validate.validator.annotation.Expression;
import com.meidusa.venus.validate.validator.annotation.IntRange;
import com.meidusa.venus.validate.validator.annotation.LongRange;
import com.meidusa.venus.validate.validator.annotation.Regex;
import com.meidusa.venus.validate.validator.annotation.Required;
import com.meidusa.venus.validate.validator.annotation.StringDateFormat;
import com.meidusa.venus.validate.validator.annotation.StringIn;
import com.meidusa.venus.validate.validator.annotation.StringLength;
import com.meidusa.venus.validate.validator.annotation.StringNotEmpty;
import com.meidusa.venus.validate.validator.annotation.URL;
import com.meidusa.venus.validate.validator.annotation.Visitor;

@Service
public interface ServiceExampleTestAnnotation {
    @Endpoint
    boolean testDateRange(@Param(name = "dateValue") @DateRange(min = "2011-01-01", max = "2011-03-03", format = "yyyy-MM-dd") Date date);

    @Endpoint
    boolean testDoubleRange(@Param(name = "doubleValue") @DoubleRange(minInclusive = 1.00, maxInclusive = 1.360) double doubleValue);

    @Endpoint
    boolean testEmail(@Param(name = "emailValue") @Email String email);

    @Expression(" val1 > 0 && val2[1] > 1 && val3==\"abc\" ")
    @Endpoint
    boolean testExpression(@Param(name = "val1") int val1, @Param(name = "val2") int[] val2, @Param(name = "val3") String val3);

    @Endpoint
    boolean testFieldExpression(@Param(name = "account") @Expression(" id < 10000 && city!=\"heaven\"") AccountTestAnnotation account);

    @Endpoint
    boolean testIntRange(@Param(name = "intValue") @IntRange(min = 1, max = 100) int intValue);

    @Endpoint
    boolean testLongRange(@Param(name = "longValue") @LongRange(min = 1, max = 100) long longValue);

    @Endpoint
    boolean testRegex(@Param(name = "stringValue") @Regex(value = "\\%\\{([^\\}]+)\\}") String stringValue);

    @Endpoint
    boolean testRequired(@Param(name = "value") @Required String value);

    @Endpoint
    boolean testRequiredString(@Param(name = "stringValue") @StringNotEmpty String value);

    @Endpoint
    boolean testStringLength(@Param(name = "stringValue") @StringLength(minLength = 10, maxLength = 20) String stringValue);

    @Endpoint
    boolean testURL(@Param(name = "url") @URL String url);

    @Endpoint
    boolean testVisitor(@Param(name = "account") @Visitor AccountTestAnnotation account);

    @Endpoint
    boolean testPolicy(@Param(name = "account") @Visitor(policy = "COUNTRY_NEED") AccountTestAnnotation account);

    @Endpoint
    boolean testMapVisitor(@Param(name = "account") @Visitor Map<String, AccountTestAnnotation> account);

    @Endpoint
    boolean testStringIn(@Param(name = "stringValue") @StringIn(in = { "aaa", "nnn", "ccc" }) String stringValue);

    @Endpoint
    boolean testStringDateFormat(@Param(name = "requestTime") @StringDateFormat("yyyyMMddHHmmss") String requstTime);

}
