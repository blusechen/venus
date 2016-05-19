package com.meidusa.venus.client.nio.config;

import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * Created by huawei on 5/17/16.
 */
public class RemoteServer {

    private String hostname;
    private int port;

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}
