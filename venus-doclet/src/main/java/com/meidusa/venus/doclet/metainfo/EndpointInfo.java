/**
 * 
 */
package com.meidusa.venus.doclet.metainfo;

/**
 * @author gaoyong
 *
 */
public class EndpointInfo {
	
	private String endpointName;
	
	private String async;
	
	private String returnType;

	public String getEndpointName() {
		return endpointName;
	}

	public void setEndpointName(String endpointName) {
		this.endpointName = endpointName;
	}

	public String getAsync() {
		return async;
	}

	public void setAsync(String async) {
		this.async = async;
	}

	public void setReturnType(String returnType) {
		this.returnType = returnType;
	}

	public String getReturnType() {
		return returnType;
	}

	
}
