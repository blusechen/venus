package com.meidusa.venus.validate.file;

/**
 * Generate path of validation file by {@link ValidationFileInfo}
 * 
 * @author lichencheng.daisy
 * @since 1.0.0-SNAPSHOT
 * 
 */
public interface FilePathGenerator {

    String getConfigPath(ValidationFileInfo info);
}
