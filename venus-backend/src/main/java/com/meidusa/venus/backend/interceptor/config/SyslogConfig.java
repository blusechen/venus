package com.meidusa.venus.backend.interceptor.config;

public class SyslogConfig {

    private String syslogHost;

    private String pattern;

    private String level;

    public String getSyslogHost() {
        return syslogHost;
    }

    public void setSyslogHost(String syslogHost) {
        this.syslogHost = syslogHost;
    }

    public String getPattern() {
        return pattern;
    }

    public void setPattern(String pattern) {
        this.pattern = pattern;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

}
