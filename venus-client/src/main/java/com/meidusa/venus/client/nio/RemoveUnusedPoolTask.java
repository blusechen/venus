package com.meidusa.venus.client.nio;

import com.meidusa.toolkit.net.BackendConnectionPool;

/**
 * Created by godzillahua on 5/24/16.
 */
public class RemoveUnusedPoolTask {

    private long createTime =  System.currentTimeMillis();
    private BackendConnectionPool removePool;

    public BackendConnectionPool getRemovePool() {
        return removePool;
    }

    public void setRemovePool(BackendConnectionPool removePool) {
        this.removePool = removePool;
    }

    public boolean canRemove() {
        return ((System.currentTimeMillis() - createTime) / 1000 > 30);
    }
}
