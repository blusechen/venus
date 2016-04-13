/**
 * 
 */
package com.meidusa.venus.doclet.engine;

import java.util.Properties;

import org.apache.velocity.app.Velocity;

/**
 * @author gaoyong
 *
 */
public class DocletConfig {
	
	public static final String TEMPLATE_PATH = "/home/gaoyong/mail2.0_Refactoring/velocitytest";
	
	public static final String TEMPLATE_NAME = "api_template.vm";
	
	public static final String OUTPUT_FILE = "/home/gaoyong/mail2.0_Refactoring/velocitytest/testtmp.html";
	
	public static final Properties properties = new Properties();
	static {
		properties.setProperty(Velocity.FILE_RESOURCE_LOADER_PATH,TEMPLATE_PATH);
		properties.setProperty(Velocity.ENCODING_DEFAULT, "UTF-8");
		properties.setProperty(Velocity.INPUT_ENCODING, "UTF-8");
		properties.setProperty(Velocity.OUTPUT_ENCODING, "UTF-8");
		/*
		 * p.setProperty(Velocity.ENCODING_DEFAULT, "GBK");
		 * p.setProperty(Velocity.INPUT_ENCODING, "GBK");
		 * p.setProperty(Velocity.OUTPUT_ENCODING, "GBK");
		 */
		
	}
}
