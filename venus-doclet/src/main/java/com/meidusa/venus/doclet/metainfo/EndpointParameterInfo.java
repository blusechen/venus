/**
 * 
 */
package com.meidusa.venus.doclet.metainfo;

/**
 * @author gaoyong
 *
 */
public class EndpointParameterInfo {

	private String name;
	private String type;
	private String defaultValue;
	private String optional;
	
	private String paramComent;
	
	
	public String getParamComent() {
		return paramComent;
	}
	public void setParamComent(String paramComent) {
		this.paramComent = paramComent;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getDefaultValue() {
		return defaultValue;
	}
	public void setDefaultValue(String defaultValue) {
		this.defaultValue = defaultValue;
	}
	public String getOptional() {
		return optional;
	}
	public void setOptional(String optional) {
		this.optional = optional;
	}
	
	
}
