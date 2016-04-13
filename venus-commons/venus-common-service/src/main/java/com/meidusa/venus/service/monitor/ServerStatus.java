package com.meidusa.venus.service.monitor;

import java.util.Date;

public class ServerStatus {
    private int connectionSize;
    private Date uptime;
    private int requestQueueSize;
    private int activeExecutorThreadSize;

    public int getConnectionSize() {
        return connectionSize;
    }

    public void setConnectionSize(int connectionSize) {
        this.connectionSize = connectionSize;
    }

    public Date getUptime() {
        return uptime;
    }

    public void setUptime(Date uptime) {
        this.uptime = uptime;
    }

    public int getRequestQueueSize() {
        return requestQueueSize;
    }

    public void setRequestQueueSize(int requestQueueSize) {
        this.requestQueueSize = requestQueueSize;
    }

    public int getActiveExecutorThreadSize() {
        return activeExecutorThreadSize;
    }

    public void setActiveExecutorThreadSize(int activeExecutorThreadSize) {
        this.activeExecutorThreadSize = activeExecutorThreadSize;
    }

}
