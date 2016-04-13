package com.meidusa.venus.extension.monitor.entity;

import java.io.Serializable;
import java.util.Date;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

public abstract class AbstractMonitorEntity implements Serializable {
    private static final long serialVersionUID = -1L;
    private String host;
    private String appId;
    private Date time;
    private String monitorEventId;

    public Date getTime() {
        return time;
    }

    public void setTime(Date time) {
        this.time = time;
    }
    public String getHost() {
        return host;
    }

    public String getMonitorEventId() {
        return monitorEventId;
    }

    public void setMonitorEventId(String monitorEventId) {
        this.monitorEventId = monitorEventId;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
