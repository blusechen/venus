/**
 * 
 */
package com.meidusa.venus.doclet.filesystem;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.meidusa.venus.doclet.metainfo.engine.CommentScanner;

/**
 * @author gaoyong
 *
 */
public class FileSourceFilter {
	//public static List docletFileSet = new ArrayList();
	protected static Logger log = LoggerFactory.getLogger(FileSourceFilter.class);
	
	public static List<DocletSource> getForDocletFileSet(FileSourceSet fs){
		List<DocletSource> docletFileSet = new ArrayList<DocletSource>();
		ArrayList<String>  sourceFiles = fs.getJavasourceFiles();
		Iterator<String> i = sourceFiles.iterator();
		String javasourceName;
		File srcfile;
		DocletSource docletSource;
		while(i.hasNext()){
			
			javasourceName = i.next();
			//System.out.println(Util.getQualifiedInterfaceName(javasourceName));
			srcfile = new File(fs.getRootDir(),javasourceName);
			try {
				if(isIDLFile(srcfile)){
					docletSource = new DocletSource();
					docletSource.setInterfaceFile(srcfile);
					docletSource.setQualifiedInterfaceName(Util.getQualifiedInterfaceName(javasourceName));
					docletFileSet.add(docletSource);
				}
			} catch (IOException e) {
				log.error("generator docletSource error : "+e.getMessage());
			}
		}
		return docletFileSet;
	}
	
	
	public static boolean isIDLFile(File sourceFile) throws IOException{
		BufferedReader reader = new BufferedReader(getFileReader(sourceFile));
		String line;
		while ((line = reader.readLine()) != null) {
			if(CommentScanner.containInterface(line)){
				return true;
			}
		}
		return false;
	}
	
	
	/**
	 * 
	 */
	public static BufferedReader getFileReader(File sourceFile) {
		//File file = new File("/home/gaoyong/mail2.0_Refactoring/service-helloworld/trunk/src/main/java/com/meidusa/venus/hello/api/HelloService.java");
		FileInputStream in = null;
		InputStreamReader reader = null;
		try {
			in = new FileInputStream(sourceFile);
		} catch (FileNotFoundException e) {
			log.error("source file error file :"+sourceFile.getName()+" "+e.getMessage());
		}
		reader = new InputStreamReader(in,Charset.forName("GBK"));
		return new BufferedReader(reader);
	}
	

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		//File f = new File("/home/gaoyong/mail2.0_Refactoring/service-helloworld/trunk/src/main/java/com/meidusa/venus/hello/api/Hello.java");
		//System.out.println(FileSourceFilter.isInterfaceFile(f));
		FileSourceSet fs = new FileSourceSet(new File("/home/gaoyong/mail2.0_Refactoring/service-helloworld/trunk/src/main/java/"));
		FileSourceFilter.getForDocletFileSet(fs);

	}

}
