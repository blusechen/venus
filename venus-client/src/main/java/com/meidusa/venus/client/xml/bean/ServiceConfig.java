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
package com.meidusa.venus.client.xml.bean;

import java.util.HashMap;
import java.util.Map;

public class ServiceConfig {
    private String remote;
    private Class<?> type;
    private Object instance;
    private String beanName;
    private String ipAddressList;
    private Map<String, EndpointConfig> endPointMap = new HashMap<String, EndpointConfig>();
    private int timeWait;
    private boolean enabled = true;
    
    public int getTimeWait() {
        return timeWait;
    }

    public void setTimeWait(int soTimeout) {
        this.timeWait = soTimeout;
    }

    public String getIpAddressList() {
        return ipAddressList;
    }

    public void setIpAddressList(String ipAddressList) {
        this.ipAddressList = ipAddressList;
    }

    public String getRemote() {
        return remote;
    }

    public void setRemote(String remote) {
        this.remote = remote;
    }

    public Class<?> getType() {
        return type;
    }

    public void setType(Class<?> type) {
        this.type = type;
    }

    public Object getInstance() {
        return instance;
    }

    public void setInstance(Object implement) {
        this.instance = implement;
    }

    public void addEndPointConfig(EndpointConfig config) {
        endPointMap.put(config.getName(), config);
    }

    public EndpointConfig getEndpointConfig(String key) {
        return endPointMap.get(key);
    }

    public String getBeanName() {
        return beanName;
    }

    public void setBeanName(String beanName) {
        this.beanName = beanName;
    }

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

}
