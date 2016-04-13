package com.meidusa.venus.validate;

import java.security.PrivilegedAction;

public class ASMClassLoader extends ClassLoader {

    private static java.security.ProtectionDomain DOMAIN;

    static {
        DOMAIN = (java.security.ProtectionDomain) java.security.AccessController.doPrivileged(new PrivilegedAction<Object>() {
            public Object run() {
                return ASMClassLoader.class.getProtectionDomain();
            }
        });
    }

    public ASMClassLoader() {
        super(ASMClassLoader.class.getClassLoader());
    }

    public Class<?> defineClassPublic(String name, byte[] b, int off, int len) throws ClassFormatError {
        Class<?> clazz = defineClass(name, b, off, len, DOMAIN);

        return clazz;
    }

    public static Class<?> forName(String className) throws ClassNotFoundException {
        try {
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            return classLoader.loadClass(className);
        } catch (ClassNotFoundException e) {
            throw e;
        }
    }
}
