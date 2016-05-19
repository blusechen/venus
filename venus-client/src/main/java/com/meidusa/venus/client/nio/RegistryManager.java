package com.meidusa.venus.client.nio;

import com.meidusa.venus.client.nio.config.RemoteServer;
import com.meidusa.venus.client.simple.SimpleServiceFactory;
import com.meidusa.venus.service.registry.ServiceDefinition;
import com.meidusa.venus.service.registry.ServiceRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Created by huawei on 5/17/16.
 */
public class RegistryManager {

    private static Logger logger = LoggerFactory.getLogger(RegistryManager.class);

    private List<RemoteServer> servers;

    public List<RemoteServer> getServers() {
        return servers;
    }

    public void setServers(List<RemoteServer> servers) {
        this.servers = servers;
    }

    public List<String> getRemote(String serviceName, int version) {

        if (servers == null || servers.size() == 0) {
            logger.warn("registry server not found");
            return null;
        }

        if (!StringUtils.hasLength(serviceName)){
            logger.warn("service name not be empty");
            return null;
        }

        for(RemoteServer server : servers) {
            SimpleServiceFactory ssf = new SimpleServiceFactory(server.getHostname(), server.getPort());
            ServiceRegistry sr = ssf.getService(ServiceRegistry.class);
            try{
                ServiceDefinition sd = sr.getServiceDefinition(serviceName, version);
                if (sd != null) {
                    List<String> ipAddressList = new ArrayList<String>();
                    ipAddressList.addAll(sd.getIpAddress());
                    return ipAddressList;
                }
            }catch (Exception e) {
                logger.warn(server + " get registry info error", e);
                continue;
            }

        }
        return null;
    }
}
