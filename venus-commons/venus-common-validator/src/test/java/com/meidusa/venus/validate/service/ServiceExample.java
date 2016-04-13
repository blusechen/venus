package com.meidusa.venus.validate.service;

import java.util.Date;

import com.meidusa.venus.annotations.Endpoint;
import com.meidusa.venus.annotations.Param;
import com.meidusa.venus.annotations.Service;
import com.meidusa.venus.validate.service.domain.Account;

@Service
public interface ServiceExample {
    @Endpoint
    boolean testDateRange(@Param(name = "dateValue") Date date);

    @Endpoint
    boolean testDoubleRange(@Param(name = "doubleValue") double doubleValue);

    @Endpoint
    boolean testEmail(@Param(name = "emailValue") String email);

    @Endpoint
    boolean testExpression(@Param(name = "val1") int val1, @Param(name = "val2") int[] val2, @Param(name = "val3") String val3);

    @Endpoint
    boolean testFieldExpression(@Param(name = "account") Account account);

    @Endpoint
    boolean testIntRange(@Param(name = "intValue") int intValue);

    @Endpoint
    boolean testLongRange(@Param(name = "longValue") long longValue);

    @Endpoint
    boolean testRegex(@Param(name = "stringValue") String stringValue);

    @Endpoint
    boolean testRequired(@Param(name = "value") String value);

    @Endpoint
    boolean testRequiredString(@Param(name = "stringValue") String value);

    @Endpoint
    boolean testStringLength(@Param(name = "stringValue") String stringValue);

    @Endpoint
    boolean testURL(@Param(name = "url") String url);

    @Endpoint
    boolean testVisitor(@Param(name = "account") Account account);

    @Endpoint
    boolean testStringDateFormat(@Param(name = "requestTime") String requstTime);

}
