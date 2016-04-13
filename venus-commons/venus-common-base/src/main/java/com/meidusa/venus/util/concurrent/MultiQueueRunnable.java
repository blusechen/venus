package com.meidusa.venus.util.concurrent;

import java.util.Queue;

import com.meidusa.toolkit.common.util.Tuple;

public abstract class MultiQueueRunnable implements Runnable, Named {

    private Tuple<QueueConfig, Queue> tuple;
    private MultipleQueue wrapper;

    public final void run() {
        long start = System.currentTimeMillis();
        try {
            doRun();
        } finally {
            finished(start, System.currentTimeMillis());
        }
    }

    public abstract void doRun();

    final void setQueue(MultipleQueue wrapper, Tuple<QueueConfig, Queue> tuple) {
        this.tuple = tuple;
        this.wrapper = wrapper;
    }

    final void taked() {
        tuple.left.incrementAndGet();
    }

    final void finished(long start, long current) {
        if (wrapper != null) {
            MultipleQueue temp = wrapper;
            Tuple<QueueConfig, Queue> tupleTemp = tuple;
            this.tuple = null;
            this.wrapper = null;
            temp.finished(tupleTemp, start, current);
        }
    }
}
