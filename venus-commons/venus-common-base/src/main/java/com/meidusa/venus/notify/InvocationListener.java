package com.meidusa.venus.notify;

import com.meidusa.venus.annotations.Param;

public interface InvocationListener<T> {

    public void callback(@Param(name = "object") T object);

    public void onException(Exception e);
}
