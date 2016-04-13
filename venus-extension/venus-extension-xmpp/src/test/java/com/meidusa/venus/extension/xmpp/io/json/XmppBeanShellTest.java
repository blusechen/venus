package com.meidusa.venus.extension.xmpp.io.json;

import org.xmpp.packet.Message;

import com.meidusa.toolkit.util.TimeUtil;

import bsh.EvalError;
import bsh.Interpreter;

public class XmppBeanShellTest {

    /**
     * @param args
     * @throws EvalError
     */
    public static void main(String[] args) throws EvalError {
        Message message = new Message();
        message.setTo("13100000000@mim.snda");
        message.setFrom("mim.snda");
        message.setBody("asdfqwerqwer阿斯顿发达");
        Interpreter i = new Interpreter(); // Construct an interpreter
        long start = TimeUtil.currentTimeMillis();
        for (int j = 0; j < 100; j++) {
            i.set("message", message); // Set variables
            System.out.println(i.eval("message.to.node"));
            i.unset("message");
        }
        System.out.println((TimeUtil.currentTimeMillis() - start) + "ms");
    }

}
