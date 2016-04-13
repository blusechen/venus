package com.meidusa.venus.benchmark;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

public class BenchmarkContext {
    private int total;
    final CountDownLatch requestLatcher;
    final CountDownLatch responseLatcher;
    final AtomicInteger errorNum = new AtomicInteger();
    private boolean running = true;

    public BenchmarkContext(int total) {
        this.total = total;
        requestLatcher = new CountDownLatch((int) total);
        responseLatcher = new CountDownLatch((int) total);
    }

    public int getTotal() {
        return total;
    }

    public CountDownLatch getRequestLatcher() {
        return requestLatcher;
    }

    public CountDownLatch getResponseLatcher() {
        return responseLatcher;
    }

    public AtomicInteger getErrorNum() {
        return errorNum;
    }

    public boolean isRunning() {
        return running;
    }

    public void setRunning(boolean running) {
        this.running = running;
    }

}
