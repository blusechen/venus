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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VenusClient {
    private List<ServiceConfig> serviceConfigs = new ArrayList<ServiceConfig>();
    private Map<String, Remote> remoteMap = new HashMap<String, Remote>();

    public void addService(ServiceConfig config) {
        serviceConfigs.add(config);
    }

    public void addRemote(Remote remote) {
        remoteMap.put(remote.getName(), remote);
    }

    public List<ServiceConfig> getServiceConfigs() {
        return serviceConfigs;
    }

    public Map<String, Remote> getRemoteMap() {
        return remoteMap;
    }

}
