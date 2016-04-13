package com.meidusa.venus.backend.services.xml.bean;

import java.util.ArrayList;
import java.util.List;

public class InterceptorStackConfig {
    private String name;
    private List<Object> interceptors = new ArrayList<Object>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void addInterceptorRef(InterceptorRef ref) {
        interceptors.add(ref);
    }

    public void addInterceptorStackRef(InterceptorStackRef ref) {
        interceptors.add(ref);
    }

    public List<Object> getInterceptors() {
        return interceptors;
    }

    public void setInterceptors(List<Object> interceptors) {
        this.interceptors = interceptors;
    }

}
