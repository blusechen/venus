package com.meidusa.venus.validate.file;

import java.util.Iterator;

import com.meidusa.venus.annotations.Service;

/**
 * @author lichencheng.daisy
 * @since 1.0.0-SNAPSHOT
 * 
 */
public class DefaultFilePathGenerator implements FilePathGenerator {
    private static char PACKAGE_DELIMITER = '.';
    private static char FOLDER_DELIMITER = '/';

    private String prefix;
    private String suffix;
    private String delimiter;

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public String getSuffix() {
        return suffix;
    }

    public void setSuffix(String suffix) {
        this.suffix = suffix;
    }

    public String getDelimiter() {
        return delimiter;
    }

    public void setDelimiter(String delimiter) {
        this.delimiter = delimiter;
    }

    public DefaultFilePathGenerator() {
        super();
        prefix = "";
        suffix = "validation.xml";
        delimiter = "-";
    }

    public DefaultFilePathGenerator(String prefix, String suffix, String delimiter) {
        super();
        this.prefix = prefix;
        this.suffix = suffix;
        this.delimiter = delimiter;
    }

    @Override
    public String toString() {
        return "DefaultValidationFilePathGenerator [prefix=" + prefix + ", suffix=" + suffix + ", delimiter=" + delimiter + "]";
    }

    @Override
    public String getConfigPath(ValidationFileInfo info) {
        String serviceName = null;
        Service serviceAnnotation = info.getService().getAnnotation(Service.class);
        if (serviceAnnotation != null) {
            serviceName = serviceAnnotation.name();
        }

        StringBuffer urlString = new StringBuffer();
        urlString.append(info.getService().getPackage().getName().replace(PACKAGE_DELIMITER, FOLDER_DELIMITER));
        urlString.append(FOLDER_DELIMITER);

        if (serviceName != null && serviceName.length() != 0) {
            urlString.append(serviceName);
        } else {
            urlString.append(Character.toLowerCase(info.getService().getSimpleName().charAt(0)));
            urlString.append(info.getService().getSimpleName().substring(1));
        }

        urlString.append(this.getDelimiter());
        urlString.append(info.getEndpoint());
        urlString.append(this.getDelimiter());
        if (info.getInnerParam() != null) {
            for (Iterator<String> iterator = info.getInnerParam().iterator(); iterator.hasNext();) {
                String innerParam = (String) iterator.next();
                urlString.append(innerParam);
                urlString.append(this.getDelimiter());
            }
        }
        if (info.getSuffix() != null && info.getSuffix().length() > 0) {
            urlString.append(info.getSuffix());
            urlString.append(this.getDelimiter());
        }
        urlString.append(this.getSuffix());
        return urlString.toString();
    }

}
