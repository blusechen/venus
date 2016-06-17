package com.meidusa.venus.poolable;

import com.meidusa.venus.annotations.Endpoint;

import java.util.Map;

public interface RequestLoadbalanceObjectPool {

    public Object borrowObject(Map<String, Object> parameters, Endpoint endpoint) throws Exception;

}
