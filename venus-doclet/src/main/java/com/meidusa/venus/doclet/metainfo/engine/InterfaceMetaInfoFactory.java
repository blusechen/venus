/**
 * 
 */
package com.meidusa.venus.doclet.metainfo.engine;

import java.lang.reflect.Method;

/**
 * @author gaoyong
 * @param <T>
 *
 */
public interface InterfaceMetaInfoFactory {
	
	public byte[] createFunctionMetaInfo(@SuppressWarnings("rawtypes") Class clazz,Method method);
	
	public byte[] createProcedureMetaInfo(@SuppressWarnings("rawtypes") Class clazz,String callee);
}
