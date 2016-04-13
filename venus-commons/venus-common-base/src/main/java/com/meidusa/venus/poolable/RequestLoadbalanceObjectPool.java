package com.meidusa.venus.poolable;

import java.util.Map;

import com.meidusa.venus.annotations.Endpoint;

public interface RequestLoadbalanceObjectPool {

    public Object borrowObject(Map<String, Object> parameters, Endpoint endpoint) throws Exception;

}
