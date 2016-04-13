/**
 * 
 */
package com.meidusa.venus.doclet.metainfo.engine;

import java.io.BufferedReader;

import java.io.IOException;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.meidusa.venus.doclet.metainfo.MethodInfo;
import com.meidusa.venus.doclet.metainfo.ParamInfo;

/**
 * @author gaoyong
 * 
 */
public class CommentScanner {
	
	private static String INTERFACE_regex = "^(.*)(@Service)(.*)";
	private static String SERVICE_regex = "^(.*)(@Service\\(name=\")(.*)(\")";
	private static String PARAM_regex = "^(.*)(@param)(\\s)(\\w*)(.*)";
	private static String RETURN_regex = "^(.*)(@return)(.*)";
	private static String ENDPOINT_regex = "^(.*)(@Endpoint\\(name=\")(.*)(\")";
	private static String COMMENT_regex = "^(.*)(\\*)(.*)";
	private static String COMMENT_END = "^(.*)(\\*/)";
	
	public static List<MethodInfo> scanPlainSource(BufferedReader reader) throws IOException{
		List<MethodInfo> mList = new ArrayList<MethodInfo>();
		//Map<String, List<ParamInfo>> methodParamsMap = new HashMap<String, List<ParamInfo>>();
		List<ParamInfo> paramList = new ArrayList<ParamInfo>();
		String line;
		String methodName;
		String methodDesc;
		StringBuffer desc = new StringBuffer(" ");
		ParamInfo paramInfo;
		boolean scanedServiceComment = false;
		while ((line = reader.readLine()) != null) {
			if(!scanedServiceComment){
				scanedServiceComment = scanedServiceComment(line);
			}
			if(scanedServiceComment){
				if(!paramStart(line)&&!returnStart(line)&&!commentEnd(line)){
					methodDesc = scanMethodDesc(line);
					if(methodDesc!=null){
						desc.append(methodDesc);
					}
				}
				paramInfo = scanParam(line);
				if(paramInfo != null){
					paramList.add(paramInfo);
				}
				methodName = scanMethodName(line);
				if(methodName != null){
					MethodInfo methodInfo = new MethodInfo();
					methodInfo.setInterfaceName(methodName);
					methodInfo.setDesc(desc.toString());
					methodInfo.setParamList(paramList);
					
					mList.add(methodInfo);
					//methodParamsMap.put(methodName, paramList);
					//methodDescMap.put(methodName, desc.toString());
					desc = new StringBuffer(" ");
					paramList = new ArrayList<ParamInfo>();
				}
			}
		}
		return mList;
	}
	
	public static boolean containInterface(String text){
		return isInterface(text);
	}
	
	private static boolean isInterface(String text){
		Pattern pattern = Pattern.compile(INTERFACE_regex,Pattern.DOTALL);
		Matcher m = pattern.matcher(text);
		if(m.find()){
			//System.out.println(m.group(3));
			return true;
		}
		return false;
		
	}
	
	private static boolean scanedServiceComment(String text){
		Pattern pattern = Pattern.compile(SERVICE_regex,Pattern.DOTALL);
		Matcher m = pattern.matcher(text);
		if(m.find()){
			return true;
		}
		return false;
	}
	
	@SuppressWarnings("unused")
	private static String scanServiceName(String text) {
		String serviceName = null;
		Pattern pattern = Pattern.compile(SERVICE_regex,Pattern.DOTALL);
		Matcher m = pattern.matcher(text);
		if(m.find()){
			serviceName = m.group(3);
		}
		return serviceName;
	}
	
	private static boolean paramStart(String text) {
		Pattern pattern = Pattern.compile(PARAM_regex,Pattern.DOTALL);
		Matcher m = pattern.matcher(text);
		if(m.find()){
			return true;
		}
		return false;
	}
	
	private static boolean commentEnd(String text) {
		Pattern pattern = Pattern.compile(COMMENT_END,Pattern.DOTALL);
		Matcher m = pattern.matcher(text);
		if(m.find()){
			return true;
		}
		return false;
	}
	
	
	private static ParamInfo scanParam(String text){
		Pattern pattern = Pattern.compile(PARAM_regex,Pattern.DOTALL);
		Matcher m = pattern.matcher(text);
		ParamInfo paramInfo = null;
		if(m.find()){
			paramInfo = new ParamInfo();
			paramInfo.setParamName(m.group(4));
			paramInfo.setParamComent(m.group(5)+" ");
		}
		return paramInfo;
	}
	
	private static boolean returnStart(String text) {
		Pattern pattern = Pattern.compile(RETURN_regex,Pattern.DOTALL);
		Matcher m = pattern.matcher(text);
		if(m.find()){
			return true;
		}
		return false;
	}
	
	private static String scanMethodName(String text){
		String methodName = null;
		Pattern pattern = Pattern.compile(ENDPOINT_regex,Pattern.DOTALL);
		Matcher m = pattern.matcher(text);
		if(m.find()){
			methodName = m.group(3);
		}
		return methodName;
	}
	
	public static String scanMethodDesc(String text){
		String desc = null;
		Pattern pattern = Pattern.compile(COMMENT_regex,Pattern.DOTALL);
		Matcher m = pattern.matcher(text);
		if(m.find()){
			desc = m.group(3);
		}
		return desc;
	}

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		/*Map map = ParamCreator.scanPlainSource(ParamCreator.getFileReader());
		System.out.println(map);*/
		//ParamCreator.scanPlainSource(FileSourceFilter.getFileReader(new File("/home/gaoyong/mail2.0_Refactoring/service-helloworld/trunk/src/main/java/com/meidusa/venus/hello/api/HelloService.java")));
		//ParamCreator.scanMethodDesc(" * 鏃犺繑鍥炵粨鏋滅殑鏈嶅姟璋冪敤锛屾敮鎸佸洖璋冩柟寮�);
          
	}

}
