package com.meidusa.venus.util.concurrent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import com.meidusa.toolkit.common.util.Tuple;

public class DefaultMultiQueueManager<E> implements MultiQueueManager<E> {
    private Map<String, Tuple<QueueConfig, Queue<E>>> namedQueueMap = new HashMap<String, Tuple<QueueConfig, Queue<E>>>();

    public Tuple<QueueConfig, Queue<E>> getQueueTuple(Named named) {
        Tuple<QueueConfig, Queue<E>> tuple = namedQueueMap.get(named.getName());
        if (tuple == null) {
            synchronized (namedQueueMap) {
                tuple = namedQueueMap.get(named.getName());
                if (tuple == null) {
                    tuple = newTuple(named);
                    namedQueueMap.put(named.getName(), tuple);
                }
            }
        }
        return tuple;
    }

    public Tuple<QueueConfig, Queue<E>> newTuple(Named named) {
        QueueConfig config = getConfig(named);
        Tuple<QueueConfig, Queue<E>> tuple = new Tuple<QueueConfig, Queue<E>>(config, createQueue(config));
        return tuple;
    }

    public Queue<E> createQueue(QueueConfig config) {
        return new LinkedList<E>();
    }

    public QueueConfig getConfig(Named named) {
        QueueConfig config = new QueueConfig();
        config.setMaxActive(50);
        config.setMaxQueue(100000);
        config.setName(named.getName());
        return config;
    }

    public List<Tuple<QueueConfig, Queue<E>>> getAll() {
        List<Tuple<QueueConfig, Queue<E>>> list = new ArrayList<Tuple<QueueConfig, Queue<E>>>();
        synchronized (namedQueueMap) {
            for (Map.Entry<String, Tuple<QueueConfig, Queue<E>>> it : namedQueueMap.entrySet()) {
                list.add(it.getValue());
            }
            return list;
        }
    }

}
