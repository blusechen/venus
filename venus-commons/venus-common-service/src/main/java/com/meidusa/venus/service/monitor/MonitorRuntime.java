package com.meidusa.venus.service.monitor;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class MonitorRuntime {
    static MonitorRuntime runtime = new MonitorRuntime();
    private Date uptime = new Date();
    private Map<String, ServiceBean> serviceMap = new HashMap<String, ServiceBean>();

    public static MonitorRuntime getInstance() {
        return runtime;
    }

    public Date getUptime() {
        return uptime;
    }

    public Map<String, ServiceBean> getServiceMap() {
        return serviceMap;
    }

    private MonitorRuntime() {
    };

    public void calculateAverage(String serviceName, String endpoint, long current,boolean isError) {
        getPerformanceBean(serviceName, endpoint).calculateAverage(current,isError);
    }

    public void initEndPoint(String serviceName, String endpoint) {
        getPerformanceBean(serviceName, endpoint);
    }

    private PerformanceBean getPerformanceBean(String serviceName, String endpoint) {
        ServiceBean bean = serviceMap.get(serviceName);
        if (bean == null) {
            synchronized (serviceMap) {
                bean = serviceMap.get(serviceName);
                if (bean == null) {
                    bean = new ServiceBean();
                    bean.setName(serviceName);
                    serviceMap.put(serviceName, bean);
                }
            }
        }

        PerformanceBean performance = bean.getEndpointPerformance(endpoint);
        return performance;
    }
}
