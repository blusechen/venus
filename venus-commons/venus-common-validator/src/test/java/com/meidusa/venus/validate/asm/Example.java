package com.meidusa.venus.validate.asm;

import java.util.Date;

import com.meidusa.venus.validate.validator.annotation.DateRange;
import com.meidusa.venus.validate.validator.annotation.Expression;
import com.meidusa.venus.validate.validator.annotation.IntRange;
import com.meidusa.venus.validate.validator.annotation.StringLength;

@Expression("a!=null")
public class Example {

    @StringLength(minLength = 1, maxLength = 10)
    private String a;
    @DateRange(min = "1965-02-01", max = "2012-02-02")
    private Date b;
    @IntRange(min = 1, max = 5)
    private int c;

    public String getA() {
        return a;
    }

    public void setA(String a) {
        this.a = a;
    }

    public Date getB() {
        return b;
    }

    public void setB(Date b) {
        this.b = b;
    }

    public int getC() {
        return c;
    }

    public void setC(int c) {
        this.c = c;
    }

}
