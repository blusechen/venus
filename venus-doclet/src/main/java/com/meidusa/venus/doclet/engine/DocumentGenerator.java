/**
 * 
 */
package com.meidusa.venus.doclet.engine;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;

import com.meidusa.venus.doclet.ProjectInfo;
import com.meidusa.venus.doclet.classloader.AppJarClassLoader;
import com.meidusa.venus.doclet.filesystem.DocletSource;
import com.meidusa.venus.doclet.filesystem.FileSourceFilter;
import com.meidusa.venus.doclet.filesystem.FileSourceSet;
import com.meidusa.venus.doclet.metainfo.EndpointInfo;
import com.meidusa.venus.doclet.metainfo.EndpointParameterInfo;
import com.meidusa.venus.doclet.metainfo.MetaInfoLoaderUtil;
import com.meidusa.venus.doclet.metainfo.MethodInfo;
import com.meidusa.venus.doclet.metainfo.ParamInfo;
import com.meidusa.venus.doclet.metainfo.ServiceInfo;
import com.meidusa.venus.doclet.metainfo.engine.CommentScanner;

/**
 * @author gaoyong
 * 
 */
public class DocumentGenerator {

	protected static Logger log = LoggerFactory.getLogger(DocumentGenerator.class);
	private static String logTag = "api_template.vm";
	private AppJarClassLoader appJarClassLoader;

	public DocumentGenerator(AppJarClassLoader appJarClassLoader){
		this.appJarClassLoader = appJarClassLoader;
	}
	/*
	 * static { try { Velocity.init(DocletConfig.properties); } catch (Exception
	 * e) { log.error("initial velocity fail : " + e.getMessage()); } }
	 */

	public void generateDoclet(ProjectInfo projectInfo) {

		FileSourceSet fs = new FileSourceSet(projectInfo.getSourceDirectory());
		List<DocletSource> docletFileSet = FileSourceFilter
				.getForDocletFileSet(fs);
		Iterator<DocletSource> i = docletFileSet.iterator();
		while (i.hasNext()) {
			DocletSource ds = (DocletSource) i.next();
			handleDoclet(ds, projectInfo);
		}
	}

	private void handleDoclet(DocletSource ds, ProjectInfo projectInfo) {
		List<MethodInfo> mList = new ArrayList<MethodInfo>();
		try {
			mList = CommentScanner.scanPlainSource(FileSourceFilter
					.getFileReader(ds.getInterfaceFile()));
		} catch (IOException e) {
			log.error("handleDoclet error DocletSource generate error "
					+ e.getMessage());
		}
		
		
		Class<?> clazz = null;
		try {
			clazz = appJarClassLoader.loadClass(ds.getQualifiedInterfaceName());
		} catch (ClassNotFoundException e) {
			log.error("handleDoclet error : "+e.getMessage());
		}
		renderTemlate(clazz, mList, projectInfo);

	}

	private void renderTemlate(Class<?> clazz, List<MethodInfo> mList,
			ProjectInfo projectInfo) {

		VelocityContext context = new VelocityContext();
		context = fillServiceContext(context, clazz);
		context = fillMethodContext(context, clazz, mList);

		Reader reader = createReader();
		Writer writer = createWriter(projectInfo);

		// mergeTemplate(context, writer);

		try {
			Velocity.evaluate(context, writer, logTag, reader);
			writer.flush();
		} catch (IOException e) {
			log.error("writer close failed :" + e.getMessage());
		} finally {
			if (writer != null) {
				try {
					writer.close();
				} catch (IOException e) {
					log.error("writer close failed :" + e.getMessage());
				}
			}
		}
	}

	@SuppressWarnings("unused")
	private void mergeTemplate(VelocityContext context, Writer writer) {

		Template template = null;

		try {
			template = Velocity.getTemplate(DocletConfig.TEMPLATE_NAME);
			template.merge(context, writer);
		} catch (ResourceNotFoundException e) {
			log.error("merge temlate error : " + e.getMessage());
		} catch (ParseErrorException e) {
			log.error("merge temlate error : " + e.getMessage());
		} catch (Exception e) {
			log.error("merge temlate error : " + e.getMessage());
		}
	}

	private Reader createReader() {
		ClasspathResourceLoader cla = new ClasspathResourceLoader();

		// Reader reader = new
		// InputStreamReader(DocumentGenerator.class.getResourceAsStream("/api_template.vm"));
		Reader reader = null;
		try {
			reader = new InputStreamReader(cla.getResourceStream("/api_template.vm"),"UTF-8");
		} catch (ResourceNotFoundException e) {
			log.error("createReader error :"+e.getMessage());
		} catch (UnsupportedEncodingException e) {
			log.error("createReader error :"+e.getMessage());
		}
		return reader;
	}

	private static Writer createWriter(ProjectInfo projectInfo) {

		FileOutputStream fos;
		BufferedWriter writer = null;
		try {
			// File file = new File(new
			// File("/home/gaoyong/mail2.0_Refactoring/velocitytest/"),"testTemplate.html");
			fos = new FileOutputStream(projectInfo.getOutputDocument());
			// fos = new FileOutputStream(projectInfo.getOutputDocument());
			// fos = new FileOutputStream(projectInfo.getOutputDocument());
			try {
				writer = new BufferedWriter(
						new OutputStreamWriter(fos, "UTF-8"));
			} catch (UnsupportedEncodingException e) {
				log.error("generator writer fail :" + e.getMessage());
			}
		} catch (FileNotFoundException e1) {
			log.error("file not found : " + e1.getMessage());
		}
		return writer;
	}

	private VelocityContext fillServiceContext(VelocityContext context,
			Class<?> clazz) {

		ServiceInfo serviceInfo = MetaInfoLoaderUtil.getServiceMetaInfo(clazz);
		context.put(VTLreference.SERVICE_NAME, serviceInfo.getShortClassName());

		return context;
	}

	private VelocityContext fillMethodContext(VelocityContext context,
			Class<?> clazz, List<MethodInfo> mList) {

		Method[] methods = clazz.getDeclaredMethods();
		MethodInfo[] methodInfos = new MethodInfo[methods.length];
		for (int i = 0; i < methods.length; i++) {
			Method m = methods[i];
			EndpointInfo endpointInfo = MetaInfoLoaderUtil
					.getEndpointMetaInfo(m);

			MethodInfo methodInfo;
			Iterator<MethodInfo> iterator = mList.iterator();
			while (iterator.hasNext()) {

				methodInfo = iterator.next();

				if (endpointInfo.getEndpointName().equals(
						methodInfo.getInterfaceName())) {

					methodInfo.setInterfaceName(endpointInfo.getEndpointName());
					methodInfo.setInvokeStyle(endpointInfo.getAsync());
					methodInfo.setReturnType(endpointInfo.getReturnType());

					List<ParamInfo> paramComments = methodInfo.getParamList();
					Iterator<ParamInfo> it = paramComments.iterator();

					EndpointParameterInfo[] params = MetaInfoLoaderUtil
							.getEndpointParameterMetaInfo(m);
					for (EndpointParameterInfo endpoint : params) {
						endpoint.setParamComent(" ");
						while (it.hasNext()) {
							ParamInfo paramInfo = (ParamInfo) it.next();
							if (endpoint.getName().equals(
									paramInfo.getParamName())) {
								endpoint.setParamComent(paramInfo
										.getParamComent());
								break;
							}
						}
					}
					methodInfo.setParams(params);
					methodInfos[i] = methodInfo;
					break;

				}
			}

		}

		context.put("methods", methodInfos);

		return context;

	}

	/**
	 * @param args
	 * @throws NoSuchMethodException
	 * @throws SecurityException
	 */
	public static void main(String[] args) throws SecurityException,
			NoSuchMethodException {
		// Class clazz = HelloService.class;
		/*
		 * Class[] parameterTypes = { String.class, InvocationListener.class };
		 * Method method = clazz.getMethod("sayHello", parameterTypes);
		 * 
		 * DocumentGenerator.renderTemlate(clazz, method);
		 */
		// System.out.println(clazz.getResource("com.meidusa.venus.hello.api.HelloService"));

		// DocumentGenerator.renderTemlate(clazz);
		String targetDir = "/home/gaoyong/mail2.0_Refactoring/service-helloworld/trunk/target/";
		String targetJar = "service-helloworld-1.0.0-SNAPSHOT.jar";
		File sourceDirectory = new File(
				"/home/gaoyong/mail2.0_Refactoring/service-helloworld/trunk/src/main/java/");
		File jarFile = new File(new File(targetDir), targetJar);
		File document = new File(new File(
				"/home/gaoyong/mail2.0_Refactoring/velocitytest/"),
				"testTemplate.html");

		List jars = new ArrayList();
		jars.add(jarFile);
		jars.add(new File(
				"/home/gaoyong/.m2/repository/com/meidusa/venus/commons/service-common-exception/1.0.0-SNAPSHOT/service-common-exception-1.0.0-SNAPSHOT.jar"));
		jars.add(new File(
				"/home/gaoyong/.m2/repository/com/meidusa/venus/commons/service-common-base/1.0.0-SNAPSHOT/service-common-base-1.0.0-SNAPSHOT.jar"));
		ProjectInfo info = new ProjectInfo();
		// info.setOutputTemlate("testTemplate.html");
		info.setOutputDocument(document);
		//info.setJarFiles(jars);
		info.setSourceDirectory(sourceDirectory);
		//DocumentGenerator.generateDoclet(info);

	}

}
