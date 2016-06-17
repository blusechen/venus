package com.meidusa.venus.client.nio;

import java.util.concurrent.Callable;

/**
 * Created by godzillahua on 5/24/16.
 */
public class NioCallable implements Callable<Object> {
    private NioPacketWaitTask task;

    public NioCallable(NioPacketWaitTask task) {
        this.task = task;
    }

    @Override
    public Object call() throws Exception {
        while(true) {

            if (task.isExpire()) {
                break;
            }
        }
        return task.getResult();
    }
}
