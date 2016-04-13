package com.meidusa.venus.util;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.reflections.Reflections;

public class ClasspathAnnotationScanner {
    private ClasspathAnnotationScanner(){}
    
    /**
     * 
     * @param parent
     * @param targetAnnotation
     * @return
     */
    public static <K,T extends Annotation> Map<Class<?>,T> find(Class<K> parent,Class<T> targetAnnotation){
    	
    	return find(parent,targetAnnotation,null);
    }
    
    public static <K,T extends Annotation> Map<Class<?>,T> find(Class<K> parent,Class<T> targetAnnotation,String packageName){
    	Reflections reflections = new Reflections(packageName);
    	
        Set<Class<?>> annotated =  reflections.getTypesAnnotatedWith(targetAnnotation);
        
        Map<Class<?>,T> map = new HashMap<Class<?>,T>();
        for(Class<?> clazz : annotated){
	        T code = clazz.getAnnotation(targetAnnotation);
	        
	        if(parent != null){
	        	if(!parent.isAssignableFrom(clazz)){
	        		continue;
	        	}
	        }
	        
	        if(code != null){
	        	map.put(clazz,code);
	        }
        }
        return map;
    }

}
