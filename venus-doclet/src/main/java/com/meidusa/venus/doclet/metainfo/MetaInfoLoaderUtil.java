package com.meidusa.venus.doclet.metainfo;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import com.meidusa.venus.annotations.Endpoint;
import com.meidusa.venus.annotations.Param;
import com.meidusa.venus.annotations.Service;
import com.meidusa.venus.annotations.util.AnnotationUtil;


/**
 * 閻╊喚娈戦弰顖欒礋娴滃棜骞忛崣鏍ㄥ复閸欙絿娈戞稉锟界昂Annotation閻ㄥ嫬甯慨瀣╀繆閹拷 * @author gaoyong
 *
 */
public class MetaInfoLoaderUtil {

	/**
	 * 閼惧嘲绶遍幒銉ュ經閻拷{@link Service} Annotation娣団剝浼�
	 * @param clazz
	 */
	public static ServiceInfo getServiceMetaInfo(Class clazz) {
		Annotation[] annotations = clazz.getAnnotations();
		ServiceInfo[] serviceInfos = new ServiceInfo[annotations.length];
		for (int i = 0; i < annotations.length; i++) {
			Service s = (Service) annotations[i];
			ServiceInfo serviceInfo = new ServiceInfo();
			serviceInfo.setShortClassName(s.name());
			serviceInfo.setVersion(String.valueOf(s.version()));
			serviceInfo.setImplement(s.implement());
			if(s.singleton()){
				serviceInfo.setSingleton("yes");
			}
			else{
				serviceInfo.setSingleton("no");
			}
			serviceInfos[i] = serviceInfo;
		}
		return serviceInfos[0];
	}

	/**
	 * 閼惧嘲绶遍幒銉ュ經閻拷{@link Endpoint} 閻拷Annotation娣団剝浼�
	 * @param method
	 */
	public static EndpointInfo getEndpointMetaInfo(Method method) {
		String returnType = method.getReturnType().toString();
		Annotation[] annotations = method.getDeclaredAnnotations();
		EndpointInfo[] endpointInfos = new EndpointInfo[annotations.length];
		for (int i = 0; i < annotations.length; i++) {
			Endpoint e = (Endpoint) annotations[i];
			EndpointInfo endpointInfo = new EndpointInfo();
			endpointInfo.setEndpointName(e.name());
			endpointInfo.setReturnType(returnType);
			if(e.async()){
				endpointInfo.setAsync("async");
			}
			else {
				endpointInfo.setAsync("sync");
			}
			endpointInfos[i]=endpointInfo;
			
		}
		return endpointInfos[0];
	}

	/**
	 * 閼惧嘲绶遍幒銉ュ經閻拷{@link Endpoint} 閻拷閸氬嫪閲�{@link Param} 閻拷Annotation娣団剝浼�
	 * @param method
	 * @return
	 */
	public static EndpointParameterInfo[] getEndpointParameterMetaInfo(Method method) {
		EndpointParameterInfo[] params;

		Class[] types = method.getParameterTypes();
		Annotation[][] annotations = method.getParameterAnnotations();
		params = new EndpointParameterInfo[types.length];
		for (int i = 0; i < types.length; i++) {
			Param param = AnnotationUtil.getAnnotation(annotations[i],Param.class);

			params[i] = new EndpointParameterInfo();
			params[i].setName(param.name());
			params[i].setType(types[i].toString());
			if(param.optional()){
				params[i].setOptional("yes");
			}
			else{
				params[i].setOptional("no");
			}
			params[i].setDefaultValue(param.defaultValue());
		}

		return params;
	}
	

	/**
	 * @param args
	 * @throws NoSuchMethodException
	 * @throws SecurityException
	 */
	public static void main(String[] args) throws SecurityException,
			NoSuchMethodException {
		/*System.out.println("service info ----------");
		ServiceInfo serviceInfo = MetaInfoLoaderUtil.getServiceMetaInfo(HelloService.class);
		System.out.println(serviceInfo.getShortClassName());
		System.out.println("method info ---------------");
		Class[] parameterTypes = { String.class, InvocationListener.class };
		Method method = HelloService.class.getMethod("sayHello", parameterTypes);
		EndpointInfo endpointInfo = MetaInfoLoaderUtil.getEndpointMetaInfo(method);
		System.out.println(endpointInfo.getEndpointName());

		System.out.println("param info ------------------");
		EndpointParameterInfo[] params = MetaInfoLoaderUtil.getEndpointParameterMetaInfo(method);
		for (int i = 0; i < params.length; i++) {
			System.out.println(params[i].getDefaultValue());
			System.out.println(params[i].getName());
			System.out.println(params[i].getOptional());
			System.out.println(params[i].getType());
		}*/

	}

}
