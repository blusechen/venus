package com.meidusa.venus.validate.expression;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;

public class PolicyExpression {

    private static final Pattern notPattern = Pattern.compile("\\!\\(([^\\}]+)\\)");

    public static boolean match(String policy, String policyExpression) {
        if (policyExpression.equals("")) {
            return true;
        }
        if (policy.equals("")) {
            return true;
        }
        policyExpression = StringUtils.remove(policyExpression, ' ');
        Matcher matcher = notPattern.matcher(policyExpression);
        String[] notExpression = null;
        String[] inExpression = null;
        if (matcher.find()) {
            String notExpressions = matcher.group(1);
            notExpression = splitStr(notExpressions);
        } else if (policyExpression.trim().startsWith("!")) {
            notExpression = new String[1];
            notExpression[0] = policyExpression.substring(1, policyExpression.length());
        } else {
            inExpression = splitStr(policyExpression);
        }
        if (notExpression != null) {
            boolean notExpressionMatch = false;
            for (int i = 0; i < notExpression.length; i++) {
                if (policy.equals(notExpression[i])) {
                    notExpressionMatch = true;
                    break;
                }
            }
            if (notExpressionMatch) {
                return false;
            }
        }
        if (inExpression != null) {
            for (int i = 0; i < inExpression.length; i++) {
                if (policy.equals(inExpression[i])) {
                    return true;
                }
            }
        }
        return false;
    }

    private static String[] splitStr(String str) {
        return StringUtils.split(str, '|');
    }

    public static void main(String[] args) {
        System.out.println(PolicyExpression.match("AAA", "!AAA"));
    }
}
