package com.meidusa.venus.client;

import com.meidusa.toolkit.util.StringUtil;
import com.meidusa.venus.client.nio.config.RemoteServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by godzillahua on 5/24/16.
 */
public class Utils {

    private static Logger logger = LoggerFactory.getLogger(Utils.class);

    public static List<RemoteServer> getRemote(String ipAddressList) {
        if (ipAddressList == null) {
            return null;
        }
        String[] ipAddressArray = StringUtil.split(ipAddressList, ",");
        if (ipAddressArray == null || ipAddressArray.length <= 0) {
            return null;
        }
        List<RemoteServer> remoteServers = new ArrayList<RemoteServer>();
        for(String ipAddress : ipAddressArray) {
            String[] temp = StringUtil.split(ipAddress, ":");
            if (temp == null) {
                continue;
            }
            RemoteServer remoteServer = new RemoteServer();
            if (temp.length == 1) {
                remoteServer.setHostname(temp[0]);
                remoteServer.setPort(Constants.DEFAULT_SERVICE_PORT);
            }else {
                remoteServer.setHostname(temp[0]);
                try{
                    remoteServer.setPort(Integer.parseInt(temp[1]));
                }catch (Exception e) {
                    logger.warn("parser remote server error:" + ipAddress, e);
                    continue;
                }
            }
            remoteServers.add(remoteServer);
        }
        return remoteServers;
    }
}
