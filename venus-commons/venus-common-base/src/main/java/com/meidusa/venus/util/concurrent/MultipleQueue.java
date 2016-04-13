package com.meidusa.venus.util.concurrent;

import java.util.Queue;

import com.meidusa.toolkit.common.util.Tuple;

public interface MultipleQueue {
    void finished(Tuple<QueueConfig, Queue> tuple, long start, long finished);
}
