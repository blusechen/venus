package com.meidusa.venus.client.nio;

import java.lang.reflect.Type;

/**
 * Created by huawei on 5/20/16.
 */
public class NioPacketWaitTask {
    private long createTime = System.currentTimeMillis();
    private long expireTime;
    private Type returnType;
    private Object result;
    private boolean complete;

    public void setExpireTime(int expireTime) {
        this.expireTime = createTime + expireTime * 1000;
    }

    public boolean isExpire(){
        return (System.currentTimeMillis() - expireTime) > 0;
    }

    public Type getReturnType() {
        return returnType;
    }

    public void setReturnType(Type returnType) {
        this.returnType = returnType;
    }

    public Object getResult() {
        return result;
    }

    public void setResult(Object result) {
        this.result = result;
    }

    public boolean isComplete() {
        return complete;
    }

    public void setComplete(boolean complete) {
        this.complete = complete;
    }
}
