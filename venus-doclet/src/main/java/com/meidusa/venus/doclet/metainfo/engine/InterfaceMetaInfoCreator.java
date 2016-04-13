/**
 * 
 */
package com.meidusa.venus.doclet.metainfo.engine;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.meidusa.venus.annotations.Param;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.CtField;
import javassist.CtMethod;
import javassist.CtNewMethod;
import javassist.Modifier;
import javassist.NotFoundException;
import javassist.bytecode.AttributeInfo;
import javassist.bytecode.CodeAttribute;
import javassist.bytecode.LocalVariableAttribute;
import javassist.bytecode.MethodInfo;
import javassist.bytecode.SignatureAttribute;

/**
 * @author gaoyong
 * 
 */
@SuppressWarnings("unused")
public class InterfaceMetaInfoCreator  implements InterfaceMetaInfoFactory{

	private static final CtClass[] NO_ARGS = {};
	private static ClassPool classPool = ClassPool.getDefault();
	protected static Log log = LogFactory.getLog("com.meidusa.venus.hello.metainfo.InterfaceMetaInfoCreator");

	public byte[] createFunctionMetaInfo(Class sourceClazz, Method method) {
		
		CtClass metaInfoClazz = classPool.makeClass(createClassName(
				sourceClazz, method.getName()));
		
		genConstructor(metaInfoClazz);
		genFields(metaInfoClazz, method);

		return null;
	}

	public byte[] createProcedureMetaInfo(Class sourceClazz, String callee) {
		// TODO Auto-generated method stub
		return null;
	}

	private String createClassName(Class sourceClazz, String method) {
		StringBuffer bf = new StringBuffer();
		bf.append(sourceClazz.getName());
		bf.append("2");
		bf.append(method);
		System.out.println(bf.toString());
		return bf.toString();
	}

	private void genConstructor(CtClass metaInfoClazz) {
		CtConstructor constructor = new CtConstructor(NO_ARGS, metaInfoClazz);
		try {
			constructor.setBody("{}");
			metaInfoClazz.addConstructor(constructor);
		} catch (CannotCompileException e) {
			log.error("Can't make constructor : "+e.getMessage());
		}
	}

	private void genFields(CtClass metaInfoClazz, Method method) {
		Type[] gtps = method.getGenericParameterTypes();
		Annotation[][] annotations = method.getParameterAnnotations();

		for (int i = 0; i < gtps.length; i++) {
			String fieldName = createFieldName(annotations[i][0]);
			CtClass fieldType;
			
			Type paramType = gtps[i];
			
            if (paramType instanceof ParameterizedType) {
				ParameterizedType pType = (ParameterizedType) paramType;
				
				try {
					fieldType = classPool.get(createFieldTypeName(pType.getRawType().toString()));
					createField(metaInfoClazz,fieldType,fieldName);
				} catch (NotFoundException e) {
					log.error("Can't find fieldType in classPool :"+e.getMessage());
				}
				
				Type[] types = pType.getActualTypeArguments();
				for (Type type : types) {
					try {
						fieldType = classPool.get(createFieldTypeName(type.toString()));
						createField(metaInfoClazz,fieldType,fieldName);
					} catch (NotFoundException e) {
						log.error("Can't find fieldType in classPool :"+e.getMessage());
					}
				}
			} else {
				//System.out.println(paramType);
				try {
					fieldType = classPool.get(createFieldTypeName(paramType.toString()));
					createField(metaInfoClazz,fieldType,fieldName);
					
				} catch (NotFoundException e) {
					log.error("Can't find fieldType in classPool :"+e.getMessage());
				}
			}

		}

	}
	
	private String createFieldName(Annotation an){
		Param realAn = (Param) an;
        return realAn.name();
	}
	
	private String createFieldTypeName(String typeName){
		return typeName.substring(typeName.indexOf(' ')+1);
	}

	private void createField(CtClass metaInfoClazz, CtClass fieldType ,String paramName) {
		try {
			CtField field = new CtField(fieldType, paramName, metaInfoClazz);
			CtNewMethod.getter("getTest", field);
			metaInfoClazz.addField(field);
		} catch (CannotCompileException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	// private void makeFields

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		/*InterfaceMetaInfoCreator i = new InterfaceMetaInfoCreator();
		Class[] parameterTypes = { String.class, InvocationListener.class };
		Method m;
		try {
			m = HelloService.class.getDeclaredMethod("sayHello", parameterTypes);
			i.createFunctionMetaInfo(HelloService.class, m);
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/

	}

}
