package com.meidusa.venus.client.xml.bean;

public class EndpointConfig {
    private String name;
    private int timeWait;

    public int getTimeWait() {
        return timeWait;
    }

    public void setTimeWait(int timeWait) {
        this.timeWait = timeWait;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
