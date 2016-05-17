package com.meidusa.venus.client.nio;

import java.util.List;
import java.util.Set;

/**
 * Created by huawei on 5/17/16.
 */
public class RegistryManager {

    private List<RegistryServer> servers;

    public List<RegistryServer> getServers() {
        return servers;
    }

    public void setServers(List<RegistryServer> servers) {
        this.servers = servers;
    }

    public List<String> getRemote(String serviceName) {

        return null;
    }
}
