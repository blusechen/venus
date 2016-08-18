package com.meidusa.venus.service.registry;

import java.util.List;

import com.meidusa.venus.annotations.Endpoint;
import com.meidusa.venus.annotations.Param;
import com.meidusa.venus.annotations.Service;

/**
 * 服务注册的相关接口, 供给Venus Registry使用
 * 
 * @author structchen
 * 
 */
@Service(name = "venus.ServiceRegistry", version = 1, athenaFlag = false)
public interface ServiceRegistry {

    /**
     * 获得所有服务列表
     * 
     * @return
     */
    @Endpoint(name = "getServiceDefinitions")
    public List<ServiceDefinition> getServiceDefinitions();

    /**
     * 获得单个服务的 服务定义
     * @param name 服务名
     * @param version 版本号
     * @return ServiceDefinition
     */
    @Endpoint(name = "getServiceDefinition")
    public ServiceDefinition getServiceDefinition(@Param(name = "name") String name, @Param(name = "version") int version);

}
