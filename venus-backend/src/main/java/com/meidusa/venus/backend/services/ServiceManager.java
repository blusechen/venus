/**
 * 
 */
package com.meidusa.venus.backend.services;

import java.util.Collection;

import com.meidusa.venus.exception.ServiceNotFoundException;

/**
 * find a service instance from pool
 * 
 * @author Sun Ning
 * @since 2010-3-5
 */
public interface ServiceManager extends EndpointLocator {

    public Service getService(String serviceName) throws ServiceNotFoundException;

    public Collection<Service> getServices();
}
