package com.meidusa.venus.backend.network.handler;

import com.meidusa.venus.annotations.ExceptionCode;
import com.meidusa.venus.annotations.RemoteException;
import com.meidusa.venus.util.ClasspathAnnotationScanner;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by godzillahua on 7/4/16.
 */
public class CodeMapScanner {
    private static Map<Class<?>, Integer> codeMap = new HashMap<Class<?>, Integer>();

    static {
        Map<Class<?>, ExceptionCode> map = ClasspathAnnotationScanner.find(Exception.class, ExceptionCode.class);
        if (map != null) {
            for (Map.Entry<Class<?>, ExceptionCode> entry : map.entrySet()) {
                codeMap.put(entry.getKey(), entry.getValue().errorCode());
            }
        }

        Map<Class<?>, RemoteException> rmap = ClasspathAnnotationScanner.find(Exception.class, RemoteException.class);

        if (rmap != null) {
            for (Map.Entry<Class<?>, RemoteException> entry : rmap.entrySet()) {
                codeMap.put(entry.getKey(), entry.getValue().errorCode());
            }
        }
    }

    public static Map<Class<?>, Integer> getCodeMap() {
        return codeMap;
    }
}
