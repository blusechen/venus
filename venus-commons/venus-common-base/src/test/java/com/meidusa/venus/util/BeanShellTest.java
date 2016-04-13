package com.meidusa.venus.util;

import java.util.Date;

import bsh.EvalError;
import bsh.Interpreter;

public class BeanShellTest {

    /**
     * @param args
     * @throws EvalError
     */
    public static void main(String[] args) throws EvalError {
        Interpreter i = new Interpreter(); // Construct an interpreter
        long start = System.currentTimeMillis();
        for (int j = 0; j < 1; j++) {

            i.set("foo", 5); // Set variables
            i.set("date", new Date());

            Date date = (Date) i.get("date"); // retrieve a variable

            // Eval a statement and get the result
            i.eval("foo*10");
            // System.out.println( i.get("bar") );
            i.unset("foo");
            i.unset("date");
        }
        System.out.println((System.currentTimeMillis() - start) + "ms");
        System.out.println(33 >>> 1);
    }

}
