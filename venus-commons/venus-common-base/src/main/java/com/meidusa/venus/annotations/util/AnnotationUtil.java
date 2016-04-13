/**
 * 
 */
package com.meidusa.venus.annotations.util;

import java.lang.annotation.Annotation;
import java.util.ArrayList;

/**
 * utility methods
 * 
 * @author Sun Ning
 * @since 2010-3-5
 */
public class AnnotationUtil {

    /**
     * find an annotation from an array
     * 
     * @param <T>
     * @param annotaions
     * @param T
     * @return
     */
    @SuppressWarnings("unchecked")
    public static <T> T getAnnotation(Annotation[] annotaions, Class<? extends Annotation> T) {

        for (int i = 0; i < annotaions.length; i++) {
            if (annotaions[i].annotationType().equals(T)) {
                return (T) annotaions[i];
            }
        }

        return null;
    }

    /**
     * 
     * @param classes
     * @param annotation
     * @return
     */
    public static Class<?> getAnnotatedClass(Class<?>[] classes, Class<? extends Annotation> annotation) {
        for (int i = 0; i < classes.length; i++) {
            if (classes[i].isAnnotationPresent(annotation)) {
                return classes[i];
            }
        }

        return null;
    }

    public static Class<?>[] getAnnotatedClasses(Class<?>[] classes, Class<? extends Annotation> annotation) {
        ArrayList<Class<?>> annotatedClasses = new ArrayList<Class<?>>();
        for (int i = 0; i < classes.length; i++) {
            if (classes[i].isAnnotationPresent(annotation)) {
                annotatedClasses.add(classes[i]);
            }
        }
        return annotatedClasses.toArray(new Class[0]);
    }

}
