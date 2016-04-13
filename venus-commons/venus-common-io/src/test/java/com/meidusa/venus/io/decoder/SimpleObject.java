package com.meidusa.venus.io.decoder;

import java.util.Date;
import java.util.Map;

public class SimpleObject {
    private int a;
    private String b;
    private Date c;
    private Long d;
    private Map<String, Integer> e;

    public int getA() {
        return a;
    }

    public void setA(int a) {
        this.a = a;
    }

    public String getB() {
        return b;
    }

    public void setB(String b) {
        this.b = b;
    }

    public Date getC() {
        return c;
    }

    public void setC(Date c) {
        this.c = c;
    }

    public Long getD() {
        return d;
    }

    public void setD(Long d) {
        this.d = d;
    }

    public Map<String, Integer> getE() {
        return e;
    }

    public void setE(Map<String, Integer> e) {
        this.e = e;
    }

    @Override
    public String toString() {
        return "SimpleObject [a=" + a + ", b=" + b + ", c=" + c + ", d=" + d + ", e=" + e + "]";
    }

}
