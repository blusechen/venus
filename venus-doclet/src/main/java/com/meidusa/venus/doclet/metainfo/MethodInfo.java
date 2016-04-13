/**
 * 
 */
package com.meidusa.venus.doclet.metainfo;

import java.util.List;

/**
 * @author gaoyong
 * 
 */
public class MethodInfo {

	private String interfaceName;

	private String invokeStyle;

	private String desc;

	private String returnType;
	
	private List<ParamInfo> paramList;

	private EndpointParameterInfo[] params;

	public String getInterfaceName() {
		return interfaceName;
	}

	public void setInterfaceName(String interfaceName) {
		this.interfaceName = interfaceName;
	}

	public String getInvokeStyle() {
		return invokeStyle;
	}

	public void setInvokeStyle(String invokeStyle) {
		this.invokeStyle = invokeStyle;
	}

	public String getDesc() {
		return desc;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}

	public String getReturnType() {
		return returnType;
	}

	public void setReturnType(String returnType) {
		this.returnType = returnType;
	}

	public EndpointParameterInfo[] getParams() {
		return params;
	}

	public void setParams(EndpointParameterInfo[] params) {
		this.params = params;
	}

	public List<ParamInfo> getParamList() {
		return paramList;
	}

	public void setParamList(List<ParamInfo> paramList) {
		this.paramList = paramList;
	}
}
