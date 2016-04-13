/**
 * 
 */
package com.meidusa.venus.backend.services;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.meidusa.venus.annotations.util.AnnotationUtil;
import com.meidusa.venus.exception.ConvertException;
import com.meidusa.venus.exception.ServiceDefinitionException;
import com.meidusa.venus.exception.VenusConfigException;

public class SimpleServiceManager extends AbstractServiceManager implements InitializingBean {
    private static Logger logger = LoggerFactory.getLogger(SimpleServiceManager.class);
    private List<Object> serviceInstances;

    public void afterPropertiesSet() throws Exception {
        for (Object instance : serviceInstances) {
            Service[] loadedServices = loadService(instance);

            if (loadedServices == null) {
                continue;
            }

            for (Service service : loadedServices) {
                services.put(service.getName(), service);
            }
        }
    }

    /**
     * @return the serviceInstances
     */
    public List<Object> getServiceInstances() {
        return serviceInstances;
    }

    /**
     * @param serviceInstances the serviceInstances to set
     */
    public void setServiceInstances(List<Object> serviceInstances) {
        this.serviceInstances = serviceInstances;
    }

    public Map<String, Service> getServicesAsMap() {
        return services;
    }

    /**
     * @throws ConvertException
     * 
     * 
     */
    protected Service[] loadService(Object instance) throws ServiceDefinitionException, ConvertException {
        Class<?>[] interfaces = instance.getClass().getInterfaces();
        Class<?>[] types = AnnotationUtil.getAnnotatedClasses(interfaces, com.meidusa.venus.annotations.Service.class);

        if (types != null && types.length > 0) {
            Service[] services = new Service[types.length];
            for (int i = 0; i < types.length; i++) {
                services[i] = loadService0(types[i], instance);
            }
            return services;
        }

        return null;
    }

    protected Service loadService0(Class<?> type, Object instance) throws ServiceDefinitionException, ConvertException {
        SingletonService service = new SingletonService();
        if (logger.isInfoEnabled()) {
            logger.info("Loading From: " + instance.getClass().getCanonicalName());
        }

        // set type

        if (type == null) {
            throw new VenusConfigException(instance.getClass().getCanonicalName());
        }
        if (logger.isInfoEnabled()) {
            logger.info("Load Type: " + type.getCanonicalName());
        }
        service.setType(type);

        // set name
        com.meidusa.venus.annotations.Service serviceAnnotation = type.getAnnotation(com.meidusa.venus.annotations.Service.class);
        if (!serviceAnnotation.name().isEmpty()) {

            service.setName(serviceAnnotation.name());
        } else {
            service.setName(type.getCanonicalName());

        }
        if (logger.isInfoEnabled()) {
            logger.info("Use Name: " + service.getName());
        }

        // cache all methods
        Method[] methods = type.getMethods();
        Multimap<String, Endpoint> endpoints = HashMultimap.create();
        for (Method method : methods) {
            if (method.isAnnotationPresent(com.meidusa.venus.annotations.Endpoint.class)) {
                Endpoint ep = loadEndpoint(method);
                ep.setService(service);
                if (logger.isInfoEnabled()) {
                    logger.info("Add Endpoint: " + ep.getService().getName() + "." + ep.getName());
                }
                endpoints.put(ep.getName(), ep);
            }
        }
        service.setEndpoints(endpoints);

        // inject instance
        service.setInstance(instance);

        return service;
    }
}
