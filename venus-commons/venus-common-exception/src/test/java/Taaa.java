import java.util.Map;
import java.util.Set;

import org.reflections.Reflections;
import org.reflections.scanners.Scanners;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.scanners.TypeAnnotationsScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.util.FilterBuilder;

import com.meidusa.venus.annotations.RemoteException;
import com.meidusa.venus.util.ClasspathAnnotationScanner;


public class Taaa {

	public static void main(String[] args) {
    	long start = System.currentTimeMillis();
    	Map<Class<?>,RemoteException> map = ClasspathAnnotationScanner.find(Exception.class, RemoteException.class,"");
    	
    	for(Map.Entry<Class<?>,RemoteException> entry : map.entrySet()){
    		System.out.println(entry.getKey());
    	}
    	System.out.println(System.currentTimeMillis() - start);
    	
    	Reflections reflections = new Reflections(ClasspathHelper.forPackage("com.meidusa.venus"),ClasspathHelper.forPackage("com.hexnova"));
    	
        Set<Class<?>> annotated =  reflections.getTypesAnnotatedWith(RemoteException.class);
        
        for(Class<?> entry : annotated){
    		System.out.println(entry);
    	}
	}

}
