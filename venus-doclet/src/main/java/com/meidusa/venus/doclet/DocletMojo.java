/**
 * 
 */
package com.meidusa.venus.doclet;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;

import com.meidusa.venus.doclet.classloader.AppJarClassLoader;
import com.meidusa.venus.doclet.engine.DocumentGenerator;

/**
 * @author gaoyong
 * @goal doclet
 * @requiresDependencyResolution
 * 
 * 
 */
public class DocletMojo extends AbstractMojo {

	protected static Logger log = LoggerFactory.getLogger(DocletMojo.class);

	/**
	 * Location of the outputfile.
	 * 
	 * @parameter expression="${project.build.directory}"
	 * @required
	 */
	private File outputDirectory;

	/**
	 * Location of the srcfile.
	 * 
	 * @parameter expression="${project.build.sourceDirectory}"
	 * @required
	 */
	private File sourceDirectory;

	/**
	 * @parameter expression="${doclet.documentName}"
	 *            default-value="testTemplate.html"
	 */
	private String documentName;

	/**
	 * The maven project.
	 * 
	 * @parameter expression="${project}"
	 * @required
	 * @readonly
	 */
	private MavenProject project;
	
	/**
	 * @parameter expression="${project.build.finalName}"
	 */
	private String finalName;
	
	/**
	 * @parameter expression="${project.packaging}"
	 */
	private String packaging;

	public void execute() throws MojoExecutionException, MojoFailureException {
		
		ProjectInfo projectInfo = new ProjectInfo();
		projectInfo.setSourceDirectory(sourceDirectory);
		projectInfo.setOutputDocument(new File(outputDirectory, documentName));
		
		AppJarClassLoader appJarClassLoader = AppJarClassLoader.getAppJarClassLoader(processJarURLs(processDependencies()), Thread.currentThread().getContextClassLoader());

		DocumentGenerator documentGenerator =  new DocumentGenerator(appJarClassLoader);
		
		documentGenerator.generateDoclet(projectInfo);

	}
	
	private List<File> processDependencies(){
		List<File> dependencyJars = new ArrayList<File>();
		Set<Artifact> dependencyArtifacts = project.getArtifacts();
		//jdk 1.5
		for(Artifact dependency : dependencyArtifacts ){
			if (dependency.getFile() != null) {
				dependencyJars.add(dependency.getFile());
			}
		}
		/*//before jdk 1.5
		for (Iterator<Artifact> i = dependencyArtifacts.iterator(); i.hasNext();) {
			Artifact dependency = i.next();
			// getLog().info("---------->"+dependency.getFile());
			if (dependency.getFile() != null) {
				dependencyJars.add(dependency.getFile());
			}
		}*/
		if(packaging.equals("jar")){
			dependencyJars.add(new File(outputDirectory, finalName + ".jar"));
		}
		if(packaging.equals("war")){
			//TODO 		
		}
		return dependencyJars;
		
	}
	
	private URL[] processJarURLs(List<File> jarFiles) {
		List<URL> dependencyJarurls = new ArrayList<URL>();
		for(File jarFile : jarFiles){
			try {
				dependencyJarurls.add(jarFile.toURI().toURL());
			} catch (MalformedURLException e) {
				log.error("processClassloader about jarurl error "
						+ e.getMessage());
			}
		}
		
		/*Iterator<File> i = jarFiles.iterator();
		while (i.hasNext()) {
			File jarFile = (File) i.next();
			try {
				dependencyJarurls.add(jarFile.toURI().toURL());
			} catch (MalformedURLException e) {
				log.error("processClassloader about jarurl error "
						+ e.getMessage());
			}

		}*/
		return dependencyJarurls.toArray(new URL[dependencyJarurls.size()]);
		
	}

}
