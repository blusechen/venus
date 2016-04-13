package com.meidusa.venus.service.monitor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ServiceBean {

    private String name;

    private transient Map<String, PerformanceBean> endpointPerformance = new HashMap<String, PerformanceBean>();

    private List<PerformanceBean> endpoints = new ArrayList<PerformanceBean>();

    public List<PerformanceBean> getEndpoints() {
    	synchronized (endpointPerformance) {
    		List<PerformanceBean> endpoints = new ArrayList<PerformanceBean>();
    		endpoints.addAll(this.endpoints);
    		return endpoints;
		}
    }

    public void setEndpoints(List<PerformanceBean> endpoints) {
        this.endpoints = endpoints;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Map<String, PerformanceBean> getEndpointPerformance() {
        return endpointPerformance;
    }

    public PerformanceBean getEndpointPerformance(String name) {
        PerformanceBean bean = endpointPerformance.get(name);
        if (bean == null) {
            synchronized (endpointPerformance) {
                bean = endpointPerformance.get(name);
                if (bean == null) {
                    bean = new PerformanceBean();
                    bean.setName(name);
                    endpoints.add(bean);
                    endpointPerformance.put(name, bean);
                }
            }
        }
        return bean;
    }

}
