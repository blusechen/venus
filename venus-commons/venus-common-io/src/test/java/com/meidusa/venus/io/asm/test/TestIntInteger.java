package com.meidusa.venus.io.asm.test;

import org.junit.Ignore;

@Ignore
public class TestIntInteger {

    public static void printInt(int i) {
        System.out.println(i);
    }

    public static void printInteger(Integer i) {
        System.out.println(i);
    }

    public static void main(String[] args) {
        Integer i1 = 1;
        int i2 = 1;
        printInt(i1);
        printInteger(i2);
    }
}
