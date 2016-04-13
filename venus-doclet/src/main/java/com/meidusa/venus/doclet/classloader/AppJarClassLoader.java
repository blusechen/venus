/**
 * 
 */
package com.meidusa.venus.doclet.classloader;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

/**
 * @author gaoyong
 * 
 */
public class AppJarClassLoader extends URLClassLoader {

	private static AppJarClassLoader appJarClassLoader;
	
	private AppJarClassLoader(URL[] urls, ClassLoader parent) {
		super(urls, parent);
	}

	/**
	 * if in concurrent env  has risk
	 * @param urls
	 * @param parent
	 * @return
	 */
	public static AppJarClassLoader getAppJarClassLoader(URL[] urls,
			ClassLoader parent) {
		if(appJarClassLoader==null){
			appJarClassLoader = new AppJarClassLoader(urls,parent);
		}
		return appJarClassLoader ;
	}
	
	public static void main(String[] args) throws MalformedURLException, ClassNotFoundException{
		URL[] urls = {new File("/home/gaoyong/mail2.0_Refactoring/service-doclet/target").toURI().toURL()};
		AppJarClassLoader cl = AppJarClassLoader.getAppJarClassLoader(urls, Thread.currentThread().getContextClassLoader());
		cl.loadClass("com.meidusa.venus.hello.api.HelloService");
	}

}
