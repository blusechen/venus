package com.meidusa.venus.extension.monitor;

/**
 * Created by huawei on 5/15/16.
 */
public abstract class AbstractMonitorClient {

    public abstract void logMetric(String key, int value);

    public abstract void logError(String message, Throwable cause);

}
