package com.meidusa.venus.util.concurrent;

import java.util.concurrent.atomic.AtomicInteger;

public class QueueConfig {
    private static long RESET_TIME = 30 * 1000;
    private int maxActive;
    private int maxQueue = 200 * 1000;
    private String name;
    private AtomicInteger runningSize = new AtomicInteger(0);
    private boolean inWaiting = false;
    private long averageLatencyTime;
    private long executionTimes;
    private long lastResetTime = System.currentTimeMillis();

    public boolean isInWaiting() {
        return inWaiting;
    }

    public void setInWaiting(boolean inWaiting) {
        this.inWaiting = inWaiting;
    }

    public int getMaxActive() {
        return maxActive;
    }

    public void setMaxActive(int maxActive) {
        this.maxActive = maxActive;
    }

    public int getMaxQueue() {
        return maxQueue;
    }

    public void setMaxQueue(int maxQueue) {
        this.maxQueue = maxQueue;
    }

    public int getRunningSize() {
        return runningSize.get();
    }

    public int incrementAndGet() {
        return runningSize.incrementAndGet();
    }

    public int decrementAndGet() {
        return runningSize.decrementAndGet();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getAverageLatencyTime() {
        return averageLatencyTime;
    }

    public void addExecutTime(long latencyTime, long currentTime) {
        if (executionTimes == Long.MAX_VALUE || currentTime - lastResetTime > RESET_TIME) {
            reset();
            lastResetTime = currentTime;
        }
        averageLatencyTime = (averageLatencyTime * executionTimes + latencyTime) / (executionTimes + 1);
        executionTimes++;
    }

    private void reset() {
        executionTimes = 1;

    }
}
