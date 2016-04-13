import java.util.Map;

import com.meidusa.venus.annotations.RemoteException;
import com.meidusa.venus.util.ClasspathAnnotationScanner;


public class Taaa {

	public static void main(String[] args) {
    	long start = System.currentTimeMillis();
    	Map<Class<?>,RemoteException> map = ClasspathAnnotationScanner.find(Exception.class, RemoteException.class,"com.meidusa.venus");
    	
    	for(Map.Entry<Class<?>,RemoteException> entry : map.entrySet()){
    		System.out.println(entry.getKey());
    	}
    	System.out.println(System.currentTimeMillis() - start);
	}

}
