package com.meidusa.venus.doclet.filesystem;

import java.io.File;

public class DocletSource {
	
	private File interfaceFile;
	
	public File getInterfaceFile() {
		return interfaceFile;
	}

	public void setInterfaceFile(File interfaceFile) {
		this.interfaceFile = interfaceFile;
	}

	public String getQualifiedInterfaceName() {
		return qualifiedInterfaceName;
	}

	public void setQualifiedInterfaceName(String qualifiedInterfaceName) {
		this.qualifiedInterfaceName = qualifiedInterfaceName;
	}

	private String qualifiedInterfaceName;
	
	

}
