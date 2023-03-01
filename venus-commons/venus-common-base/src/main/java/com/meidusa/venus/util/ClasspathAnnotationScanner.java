package com.meidusa.venus.util;

import java.lang.annotation.Annotation;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.reflections.Reflections;
import org.reflections.scanners.Scanners;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;

public class ClasspathAnnotationScanner {
    private ClasspathAnnotationScanner(){}
    
    /**
     * 
     * @param parent
     * @param targetAnnotation
     * @return
     */
    public static <K,T extends Annotation> Map<Class<?>,T> find(Class<K> parent,Class<T> targetAnnotation){
    	
    	return find(parent,targetAnnotation,"");
    }
    
    public static <K,T extends Annotation> Map<Class<?>,T> find(Class<K> parent,Class<T> targetAnnotation,String... packageName){
    	Collection<URL> urls = new ArrayList<URL>();
    	if(packageName != null && packageName.length > 0) {
    		for(String name : packageName) {
    			urls.addAll(ClasspathHelper.forPackage(name));
    		}
    	}
    	
    	Reflections reflections = new Reflections(urls,Scanners.TypesAnnotated);
    	
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
