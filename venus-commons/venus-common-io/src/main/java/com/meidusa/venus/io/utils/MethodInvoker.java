package com.meidusa.venus.io.utils;

import java.lang.reflect.Method;
import java.util.HashMap;

import com.meidusa.venus.convert.ConvertService;
import com.meidusa.venus.convert.DefaultConvertService;

public class MethodInvoker {
    private static HashMap<Class<?>, HashMap<String, PropertyDescriptor>> classMap = new HashMap<Class<?>, HashMap<String, PropertyDescriptor>>();
    private static ConvertService convertService = new DefaultConvertService();

    private static PropertyDescriptor getPropertyDescriptor(Class<?> clazz, String attr) throws Exception {
        HashMap<String, PropertyDescriptor> descriptors = classMap.get(clazz);
        if (descriptors == null) {
            synchronized (classMap) {
                descriptors = classMap.get(clazz);
                if (descriptors == null) {
                    descriptors = new HashMap<String, PropertyDescriptor>();
                    classMap.put(clazz, descriptors);
                }
            }
        }
        PropertyDescriptor descriptor = descriptors.get(attr);

        if (descriptor == null) {
            synchronized (descriptors) {
                descriptor = descriptors.get(attr);
                if (descriptor == null) {
                    descriptor = new PropertyDescriptor(attr, clazz);
                    descriptors.put(attr, descriptor);
                }
            }
        }

        return descriptor;
    }

    public static Object getProperty(Object obj, String attr) {

        try {
            PropertyDescriptor descriptor = getPropertyDescriptor(obj.getClass(), attr);
            Method method = descriptor.getReadMethod();
            return method.invoke(obj);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void setProperty(Object obj, String attr, Object val) {
        try {
            PropertyDescriptor descriptor = getPropertyDescriptor(obj.getClass(), attr);
            Method method = descriptor.getWriteMethod();
            descriptor.getPropertyType();
            method.invoke(obj, val);
        } catch (Exception e) {
            PropertyDescriptor descriptor;
            try {
                descriptor = getPropertyDescriptor(obj.getClass(), attr);

                Method method = descriptor.getWriteMethod();
                descriptor.getPropertyType();
                Class pType = descriptor.getPropertyType();
                Class vType = val.getClass();
                if (pType != vType) {
                    val = convertService.convert(val.toString(), pType);
                }
                method.invoke(obj, val);

            } catch (Exception e1) {
                e1.printStackTrace();
                System.out.println(obj.getClass() + ": field=" + attr + ",value=" + val);
            }
        }
    }
}
