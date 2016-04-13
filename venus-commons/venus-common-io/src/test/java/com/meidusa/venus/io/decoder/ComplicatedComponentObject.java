package com.meidusa.venus.io.decoder;

public class ComplicatedComponentObject {
    private String a;
    private Integer b;

    public String getA() {
        return a;
    }

    public void setA(String a) {
        this.a = a;
    }

    public Integer getB() {
        return b;
    }

    public void setB(Integer b) {
        this.b = b;
    }

    @Override
    public String toString() {
        return "ComplicatedComponentObject [a=" + a + ", b=" + b + "]";
    }

    public ComplicatedComponentObject(String a, Integer b) {
        super();
        this.a = a;
        this.b = b;
    }

    public ComplicatedComponentObject() {
    };

}
