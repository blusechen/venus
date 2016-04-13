package com.meidusa.venus.io.serializer.decode.writer;

import com.meidusa.venus.io.utils.MethodInvoker;

public class ObjectPropertyWriter implements PropertyWriter {

    public void writeProperty(Object src, String name, Object param) {
        MethodInvoker.setProperty(src, name, param);
    }

    // public static void main(String[] args) {
    // ObjectPropertyWriter writer = new ObjectPropertyWriter();
    // A a = new A();
    // writer.writeProperty(a, "a", null, new Object[] { "abcde" });
    //
    // }

}

// class A {
// private String a;
//
// public String getA() {
// return a;
// }
//
// public void setA(String a) {
// this.a = a;
// }
//
// }
