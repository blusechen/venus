package com.meidusa.venus.doclet.filesystem;

/*
 * Copyright (c) 2001-2003 The XDoclet team
 * All rights reserved.
 */

import java.io.File;
import java.io.FileFilter;
import java.util.LinkedList;

/**
 * Various static utility methods
 *
 * @author         Aslak Helles閿熺当
 * @created        20. januar 2002
 */
public class Util
{

	private final static FileFilter _javaFilter =
		new FileFilter()
		{
			public boolean accept( File f )
			{
				return f.getName().endsWith( ".java" );
			}
		};

	private final static FileFilter _dirFilter =
		new FileFilter()
		{
			public boolean accept( File f )
			{
				return f.isDirectory();
			}
		};

	/**
	 * Returns an array of String containing relative names of all java files under
	 * root.
	 *
	 * @param root  the root directory
	 * @return      java file names
	 */
	public static String[] getJavaFiles( File root )
	{
		LinkedList<String> javaFileNames = new LinkedList<String>();

		descend( root, "", javaFileNames );
		return ( String[] ) javaFileNames.toArray( new String[javaFileNames.size()] );
	}

	public static String getPackageNameFor( String qualifiedName )
	{
		String packageName = null;
		int lastDotIndex = qualifiedName.lastIndexOf( '.' );

		if( lastDotIndex == -1 )
		{
			// default package
			packageName = "";
		}
		else
		{
			packageName = qualifiedName.substring( 0, lastDotIndex );
		}
		return packageName;
	}

	public static String getQualifiedNameFor( String packageName, String unqualifiedName )
	{
		if( packageName.equals( "" ) )
		{
			return unqualifiedName;
		}
		else
		{
			return packageName + "." + unqualifiedName;
		}
	}

	public final static StringBuffer appendDimensionAsString( final int n, final StringBuffer sb )
	{
		for( int i = 0; i < n; i++ )
		{
			sb.append( "[]" );
		}
		return sb;
	}

	public final static String toString( Object[] array, String delimiter )
	{
		StringBuffer sb = new StringBuffer();

		for( int i = 0; i < array.length; i++ )
		{
			sb.append( array[i].toString() );
			if( i < array.length - 1 )
			{
				sb.append( delimiter );
			}
		}
		return sb.toString();
	}
	/**
	 * Return only class name of a full qualified (package+classname) string.
	 *
	 * @param qualifiedName
	 * @return
	 */
	public static String classNameFromQualifiedClassName( String qualifiedName )
	{
		if( qualifiedName == null )
		{
			throw new IllegalArgumentException( "qualifiedName can't be null!" );
		}

		int dot_index = qualifiedName.lastIndexOf( '.' );

		if( dot_index != -1 )
			return qualifiedName.substring( dot_index + 1 );
		else
			return qualifiedName;
	}

	/**
	 * Recursively descends a directory and build a list of relative file names for
	 * java files.
	 *
	 * @param root           the root directory
	 * @param dirName        current directory relative filename
	 * @param javaFileNames  the list where java file names will be added
	 */
	private static void descend( File root, String dirName, LinkedList<String> javaFileNames )
	{
		File dir = new File( root, dirName );

		File[] javaFiles = dir.listFiles( _javaFilter );

		for( int i = 0; i < javaFiles.length; i++ )
		{
			StringBuffer javaFileName = new StringBuffer();

			if( dirName.length() != 0 )
			{
				javaFileName.append( dirName ).append( File.separator );
			}
			javaFileName.append( javaFiles[i].getName() );
			javaFileNames.add( javaFileName.toString() );
		}

		File[] subDirs = dir.listFiles( _dirFilter );

		for( int i = 0; i < subDirs.length; i++ )
		{
			StringBuffer subDirName = new StringBuffer( dirName );

			if( dirName.length() != 0 )
			{
				subDirName.append( File.separator );
			}
			subDirName.append( subDirs[i].getName() );
			descend( root, subDirName.toString(), javaFileNames );
		}
	}
	
	public static String getQualifiedInterfaceName(String javasourceName){
		String temp = javasourceName.replace(File.separatorChar, '.');	
		return temp.substring(0, temp.lastIndexOf("."));
	}
}
