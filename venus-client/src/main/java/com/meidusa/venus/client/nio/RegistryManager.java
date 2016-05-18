package com.meidusa.venus.client.nio;

import com.meidusa.venus.client.nio.config.RemoteServer;
import com.meidusa.venus.io.utils.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.util.List;

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

    public List<String> getRemote(String serviceName) {

        if (servers == null || servers.size() == 0) {
            logger.warn("registry server not found");
            return null;
        }

        if (!StringUtils.hasLength(serviceName)){
            logger.warn("service name not be empty");
            return null;
        }



        return null;
    }
}
