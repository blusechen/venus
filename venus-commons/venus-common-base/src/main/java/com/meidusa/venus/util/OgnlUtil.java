package com.meidusa.venus.util;

import java.util.HashMap;
import java.util.Map;

import ognl.Ognl;
import ognl.OgnlException;

/**
 * Used for OgnlBasicValueHolder to do expression computation.
 * 
 * @author lichencheng.daisy
 * @since 1.0.0-SNAPSHOT
 * 
 */
public class OgnlUtil {

    private static Map<String, Object> expressionMap = new HashMap<String, Object>();;

    private static Object getExpression(String expr) throws OgnlException {
        Object parsedExpression = expressionMap.get(expr);
        if (parsedExpression == null) {

            synchronized (expr) {
                parsedExpression = expressionMap.get(expr);
                if (parsedExpression == null) {
                    parsedExpression = Ognl.parseExpression(expr);
                    if (parsedExpression != null) {
                        expressionMap.put(expr, parsedExpression);
                    }
                }
            }
        }
        return parsedExpression;
    }

    public static void setValue(Object root, String expr, Object value) throws OgnlException {
        Object parsedExpression = getExpression(expr);
        Ognl.setValue(parsedExpression, root, value);

    }

    public static String findString(Object root, String expr) throws OgnlException {
        Object parsedExpression = getExpression(expr);
        return String.valueOf(Ognl.getValue(parsedExpression, root));
    }

    public static Object findValue(Object root, String expr) throws OgnlException {
        Object parsedExpression = getExpression(expr);
        return Ognl.getValue(parsedExpression, root);
    }

    public static Object findValue(Object root, String expr, Class<?> asType) throws OgnlException {
        Object parsedExpression = getExpression(expr);
        return Ognl.getValue(parsedExpression, root, asType);
    }
}
