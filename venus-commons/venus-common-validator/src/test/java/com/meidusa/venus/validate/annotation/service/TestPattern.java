package com.meidusa.venus.validate.annotation.service;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TestPattern {
    public static void main(String[] args) {
        Pattern p = Pattern.compile("\\%\\{([^\\}]+)\\}");
        Matcher m = p.matcher("one %{cat} two %{cat} in the yard");
        StringBuffer sb = new StringBuffer();
        while (m.find()) {
            m.appendReplacement(sb, "dog");
        }
        m.appendTail(sb);
        System.out.println(sb.toString());

    }
}
