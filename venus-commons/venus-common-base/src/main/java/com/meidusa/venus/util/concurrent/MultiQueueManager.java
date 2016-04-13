package com.meidusa.venus.util.concurrent;

import java.util.List;
import java.util.Queue;

import com.meidusa.toolkit.common.util.Tuple;

public interface MultiQueueManager<E> {
    Tuple<QueueConfig, Queue<E>> getQueueTuple(Named named);

    List<Tuple<QueueConfig, Queue<E>>> getAll();
}
