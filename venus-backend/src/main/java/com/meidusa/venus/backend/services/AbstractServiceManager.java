package com.meidusa.venus.backend.services;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.TypeHandler;
import org.apache.commons.lang.ArrayUtils;

import com.meidusa.toolkit.util.StringUtil;
import com.meidusa.venus.annotations.util.AnnotationUtil;
import com.meidusa.venus.backend.context.RequestContext;
import com.meidusa.venus.exception.ConvertException;
import com.meidusa.venus.exception.EndPointNotFoundException;
import com.meidusa.venus.exception.ServiceNotFoundException;
import com.meidusa.venus.exception.SystemParameterRequiredException;
import com.meidusa.venus.exception.ServiceDefinitionException;
import com.meidusa.venus.util.Utils;

/**
 * 
 * @author Struct
 * 
 */
public abstract class AbstractServiceManager implements ServiceManager {
    private boolean supportOverload = false;
    protected final Map<String, Service> services = new HashMap<String, Service>();

    /**
     * @return the serviceInstancePool
     */
    public Collection<Service> getServices() {
        return services.values();
    }

    @Override
    public Service getService(String serviceName) throws ServiceNotFoundException {
        if (serviceName == null) {
            throw new ServiceNotFoundException("Cannot find service with null");
        }
        if (!services.containsKey(serviceName)) {
            throw new ServiceNotFoundException("No service named " + serviceName);
        }

        return services.get(serviceName);
    }

    @Override
    public Endpoint getEndpoint(String apiName) throws ServiceNotFoundException, EndPointNotFoundException, SystemParameterRequiredException {

        if (StringUtil.isEmpty(apiName)) {
            throw new EndPointNotFoundException("No method named " + apiName);
        } else {

        }
        int index = apiName.lastIndexOf(".");
        if (index > 0) {
            String serviceName = apiName.substring(0, index);
            String endpointName = apiName.substring(index + 1);

            Service service = services.get(serviceName);
            if (service == null) {
                throw new ServiceNotFoundException("No service named " + serviceName);
            }

            // find endpoint
            Collection<Endpoint> eps = service.getEndpoints().get(endpointName);
            if (eps == null || eps.isEmpty()) {
                throw new EndPointNotFoundException("No method named " + endpointName);
            }
            return eps.iterator().next();
        }
        throw new EndPointNotFoundException("No method named " + apiName);
    }

    @Override
    public Endpoint getEndpoint(String serviceName, String endpointName, String[] paramNames) throws ServiceNotFoundException, EndPointNotFoundException,
            SystemParameterRequiredException {
        // find service
        Service service = services.get(serviceName);
        if (service == null) {
            throw new ServiceNotFoundException("No service named " + serviceName);
        }

        // find endpoint
        Collection<Endpoint> eps = service.getEndpoints().get(endpointName);
        if (eps == null || eps.isEmpty()) {
            throw new EndPointNotFoundException("No method named " + endpointName);
        }

        if (supportOverload) {
            Endpoint ep = findExactEndpoint(eps, paramNames);

            if (ep == null) {
                throw new EndPointNotFoundException("method not found, service=" + serviceName + "." + endpointName + " annotated with params: "
                        + ArrayUtils.toString(paramNames));
            }

            return ep;
        } else {
            return eps.iterator().next();
        }

    }

    /**
     * invoked when supportOverload, check parameters
     * 
     * @param endpoints
     * @param paramNames
     * @return
     */
    private Endpoint findExactEndpoint(Collection<Endpoint> endpoints, String[] paramNames) {
        if (endpoints.size() == 1) {
            Endpoint ep = endpoints.iterator().next();

            // modified on 2010-3-19, check if required parameter omitted
            String[] requiredParameterNames = ep.getRequiredParameterNames();

            if (Utils.arrayContains(requiredParameterNames, paramNames)) {
                return ep;
            } else {
                return null;
            }

        }

        Iterator<Endpoint> it = endpoints.iterator();
        while (it.hasNext()) {
            Endpoint ep = it.next();
            if (ArrayUtils.isSameLength(ep.getParameters(), paramNames)) {
                String[] epParameterNames = ep.getParameterNames();
                if (Utils.arrayEquals(paramNames, epParameterNames)) {
                    return ep;
                }
            }
        }
        return null;
    }

    /**
     * @return the supportOverload
     */
    public boolean isSupportOverload() {
        return supportOverload;
    }

    /**
     * @param supportOverload the supportOverload to set
     */
    public void setSupportOverload(boolean supportOverload) {
        this.supportOverload = supportOverload;
    }

    /**
     * 
     * @param method
     * @return
     * @throws ServiceDefinitionException
     * @throws ConvertException
     */
    protected Endpoint loadEndpoint(Method method) throws ServiceDefinitionException, ConvertException {
        Endpoint ep = new Endpoint();
        ep.setMethod(method);

        com.meidusa.venus.annotations.Endpoint endpointAnnotation = method.getAnnotation(com.meidusa.venus.annotations.Endpoint.class);

        if (!endpointAnnotation.name().isEmpty()) {
            ep.setName(endpointAnnotation.name());
        } else {
            ep.setName(method.getName());
        }
        ep.setTimeWait(endpointAnnotation.timeWait());
        ep.setVoid(method.getReturnType().equals(void.class));

        Type[] paramTypes = method.getGenericParameterTypes();

        // 判断最后一个参数是否是ctx
        if (paramTypes.length > 0 && paramTypes[paramTypes.length - 1] == RequestContext.class) {
            ep.setHasCtxParam(true);
        }

        Annotation[][] paramAnnotations = method.getParameterAnnotations();

        List<Parameter> params = new ArrayList<Parameter>(paramTypes.length);
        for (int i = 0; i < paramTypes.length; i++) {
            Parameter param = loadParameter(method, paramTypes[i], paramAnnotations[i]);

            // 只暴露出@Param的方法
            if (param != null) {
                params.add(param);
            }
        }

        ep.setParameters(params.toArray(new Parameter[0]));

        return ep;

    }

    protected Parameter loadParameter(Method method, Type paramType, Annotation[] annotations) throws ServiceDefinitionException, ConvertException {
        Parameter p = new Parameter();

        // type
        p.setType(paramType);

        com.meidusa.venus.annotations.Param paramAnnotation = AnnotationUtil.getAnnotation(annotations, com.meidusa.venus.annotations.Param.class);

        if (paramAnnotation == null) {
            throw new ServiceDefinitionException("service=" + method.getDeclaringClass().getName() + ",method=" + method.getName()
                    + " ,one more param annotaions is absent");
        }

        // name
        p.setParamName(paramAnnotation.name());

        // optional or not
        p.setOptional(paramAnnotation.optional());

        // default value
        if (!paramAnnotation.defaultValue().isEmpty()) {
            try {
                p.setDefaultValue(TypeHandler.createValue(paramAnnotation.defaultValue(), paramType));
            } catch (ParseException e) {
                throw new ConvertException("parseError", e);
            }
        }

        return p;
    }
}
