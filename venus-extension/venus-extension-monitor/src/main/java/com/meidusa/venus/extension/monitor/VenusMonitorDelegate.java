package com.meidusa.venus.extension.monitor;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by huawei on 5/14/16.
 */
public class VenusMonitorDelegate {

    private static VenusMonitorDelegate instance = new VenusMonitorDelegate();

    private VenusMonitorDelegate() {

    }

    public static VenusMonitorDelegate getInstance() {
        return instance;
    }

    private static List<AbstractMonitorClient> realMonitor = new ArrayList<AbstractMonitorClient>();

    public void addMonitorClient(AbstractMonitorClient monitor) {
        realMonitor.add(monitor);
    }

    public void reportMetric(String key, int value) {
        for (AbstractMonitorClient client : realMonitor) {
            client.logMetric(key, value);
        }
    }

    public void reportError(String message, Throwable cause) {
        for (AbstractMonitorClient client : realMonitor) {
            client.logError(message, cause);
        }
    }

    public void startTransaction(String rootId, String parentId, String messageId) {
        for (AbstractMonitorClient client : realMonitor) {
            client.startTransaction(rootId, parentId, messageId);
        }
    }

    public void setTransactionData(String key, Object value) {
        for (AbstractMonitorClient client : realMonitor) {
            client.setTransactionData(key, value);
        }
    }

    public void completeTransaction() {
        for (AbstractMonitorClient client : realMonitor) {
            client.completeTransaction();
        }
    }

    public static String getParseErrorKey(String apiName) {
        return apiName + ".parse.error";
    }

    public static String getQueueTimeoutKey(String apiName) {
        return apiName + ".queue.timeout";
    }

    public static String getExecuteTimeoutKey(String apiName) {
        return apiName + ".execute.timeout";
    }

    public static String getInvokeKey(String apiName) {
        return apiName + ".invoke";
    }


}
