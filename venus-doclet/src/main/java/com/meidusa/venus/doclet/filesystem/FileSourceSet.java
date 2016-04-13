package com.meidusa.venus.doclet.filesystem;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

public class FileSourceSet {
	
	private ArrayList<String>  javasourceFiles;
	
	private File rootDir;
	
	public ArrayList<String> getJavasourceFiles() {
		return javasourceFiles;
	}
	
	public File getRootDir() {
		return rootDir;
	}

	public FileSourceSet( File sourceDir ){
		if( !sourceDir.isDirectory() && !sourceDir.exists() )
		{
			throw new IllegalArgumentException( sourceDir.getAbsolutePath() + " must exist" );
		}
		rootDir = sourceDir;
		javasourceFiles = new ArrayList<String>();
		javasourceFiles.addAll( Arrays.asList( Util.getJavaFiles( sourceDir ) ) );
		
	}
	
	public static void main(String[] args){
		File f = new File("/home/gaoyong/mail2.0_Refactoring/service-helloworld/trunk/src/main/java/");
		FileSourceSet fs = new FileSourceSet(f);
		ArrayList<String>  al = fs.getJavasourceFiles();
		Iterator<String> i = al.iterator();
		while(i.hasNext()){
			System.out.println(i.next());
		}
		System.out.println(al);
	}

}
