package com.meidusa.venus.cache;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import net.rubyeye.xmemcached.MemcachedClient;
import net.rubyeye.xmemcached.XMemcachedClient;
import net.rubyeye.xmemcached.exception.MemcachedException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.meidusa.toolkit.common.bean.util.Initialisable;

public class XMemcachedVenusClient implements CacheClient, Initialisable {
    private static Logger logger = LoggerFactory.getLogger(XMemcachedVenusClient.class);
    private MemcachedClient client;
    private String ipAddress;
    private int port;

    public Object get(String key) {
        Object returnObj = null;
        try {
            returnObj = client.get(key);
        } catch (TimeoutException e) {
            logger.error("Time out getting data from cache. " + e.getMessage());
        } catch (InterruptedException e) {
            logger.error("Cache operation interrupted. " + e.getMessage());
        } catch (MemcachedException e) {
            logger.error("MemcachedException thrown. " + e.getMessage());
        }
        return returnObj;

    }

    public boolean set(String key, Object value, int expired) {
        boolean returnBool = false;
        try {
            returnBool = client.set(key, expired, value);
        } catch (TimeoutException e) {
            logger.error("Time out getting data from cache. " + e.getMessage());
        } catch (InterruptedException e) {
            logger.error("Cache operation interrupted. " + e.getMessage());
        } catch (MemcachedException e) {
            logger.error("MemcachedException thrown. " + e.getMessage());
        }
        return returnBool;
    }

    public boolean delete(String key) {
        boolean returnBool = false;
        try {
            returnBool = client.delete(key);
        } catch (TimeoutException e) {
            logger.error("Time out getting data from cache. " + e.getMessage());
        } catch (InterruptedException e) {
            logger.error("Cache operation interrupted. " + e.getMessage());
        } catch (MemcachedException e) {
            logger.error("MemcachedException thrown. " + e.getMessage());
        }
        return returnBool;
    }

    public void init() {
        try {
            this.client = new XMemcachedClient(ipAddress, port);
        } catch (IOException e) {
            logger.error("Cannot connect to memcached. " + e.getMessage());
        }
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

}
