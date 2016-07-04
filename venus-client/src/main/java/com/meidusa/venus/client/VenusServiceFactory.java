/*
 * Copyright 2008-2108 amoeba.meidusa.com 
 * 
 * 	This program is free software; you can redistribute it and/or modify it under the terms of 
 * the GNU AFFERO GENERAL PUBLIC LICENSE as published by the Free Software Foundation; either version 3 of the License, 
 * or (at your option) any later version. 
 * 
 * 	This program is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * See the GNU AFFERO GENERAL PUBLIC LICENSE for more details. 
 * 	You should have received a copy of the GNU AFFERO GENERAL PUBLIC LICENSE along with this program; 
 * if not, write to the Free Software Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package com.meidusa.venus.client;

import com.meidusa.toolkit.common.bean.BeanContext;
import com.meidusa.toolkit.common.bean.BeanContextBean;
import com.meidusa.toolkit.common.bean.config.ConfigUtil;
import com.meidusa.toolkit.common.bean.config.ConfigurationException;
import com.meidusa.toolkit.common.bean.util.InitialisationException;
import com.meidusa.toolkit.common.poolable.MultipleLoadBalanceObjectPool;
import com.meidusa.toolkit.common.poolable.ObjectPool;
import com.meidusa.toolkit.common.poolable.PoolableObjectPool;
import com.meidusa.toolkit.common.util.Tuple;
import com.meidusa.toolkit.net.*;
import com.meidusa.toolkit.util.StringUtil;
import com.meidusa.venus.annotations.Endpoint;
import com.meidusa.venus.client.xml.bean.*;
import com.meidusa.venus.digester.DigesterRuleParser;
import com.meidusa.venus.exception.CodedException;
import com.meidusa.venus.exception.VenusConfigException;
import com.meidusa.venus.exception.VenusExceptionFactory;
import com.meidusa.venus.exception.XmlVenusExceptionFactory;
import com.meidusa.venus.extension.athena.AthenaExtensionResolver;
import com.meidusa.venus.io.network.VenusBIOConnectionFactory;
import com.meidusa.venus.io.network.VenusBackendConnectionFactory;
import com.meidusa.venus.io.packet.PacketConstant;
import com.meidusa.venus.util.FileWatchdog;
import com.meidusa.venus.util.VenusBeanUtilsBean;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.ConvertUtilsBean;
import org.apache.commons.beanutils.PropertyUtilsBean;
import org.apache.commons.digester.Digester;
import org.apache.commons.digester.RuleSet;
import org.apache.commons.digester.xmlrules.FromXmlRuleSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.URL;
import java.util.*;

public class VenusServiceFactory implements ServiceFactory, BeanFactoryAware, InitializingBean, BeanFactoryPostProcessor {
    private static Logger logger = LoggerFactory.getLogger(ServiceFactory.class);
    private Map<Class<?>, Tuple<Object, RemotingInvocationHandler>> servicesMap = new HashMap<Class<?>, Tuple<Object, RemotingInvocationHandler>>();
    private Map<String, Tuple<Object, RemotingInvocationHandler>> serviceBeanMap = new HashMap<String, Tuple<Object, RemotingInvocationHandler>>();
    private BeanFactory beanFactory;
    private ConnectionManager connManager;
    private ConnectionConnector connector;
    private String[] configFiles;
    private BeanContext beanContext;
    private boolean enableAsync = true;
    private boolean shutdown = false;
    private Map<String, Tuple<ObjectPool, BackendConnectionPool>> poolMap = new HashMap<String, Tuple<ObjectPool, BackendConnectionPool>>(); // NOPMD
    // by
    // structchen
    // on
    // 13-10-21
    // 下午12:25
    private Map<String, Object> realPools = new HashMap<String, Object>();
    private InvocationListenerContainer container = new InvocationListenerContainer();
    private VenusNIOMessageHandler handler = new VenusNIOMessageHandler();
    private VenusExceptionFactory venusExceptionFactory;
    private Map<Class<?>, ServiceConfig> serviceConfig = new HashMap<Class<?>, ServiceConfig>();
    private int asyncExecutorSize = 10;
    private boolean needPing = false;
    private Timer reloadTimer = new Timer();
    private boolean enableReload = false;

    public boolean isEnableReload() {
        return enableReload;
    }

    public void setEnableReload(boolean enableReload) {
        this.enableReload = enableReload;
    }

    public boolean isNeedPing() {
        return needPing;
    }

    public void setNeedPing(boolean needPing) {
        this.needPing = needPing;
    }

    public boolean isEnableAsync() {
        return enableAsync;
    }

    public void setEnableAsync(boolean enableAsync) {
        this.enableAsync = enableAsync;
    }

    public int getAsyncExecutorSize() {
        return asyncExecutorSize;
    }

    public ConnectionConnector getConnector() {
        return connector;
    }

    public void setConnector(ConnectionConnector connector) {
        this.connector = connector;
    }

    public void setAsyncExecutorSize(int asyncExecutorSize) {
        this.asyncExecutorSize = asyncExecutorSize;
    }

    public VenusExceptionFactory getVenusExceptionFactory() {
        return venusExceptionFactory;
    }

    public void setVenusExceptionFactory(VenusExceptionFactory venusExceptionFactory) {
        this.venusExceptionFactory = venusExceptionFactory;
    }

    public String[] getConfigFiles() {
        return configFiles;
    }

    public void setConfigFiles(String[] configFiles) {
        this.configFiles = configFiles;
    }

    @SuppressWarnings("unchecked")
    public <T> T getService(Class<T> t) {
        if (shutdown) {
            throw new IllegalStateException("service factory has been shutdown");
        }
        Tuple<Object, RemotingInvocationHandler> object = servicesMap.get(t);

        return (T) object.left;
    }

    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
    }

    @SuppressWarnings("unchecked")
    public void afterPropertiesSet() throws Exception {
        logger.trace("current Venus Client id=" + PacketConstant.VENUS_CLIENT_ID);
        if (venusExceptionFactory == null) {
            XmlVenusExceptionFactory xmlVenusExceptionFactory = new XmlVenusExceptionFactory();

            //3.0.8版本将采用自动扫描的方式获得 exception 相关的配置
            //xmlVenusExceptionFactory.setConfigFiles(new String[] { "classpath:com/meidusa/venus/exception/VenusSystemException.xml" });
            xmlVenusExceptionFactory.init();
            this.venusExceptionFactory = xmlVenusExceptionFactory;
        }

        handler.setVenusExceptionFactory(venusExceptionFactory);
        if (enableAsync) {
            if (connector == null) {
                this.connector = new ConnectionConnector("connection Connector");
                connector.setDaemon(true);

            }

            if (connManager == null) {
                try {
                    connManager = new ConnectionManager("Connection Manager", this.getAsyncExecutorSize());
                } catch (IOException e) {
                    throw new InitialisationException(e);
                }
                connManager.setDaemon(true);
                connManager.start();
            }

            connector.setProcessors(new ConnectionManager[]{connManager});
            connector.start();
        }

        beanContext = new ClientBeanContext(beanFactory);
        BeanContextBean.getInstance().setBeanContext(beanContext);
        VenusBeanUtilsBean.setInstance(new ClientBeanUtilsBean(new ConvertUtilsBean(), new PropertyUtilsBean(), beanContext));
        AthenaExtensionResolver.getInstance().resolver();
        handler.setContainer(this.container);
        reloadConfiguration();

        __RELOAD:
        {
            if (enableReload) {
                File[] files = new File[this.configFiles.length];
                for (int i = 0; i < this.configFiles.length; i++) {
                    try {
                        files[i] = ResourceUtils.getFile(configFiles[i].trim());
                    } catch (FileNotFoundException e) {
                        logger.warn(e.getMessage());
                        enableReload = false;
                        logger.warn("venus serviceFactory configuration reload disabled!");
                        break __RELOAD;
                    }
                }
                VenusFileWatchdog dog = new VenusFileWatchdog(files);
                dog.setDelay(1000 * 10);
                dog.start();
            }
        }
    }

    class VenusFileWatchdog extends FileWatchdog {

        protected VenusFileWatchdog(File... file) {
            super(file);
        }

        @Override
        protected void doOnChange() {
            try {
                VenusServiceFactory.this.reloadConfiguration();
            } catch (Exception e) {
                VenusServiceFactory.logger.error("reload configuration error", e);
            }
        }

    }

    private synchronized void reloadConfiguration() throws Exception {
        Map<Class<?>, Tuple<Object, RemotingInvocationHandler>> servicesMap = new HashMap<Class<?>, Tuple<Object, RemotingInvocationHandler>>();
        Map<String, Tuple<ObjectPool, BackendConnectionPool>> poolMap = new HashMap<String, Tuple<ObjectPool, BackendConnectionPool>>();
        Map<Class<?>, ServiceConfig> serviceConfig = new HashMap<Class<?>, ServiceConfig>();

        final Map<String, Object> realPools = new HashMap<String, Object>();
        try {
            loadConfiguration(poolMap, servicesMap, serviceConfig, realPools);
        } catch (Exception e) {
            reloadTimer.schedule(new ClosePoolTask(realPools), 1000 * 30);
            throw e;
        }
        this.poolMap = poolMap;

        for (Map.Entry<Class<?>, Tuple<Object, RemotingInvocationHandler>> entry : servicesMap.entrySet()) {
            Class<?> key = entry.getKey();
            Tuple<Object, RemotingInvocationHandler> source = entry.getValue();
            Tuple<Object, RemotingInvocationHandler> target = this.servicesMap.get(key);
            if (target != null) {
                target.right.setBioConnPool(source.getRight().getBioConnPool());
                target.right.setNioConnPool(source.getRight().getNioConnPool());
                target.right.setSerializeType((byte) target.right.getSerializeType());
            } else {
                this.servicesMap.put(key, source);
            }
        }

        this.serviceConfig = serviceConfig;
        final Map<String, Object> oldPools = this.realPools;
        this.realPools = realPools;
        reloadTimer.schedule(new ClosePoolTask(oldPools), 1000 * 30);

    }

    class ClosePoolTask extends TimerTask {
        Map<String, Object> pools;

        public ClosePoolTask(Map<String, Object> pools) {
            this.pools = pools;
        }

        @Override
        public void run() {
            for (Map.Entry<String, Object> pool : pools.entrySet()) {
                try {
                    if (pool.getValue() instanceof ObjectPool) {
                        ((ObjectPool) pool.getValue()).close();
                    } else if (pool.getValue() instanceof BackendConnectionPool) {
                        ((BackendConnectionPool) pool.getValue()).close();
                    }
                } catch (Exception e) {
                }
            }
        }
    }

    private void loadConfiguration(Map<String, Tuple<ObjectPool, BackendConnectionPool>> poolMap,
                                   Map<Class<?>, Tuple<Object, RemotingInvocationHandler>> servicesMap, Map<Class<?>, ServiceConfig> serviceConfig, Map<String, Object> realPools)
            throws Exception {
        VenusClient all = new VenusClient();
        for (String configFile : configFiles) {
            configFile = (String) ConfigUtil.filter(configFile);
            URL url = this.getClass().getResource("venusClientRule.xml");
            if (url == null) {
                throw new VenusConfigException("venusClientRule.xml not found!,pls rebuild venus!");
            }
            RuleSet ruleSet = new FromXmlRuleSet(url, new DigesterRuleParser());
            Digester digester = new Digester();
            digester.setValidating(false);
            digester.addRuleSet(ruleSet);

            try {
                InputStream is = ResourceUtils.getURL(configFile.trim()).openStream();
                VenusClient venus = (VenusClient) digester.parse(is);
                for (ServiceConfig config : venus.getServiceConfigs()) {
                    if (config.getType() == null) {
                        logger.error("Service type can not be null:" + configFile);
                        throw new ConfigurationException("Service type can not be null:" + configFile);
                    }
                }
                all.getRemoteMap().putAll(venus.getRemoteMap());
                all.getServiceConfigs().addAll(venus.getServiceConfigs());
            } catch (Exception e) {
                throw new ConfigurationException("can not parser xml:" + configFile, e);
            }
        }

        // 初始化 remote，并且创建Pool
        for (Map.Entry<String, Remote> entry : all.getRemoteMap().entrySet()) {
            RemoteContainer container = createRemoteContainer(entry.getValue(), realPools);
            Tuple<ObjectPool, BackendConnectionPool> tuple = new Tuple<ObjectPool, BackendConnectionPool>();
            tuple.left = container.getBioPool();
            tuple.right = container.getNioPool();
            poolMap.put(entry.getKey(), tuple);
        }

        for (ServiceConfig config : all.getServiceConfigs()) {

            Remote remote = all.getRemoteMap().get(config.getRemote());
            Tuple<ObjectPool, BackendConnectionPool> tuple = null;
            if (!StringUtil.isEmpty(config.getRemote())) {
                tuple = poolMap.get(config.getRemote());
                if (tuple == null) {
                    throw new ConfigurationException("remote=" + config.getRemote() + " not found!!");
                }
            } else {
                String ipAddress = config.getIpAddressList();
                tuple = poolMap.get(ipAddress);
                if (ipAddress != null && tuple == null) {
                    RemoteContainer container = createRemoteContainer(true, ipAddress, realPools);
                    tuple = new Tuple<ObjectPool, BackendConnectionPool>();
                    tuple.left = container.getBioPool();
                    tuple.right = container.getNioPool();
                    poolMap.put(ipAddress, tuple);
                }
            }

            if (tuple != null) {
                RemotingInvocationHandler invocationHandler = new RemotingInvocationHandler();
                invocationHandler.setBioConnPool(tuple.left);
                invocationHandler.setNioConnPool(tuple.right);
                invocationHandler.setServiceFactory(this);
                invocationHandler.setVenusExceptionFactory(this.getVenusExceptionFactory());
                if (remote != null && remote.getAuthenticator() != null) {
                    invocationHandler.setSerializeType(remote.getAuthenticator().getSerializeType());
                }

                invocationHandler.setContainer(this.container);

                Object object = Proxy.newProxyInstance(this.getClass().getClassLoader(), new Class[]{config.getType()}, invocationHandler);

                for (Method method : config.getType().getMethods()) {
                    Endpoint endpoint = method.getAnnotation(Endpoint.class);
                    if (endpoint != null) {
                        Class[] eclazz = method.getExceptionTypes();
                        for (Class clazz : eclazz) {
                            if (venusExceptionFactory != null && CodedException.class.isAssignableFrom(clazz)) {
                                venusExceptionFactory.addException(clazz);
                            }
                        }
                    }
                }

                serviceConfig.put(config.getType(), config);
                Tuple<Object, RemotingInvocationHandler> serviceTuple = new Tuple<Object, RemotingInvocationHandler>(object, invocationHandler);
                servicesMap.put(config.getType(), serviceTuple);
                if (config.getBeanName() != null) {
                    serviceBeanMap.put(config.getBeanName(), serviceTuple);
                }
            } else {
                if (config.getInstance() != null) {
                    Tuple<Object, RemotingInvocationHandler> serviceTuple = new Tuple<Object, RemotingInvocationHandler>(config.getInstance(), null);
                    servicesMap.put(config.getType(), serviceTuple);
                    if (config.getBeanName() != null) {
                        serviceBeanMap.put(config.getBeanName(), serviceTuple);
                    }
                } else {
                    throw new ConfigurationException("Service instance or ipAddressList or remote can not be null:" + config.getType());
                }
            }

        }
    }

    private RemoteContainer createRemoteContainer(boolean share, String ipAddress, Map<String, Object> realPools) throws Exception {
        RemoteContainer container = new RemoteContainer();

        if (!StringUtil.isEmpty(ipAddress)) {
            String ipList[] = StringUtil.split(ipAddress, ", ");
            PoolableObjectPool bioPools[] = new PoolableObjectPool[ipList.length];
            BackendConnectionPool nioPools[] = new BackendConnectionPool[ipList.length];
            for (int i = 0; i < ipList.length; i++) {
                String shareName = share ? "SHARED-" : "";
                if (share) {
                    nioPools[i] = (PollingBackendConnectionPool) realPools.get("N-" + shareName + ipList[i]);
                    bioPools[i] = (PoolableObjectPool) realPools.get("B-" + shareName + ipList[i]);
                    if (nioPools[i] != null) {
                        continue;
                    }
                }

                VenusBackendConnectionFactory nioFactory = new VenusBackendConnectionFactory();
                nioPools[i] = new PollingBackendConnectionPool("N-" + shareName + ipList[i], nioFactory, 8);
                bioPools[i] = new PoolableObjectPool();

                // bio
                VenusBIOConnectionFactory bioFactory = new VenusBIOConnectionFactory();

                bioFactory.setNeedPing(needPing);

                String temp[] = StringUtil.split(ipList[i], ":");
                if (temp.length > 1) {
                    nioFactory.setHost(temp[0]);
                    nioFactory.setPort(Integer.valueOf(temp[1]));

                    bioFactory.setHost(temp[0]);
                    bioFactory.setPort(Integer.valueOf(temp[1]));
                } else {
                    nioFactory.setHost(temp[0]);
                    nioFactory.setPort(16800);

                    bioFactory.setHost(temp[0]);
                    bioFactory.setPort(16800);
                }

                if (this.isEnableAsync()) {
                    nioFactory.setConnector(connector);
                    nioFactory.setMessageHandler(handler);
                    nioPools[i].init();
                    realPools.put(nioPools[i].getName(), nioPools[i]);
                }
                bioPools[i].setName("B-" + shareName + nioFactory.getHost() + ":" + nioFactory.getPort());
                bioPools[i].setFactory(bioFactory);
                bioPools[i].setTestOnBorrow(true);
                bioPools[i].setTestWhileIdle(true);
                bioPools[i].init();

                realPools.put(bioPools[i].getName(), bioPools[i]);
            }

            if (ipList.length > 1) {
                MultipleLoadBalanceObjectPool bioPool = new MultipleLoadBalanceObjectPool(MultipleLoadBalanceObjectPool.LOADBALANCING_ROUNDROBIN, bioPools);
                MultipleLoadBalanceBackendConnectionPool nioPool = new MultipleLoadBalanceBackendConnectionPool("N-V-" + ipAddress,
                        MultipleLoadBalanceObjectPool.LOADBALANCING_ROUNDROBIN, nioPools);
                bioPool.setName("B-V-" + ipAddress);
                nioPool.init();
                bioPool.init();

                realPools.put(bioPool.getName(), bioPool);
                realPools.put(nioPool.getName(), nioPool);
                container.setBioPool(bioPool);
                container.setNioPool(nioPool);
            } else {
                container.setBioPool(bioPools[0]);
                container.setNioPool(nioPools[0]);
            }
        } else {
            throw new IllegalArgumentException(" ipaddress cannot be null");
        }

        return container;
    }

    private RemoteContainer createRemoteContainer(Remote remote, Map<String, Object> realPools) throws Exception {
        RemoteContainer container = new RemoteContainer();
        FactoryConfig factoryConfig = remote.getFactory();
        if (factoryConfig == null) {
            throw new ConfigurationException(remote.getName() + " factory cannot be null");
        }
        PoolConfig poolConfig = remote.getPool();
        String ipAddress = factoryConfig.getIpAddressList();
        if (!StringUtil.isEmpty(ipAddress)) {
            String ipList[] = StringUtil.split(ipAddress, ", ");
            PoolableObjectPool bioPools[] = new PoolableObjectPool[ipList.length];
            BackendConnectionPool nioPools[] = new BackendConnectionPool[ipList.length];

            for (int i = 0; i < ipList.length; i++) {
                String shareName = remote.isShare() ? "SHARED-" : "";
                if (remote.isShare()) {
                    nioPools[i] = (PollingBackendConnectionPool) realPools.get("N-" + shareName + ipList[i]);
                    bioPools[i] = (PoolableObjectPool) realPools.get("B-" + shareName + ipList[i]);
                    if (nioPools[i] != null) {
                        continue;
                    }
                }

                VenusBackendConnectionFactory nioFactory = new VenusBackendConnectionFactory();
                VenusBIOConnectionFactory bioFactory = new VenusBIOConnectionFactory();
                if (remote.getAuthenticator() != null) {
                    bioFactory.setAuthenticator(remote.getAuthenticator());
                }

                nioPools[i] = new PollingBackendConnectionPool("N-" + shareName + ipList[i], nioFactory, 8);
                bioPools[i] = new PoolableObjectPool();
                if (poolConfig != null) {
                    BeanUtils.copyProperties(nioPools[i], poolConfig);
                    BeanUtils.copyProperties(bioPools[i], poolConfig);
                } else {
                    bioPools[i].setTestOnBorrow(true);
                    bioPools[i].setTestWhileIdle(true);
                }

                if (remote.getAuthenticator() != null) {
                    nioFactory.setAuthenticator(remote.getAuthenticator());
                    bioFactory.setAuthenticator(remote.getAuthenticator());
                }

                bioFactory.setNeedPing(needPing);
                if (factoryConfig != null) {

                    BeanUtils.copyProperties(nioFactory, factoryConfig);
                    BeanUtils.copyProperties(bioFactory, factoryConfig);
                }
                String temp[] = StringUtil.split(ipList[i], ":");
                if (temp.length > 1) {
                    nioFactory.setHost(temp[0]);
                    nioFactory.setPort(Integer.valueOf(temp[1]));

                    bioFactory.setHost(temp[0]);
                    bioFactory.setPort(Integer.valueOf(temp[1]));
                } else {
                    nioFactory.setHost(temp[0]);
                    nioFactory.setPort(16800);

                    bioFactory.setHost(temp[0]);
                    bioFactory.setPort(16800);
                }

                if (this.isEnableAsync()) {
                    nioFactory.setConnector(this.connector);
                    nioFactory.setMessageHandler(handler);
                    // nioPools[i].setName("n-connPool-"+nioFactory.getIpAddress());
                    nioPools[i].init();
                    realPools.put(nioPools[i].getName(), nioPools[i]);
                }
                bioPools[i].setName("B-" + shareName + bioFactory.getHost() + ":" + bioFactory.getPort());
                bioPools[i].setFactory(bioFactory);
                bioPools[i].init();
                realPools.put(bioPools[i].getName(), bioPools[i]);
            }

            if (ipList.length > 1) {
                MultipleLoadBalanceObjectPool bioPool = new MultipleLoadBalanceObjectPool(remote.getLoadbalance(), bioPools);
                MultipleLoadBalanceBackendConnectionPool nioPool = new MultipleLoadBalanceBackendConnectionPool(remote.getName(), remote.getLoadbalance(),
                        nioPools);

                bioPool.setName("B-V-" + remote.getName());

                container.setBioPool(bioPool);
                container.setNioPool(nioPool);
                bioPool.init();
                nioPool.init();

                realPools.put(bioPool.getName(), bioPool);
                realPools.put(nioPool.getName(), nioPool);
            } else {
                container.setBioPool(bioPools[0]);
                container.setNioPool(nioPools[0]);
            }
            container.setRemote(remote);
        } else {
            throw new IllegalArgumentException("remtoe=" + remote.getName() + ", ipaddress cannot be null");
        }

        return container;
    }

    public ServiceConfig getServiceConfig(Class<?> type) {
        return serviceConfig.get(type);
    }

    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {

        // register to resolvable dependency container
        if (beanFactory instanceof ConfigurableListableBeanFactory) {
            ConfigurableListableBeanFactory cbf = (ConfigurableListableBeanFactory) beanFactory;
            for (Map.Entry<Class<?>, Tuple<Object, RemotingInvocationHandler>> entry : servicesMap.entrySet()) {
                cbf.registerResolvableDependency(entry.getKey(), entry.getValue().left);
            }
            for (Map.Entry<String, Tuple<Object, RemotingInvocationHandler>> entry : serviceBeanMap.entrySet()) {
                cbf.registerSingleton(entry.getKey(), entry.getValue().left);
            }
        }
    }

    public void destroy() {
        if (shutdown) {
            return;
        }
        shutdown = true;
        if (connector != null) {
            if (connector.isAlive()) {
                connector.shutdown();
            }
        }
        if (connManager != null) {
            if (connManager.isAlive()) {
                connManager.shutdown();
            }
        }
    }

}
