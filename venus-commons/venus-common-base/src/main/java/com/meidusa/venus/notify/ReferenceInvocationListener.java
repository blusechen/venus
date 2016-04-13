package com.meidusa.venus.notify;

import java.io.Serializable;

public class ReferenceInvocationListener<T> implements InvocationListener<T>, Serializable {
    private static final long serialVersionUID = 1L;
    private byte[] identityData;

    public byte[] getIdentityData() {
        return identityData;
    }

    public void setIdentityData(byte[] identityData) {
        this.identityData = identityData;
    }

    public void callback(T object) {

    }

    public void onException(Exception e) {

    }

}
