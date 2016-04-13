/**
 * 
 */
package com.meidusa.venus.backend.interceptor.config;

/**
 * @author gaoyong
 * 
 */
public class ScheduleConfig {

    private long initialDelay = 5;

    private long delay = 5;

    public long getInitialDelay() {
        return initialDelay;
    }

    public void setInitialDelay(long initialDelay) {
        this.initialDelay = initialDelay;
    }

    public long getDelay() {
        return delay;
    }

    public void setDelay(long delay) {
        this.delay = delay;
    }

}
