package com.meidusa.venus.io.asm.test;

public class Inner {
    private String a;
    private int b;

    public Inner() {
        super();
    }

    public String getA() {
        return a;
    }

    public void setA(String a) {
        this.a = a;
    }

    public int getB() {
        return b;
    }

    public void setB(int b) {
        this.b = b;
    }

    public Inner(String a, int b) {
        super();
        this.a = a;
        this.b = b;
    }

}
