package com.meidusa.venus.validate.file;

import java.util.ArrayList;
import java.util.List;

/**
 * Speifiy the service, endpoint and parameter infomation about a validationfile
 * 
 * @author lichencheng.daisy
 * 
 */
public class ValidationFileInfo {
    private Class<?> service;
    private String endpoint;
    private List<String> innerParam;
    private String suffix;

    public ValidationFileInfo() {

    }

    public ValidationFileInfo(Class<?> service, String endpoint, String suffix) {
        super();
        this.service = service;
        this.endpoint = endpoint;
        this.suffix = suffix;
    }

    public Class<?> getService() {
        return service;
    }

    /**
     * @param service service class
     */
    public void setService(Class<?> service) {
        this.service = service;
    }

    public String getEndpoint() {
        return endpoint;
    }

    /**
     * @param endpoint endpoint name
     */
    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    /**
     * @param param add a inner parameter name
     */
    public void addInner(String param) {
        if (innerParam == null) {
            innerParam = new ArrayList<String>();
        }
        innerParam.add(param);
    }

    public List<String> getInnerParam() {
        return innerParam;
    }

    public void setInnerParam(List<String> innerParam) {
        this.innerParam = innerParam;
    }

    public String getSuffix() {
        return suffix;
    }

    /**
     * @param suffix suffix for the file name
     */
    public void setSuffix(String suffix) {
        this.suffix = suffix;
    }

    @Override
    public String toString() {
        return "ValidationFileInfo [service=" + service + ", endpoint=" + endpoint + ", innerParam=" + innerParam + "]";
    }

}
