/**
 * 
 */
package com.meidusa.venus.util;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * utility methods
 * 
 * @author Sun Ning
 * @since 2010-3-5
 */
public class Utils {

    private static Map<Class, Map<String, Type>> typeMap = new HashMap<Class, Map<String, Type>>();

    private static Map<Class, Map<String, PropertyDescriptor>> pdMap = new HashMap<Class, Map<String, PropertyDescriptor>>();

    /**
     * 
     * @param s1
     * @param s2
     * @return
     */
    public static boolean arrayEquals(String[] s1, String[] s2) {
        String[] s1Copy = Arrays.copyOf(s1, s1.length);
        String[] s2Copy = Arrays.copyOf(s2, s2.length);

        Arrays.sort(s1Copy);
        Arrays.sort(s2Copy);

        return Arrays.equals(s1Copy, s2Copy);
    }

    /**
     * test if all items in parent also appears in child
     * 
     * @param parent
     * @param child
     * @return
     */
    private static boolean arrayContains1(String[] parent, String[] child) {
        if (child.length == 0) {
            return true;
        }

        String[] parentCopy = Arrays.copyOf(parent, parent.length);
        String[] childCopy = Arrays.copyOf(child, child.length);

        Arrays.sort(parentCopy);
        Arrays.sort(childCopy);

        int i = 0, j = 0;

        while (i < parentCopy.length && j < childCopy.length) {
            if (childCopy[j].equals(parentCopy[i])) {
                i++;
                j++;
            } else {
                j++;
            }
        }

        if (i == parentCopy.length) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 
     * @param <T>
     * @param original
     * @param predication
     * @return
     */
    public static <T> T[] subArray(T[] original, Predication<T> predication) {
        ArrayList<T> container = new ArrayList<T>(original.length);
        for (T item : original) {
            if (predication.predict(item)) {
                container.add(item);
            }
        }
        return container.toArray(Arrays.copyOf(original, 0));
    }

    /**
     * 
     * @author Sun Ning
     * 
     * @param <T>
     */
    public static interface Predication<T> {
        public boolean predict(T item);
    }

    /**
     * 
     * @param map
     * @return
     */
    public static String toString(Map<?, ?> map) {
        if (map == null || map.size() == 0) {
            return "{ }";
        }

        StringBuffer sb = new StringBuffer();

        sb.append("{");

        Iterator it = map.entrySet().iterator();

        while (it.hasNext()) {
            Map.Entry entry = (Map.Entry) it.next();
            sb.append(entry.getKey());
            sb.append(":");
            sb.append(entry.getValue());
            if (it.hasNext()) {
                sb.append(",");
            }
        }

        sb.append("}");

        return sb.toString();
    }

    /**
     * test if all items in parent also appears in child child array will be sorted
     * 
     * @param parent
     * @param child
     * @return
     */
    public static boolean arrayContains(String[] parent, String[] child) {
        if (parent == null || parent.length == 0) {
            return true;
        }

        Arrays.sort(child);
        for (String i : parent) {
            if (Arrays.binarySearch(child, i) < 0) {
                return false;
            }
        }
        return true;

    }

    
    /**
     * 
     * @param clazz
     * @return
     */
    public static Map<String, PropertyDescriptor> getBeanPropertyDescriptor(Class<?> clazz) {
        Map<String, PropertyDescriptor> pdm = pdMap.get(clazz);
        if (pdm == null) {
            synchronized (pdMap) {
                pdm = pdMap.get(clazz);
                if (pdm == null) {
                    pdm = new HashMap<String, PropertyDescriptor>();
                    pdMap.put(clazz, pdm);
                    try {
                        BeanInfo info = Introspector.getBeanInfo(clazz, Throwable.class);
                        PropertyDescriptor[] pds = info.getPropertyDescriptors();

                        for (PropertyDescriptor pd : pds) {
                            if (pd.getReadMethod() != null && pd.getWriteMethod() != null) {
                                pdm.put(pd.getName(), pd);
                            }
                        }
                    } catch (IntrospectionException e) {
                    }
                }
            }
        }
        return pdm;
    }

    public static Map<String, Type> getBeanFieldType(Class<?> clazz, Class stopClazz) {
        Map<String, Type> tmap = typeMap.get(clazz);
        if (tmap == null) {
            synchronized (typeMap) {
                tmap = typeMap.get(clazz);
                if (tmap == null) {
                    tmap = new HashMap<String, Type>();
                    typeMap.put(clazz, tmap);
                    Map<String, PropertyDescriptor> pdm = getBeanPropertyDescriptor(clazz);
                    Class superClazz = clazz;
                    while (superClazz != stopClazz && superClazz != Object.class) {
                        Field[] fields = superClazz.getDeclaredFields();
                        for (Field field : fields) {
                            PropertyDescriptor pd = pdm.get(field.getName());
                            if (pd != null) {
                                tmap.put(field.getName(), field.getGenericType());
                            }
                        }

                        superClazz = superClazz.getSuperclass();
                    }

                }
            }
        }
        return tmap;
    }

    public static void main(String[] args) {
        String[] s1 = new String[] { "name", "hello", "abcd", "qwer", "qwerqweraa", "maf3", "123" };
        String[] s2 = new String[] { "hdkje", "abcd", "qwer", "ouidb", "qwerqweraa", "heoidyhh", "maf3", "837dbb", "123", "name", "hello" };
        boolean result = false;
        result = arrayContains1(s1, s2);

        long start = System.currentTimeMillis();
        for (int i = 0; i < 1000000; i++) {
            result = arrayContains1(s1, s2);
        }
        System.out.println("result=" + result + "," + (System.currentTimeMillis() - start));

        result = arrayContains(s1, s2);

        start = System.currentTimeMillis();
        for (int i = 0; i < 1000000; i++) {
            result = arrayContains(s1, s2);
        }
        System.out.println("result=" + result + "," + (System.currentTimeMillis() - start));
    }
}
