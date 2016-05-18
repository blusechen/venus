package com.meidusa.venus.client.nio;

import com.meidusa.toolkit.net.BackendConnectionPool;
import com.meidusa.venus.annotations.Endpoint;
import com.meidusa.venus.annotations.Service;
import com.meidusa.venus.client.VenusInvocationHandler;
import com.meidusa.venus.client.nio.config.RemoteServer;
import com.meidusa.venus.client.nio.config.ServiceConfig;
import com.meidusa.venus.exception.DefaultVenusException;
import com.meidusa.venus.metainfo.EndpointParameter;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import java.lang.reflect.Method;
import java.util.List;

/**
 * Created by huawei on 5/18/16.
 */
public class NioServiceInvocationHandler extends VenusInvocationHandler implements Runnable {

    private RegistryManager rm;
    private ServiceConfig config;

    private BackendConnectionPool pool;
    private List<String> currentRemoteAddress;

    public NioServiceInvocationHandler(RegistryManager rm, ServiceConfig config) {
        Assert.notNull(rm, "注册中心管理器不能为空");
        Assert.notNull(config, "服务配置不能为空");
        this.rm = rm;
        this.config = config;
    }

    public NioServiceInvocationHandler init() {
        List<RemoteServer> servers;
        if (!config.isOverride()) {
            List<String> remoteAddresses = rm.getRemote(config.getServiceName());
            if (remoteAddresses == null || remoteAddresses.size() == 0) {
                throw new DefaultVenusException(0, "未知远程服务地址");
            }

            for(String remoteAddress : remoteAddresses) {

            }

        }else {
            servers = config.getServers();
        }

        return this;
    }

    @Override
    public void run() {
        if (!config.isOverride()) {

        }

    }

    @Override
    protected Object invokeRemoteService(Service service, Endpoint endpoint, Method method, EndpointParameter[] params, Object[] args) throws Exception {

        return null;
    }
}
