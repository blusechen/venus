package com.meidusa.venus.backend.interceptor;

import java.util.ArrayList;
import java.util.List;

public class InterceptorStack {
    private String name;
    private List<InterceptorMapping> interceptors = new ArrayList<InterceptorMapping>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void addInterceptor(InterceptorMapping interceptor) {
        interceptors.add(interceptor);
    }

    public void addInterceptorStack(InterceptorStack stack) {
        interceptors.addAll(stack.getInterceptors());
    }

    public List<InterceptorMapping> getInterceptors() {
        return interceptors;
    }

    public void setInterceptors(List<InterceptorMapping> interceptors) {
        this.interceptors = interceptors;
    }

}
