package com.meidusa.venus.backend.interceptor;

import java.io.Serializable;

public class InterceptorMapping implements Serializable {

    private static final long serialVersionUID = 1L;
    private String name;
    private Interceptor interceptor;

    public InterceptorMapping() {

    }

    public InterceptorMapping(String name, Interceptor interceptor) {
        this.name = name;
        this.interceptor = interceptor;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setInterceptor(Interceptor interceptor) {
        this.interceptor = interceptor;
    }

    public Interceptor getInterceptor() {
        return interceptor;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        final InterceptorMapping that = (InterceptorMapping) o;

        if (name != null ? !name.equals(that.name) : that.name != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result;
        result = (name != null ? name.hashCode() : 0);
        return result;
    }
}
