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

package com.meidusa.venus.frontend.http;

import java.beans.PropertyDescriptor;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.BeanUtilsBean;
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
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.util.ResourceUtils;

import com.meidusa.toolkit.common.bean.BeanContext;
import com.meidusa.toolkit.common.bean.BeanContextBean;
import com.meidusa.toolkit.common.bean.config.ConfigUtil;
import com.meidusa.toolkit.common.bean.config.ConfigurationException;
import com.meidusa.toolkit.common.poolable.MultipleLoadBalanceObjectPool;
import com.meidusa.toolkit.common.poolable.ObjectPool;
import com.meidusa.toolkit.common.poolable.PoolableObjectPool;
import com.meidusa.toolkit.net.BackendConnectionPool;
import com.meidusa.toolkit.util.StringUtil;
import com.meidusa.venus.client.ServiceFactory;
import com.meidusa.venus.client.xml.bean.FactoryConfig;
import com.meidusa.venus.client.xml.bean.PoolConfig;
import com.meidusa.venus.client.xml.bean.Remote;
import com.meidusa.venus.client.xml.bean.ServiceConfig;
import com.meidusa.venus.client.xml.bean.VenusClient;
import com.meidusa.venus.digester.DigesterRuleParser;
import com.meidusa.venus.io.network.VenusBIOConnectionFactory;
import com.meidusa.venus.io.packet.PacketConstant;
import com.meidusa.venus.util.FileWatchdog;
import com.meidusa.venus.util.VenusBeanUtilsBean;

public class VenusPoolFactory implements BeanFactoryAware, InitializingBean {
    private static Logger logger = LoggerFactory.getLogger(VenusPoolFactory.class);
    private BeanFactory beanFactory;
    private String[] configFiles;
    private BeanContext beanContext;
    private List<ObjectPool> realPools = new ArrayList<ObjectPool>();
    private boolean needPing = false;
    private Timer reloadTimer = new Timer();
    private boolean enableReload = true;
    private ObjectPool pool = null;

    public ObjectPool getPool() {
        return pool;
    }

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

    public String[] getConfigFiles() {
        return configFiles;
    }

    public void setConfigFiles(String[] configFiles) {
        this.configFiles = configFiles;
    }

    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
    }

    @SuppressWarnings("unchecked")
    public void afterPropertiesSet() throws Exception {
        logger.trace("current Venus Client id=" + PacketConstant.VENUS_CLIENT_ID);

        beanContext = new BeanContext() {
            public Object getBean(String beanName) {
                if (beanFactory != null) {
                    return beanFactory.getBean(beanName);
                } else {
                    return null;
                }
            }

            public Object createBean(Class clazz) throws Exception {
                if (beanFactory instanceof AutowireCapableBeanFactory) {
                    AutowireCapableBeanFactory factory = (AutowireCapableBeanFactory) beanFactory;
                    return factory.autowire(clazz, AutowireCapableBeanFactory.AUTOWIRE_BY_NAME, false);
                }
                return null;
            }
        };

        BeanContextBean.getInstance().setBeanContext(beanContext);
        VenusBeanUtilsBean.setInstance(new BeanUtilsBean(new ConvertUtilsBean(), new PropertyUtilsBean()) {

            public void setProperty(Object bean, String name, Object value) throws IllegalAccessException, InvocationTargetException {
                if (value instanceof String) {
                    PropertyDescriptor descriptor = null;
                    try {
                        descriptor = getPropertyUtils().getPropertyDescriptor(bean, name);
                        if (descriptor == null) {
                            return; // Skip this property setter
                        } else {
                            if (descriptor.getPropertyType().isEnum()) {
                                Class<Enum> clazz = (Class<Enum>) descriptor.getPropertyType();
                                value = Enum.valueOf(clazz, (String) value);
                            } else {
                                Object temp = null;
                                try {
                                    temp = ConfigUtil.filter((String) value, beanContext);
                                } catch (Exception e) {
                                }
                                if (temp == null) {
                                    temp = ConfigUtil.filter((String) value);
                                }
                                value = temp;
                            }
                        }
                    } catch (NoSuchMethodException e) {
                        return; // Skip this property setter
                    }
                }
                super.setProperty(bean, name, value);
            }

        });

        reloadConfiguration();

        __RELOAD: {
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
                VenusPoolFactory.this.reloadConfiguration();
            } catch (Exception e) {
                VenusPoolFactory.logger.error("reload configuration error", e);
            }
        }

    }

    private synchronized void reloadConfiguration() throws Exception {

        final List<ObjectPool> realPools = new ArrayList<ObjectPool>();
        try {
            loadConfiguration(realPools);
        } catch (Exception e) {
            reloadTimer.schedule(new ClosePoolTask(realPools), 1000 * 30);
            throw e;
        }
        final List<ObjectPool> oldPools = this.realPools;
        this.realPools = realPools;
        reloadTimer.schedule(new ClosePoolTask(oldPools), 1000 * 30);

    }

    class ClosePoolTask extends TimerTask {
        List<ObjectPool> pools;

        public ClosePoolTask(List<ObjectPool> pools) {
            this.pools = pools;
        }

        @Override
        public void run() {
            for (Object pool : pools) {
                try {
                    if (pool instanceof ObjectPool) {
                        ((ObjectPool) pool).close();
                    } else if (pool instanceof BackendConnectionPool) {
                        ((BackendConnectionPool) pool).close();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void loadConfiguration(List<ObjectPool> realPools) throws Exception {
        VenusClient all = new VenusClient();
        for (String configFile : configFiles) {
            configFile = (String) ConfigUtil.filter(configFile);
            RuleSet ruleSet = new FromXmlRuleSet(ServiceFactory.class.getResource("venusClientRule.xml"), new DigesterRuleParser());
            Digester digester = new Digester();
            digester.setValidating(false);
            digester.addRuleSet(ruleSet);

            try {
                InputStream is = ResourceUtils.getURL(configFile.trim()).openStream();
                VenusClient venus = (VenusClient) digester.parse(is);
                for (ServiceConfig config : venus.getServiceConfigs()) {
                    if (config.getType() == null) {
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
            pool = createObjectPool(entry.getValue(), realPools);
        }
    }

    private ObjectPool createObjectPool(Remote remote, List<ObjectPool> realPools) throws Exception {
        FactoryConfig factoryConfig = remote.getFactory();
        if (factoryConfig == null) {
            throw new ConfigurationException(remote.getName() + " factory cannot be null");
        }
        PoolConfig poolConfig = remote.getPool();
        String ipAddress = factoryConfig.getIpAddressList();
        if (!StringUtil.isEmpty(ipAddress)) {
            String ipList[] = StringUtil.split(ipAddress, ", ");
            PoolableObjectPool bioPools[] = new PoolableObjectPool[ipList.length];
            for (int i = 0; i < ipList.length; i++) {
                VenusBIOConnectionFactory bioFactory = new VenusBIOConnectionFactory();
                if (remote.getAuthenticator() != null) {
                    bioFactory.setAuthenticator(remote.getAuthenticator());
                }

                bioPools[i] = new PoolableObjectPool();
                if (poolConfig != null) {
                    BeanUtils.copyProperties(bioPools[i], poolConfig);
                }

                bioFactory.setNeedPing(needPing);
                if (factoryConfig != null) {
                    BeanUtils.copyProperties(bioFactory, factoryConfig);
                }
                String temp[] = StringUtil.split(ipList[i], ":");
                if (temp.length > 1) {
                    bioFactory.setHost(temp[0]);
                    bioFactory.setPort(Integer.valueOf(temp[1]));
                } else {
                    bioFactory.setHost(temp[0]);
                    bioFactory.setPort(16800);
                }
                bioPools[i].setName("b-connPool-" + bioFactory.getHost());
                bioPools[i].setFactory(bioFactory);
                bioPools[i].init();
                realPools.add(bioPools[i]);
            }

            MultipleLoadBalanceObjectPool bioPool = new MultipleLoadBalanceObjectPool(remote.getLoadbalance(), bioPools);
            bioPool.setName("b-multi-connPool-" + remote.getName());

            bioPool.init();
            realPools.add(bioPool);
            return bioPool;
        } else {
            throw new IllegalArgumentException("remtoe=" + remote.getName() + ", ipaddress cannot be null");
        }
    }

    public synchronized void destroy() {
        for (ObjectPool pool : realPools) {
            try {
                pool.close();
            } catch (Exception e) {
            }
        }
    }

}
