package com.meidusa.venus.backend.services.xml;

import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.core.io.Resource;
import org.springframework.util.ResourceUtils;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.meidusa.toolkit.common.bean.BeanContext;
import com.meidusa.toolkit.common.bean.BeanContextBean;
import com.meidusa.toolkit.common.bean.config.ConfigUtil;
import com.meidusa.toolkit.common.bean.config.ConfigurationException;
import com.meidusa.toolkit.common.util.StringUtil;
import com.meidusa.venus.VenusMetaInfo;
import com.meidusa.venus.annotations.PerformanceLevel;
import com.meidusa.venus.annotations.util.AnnotationUtil;
import com.meidusa.venus.backend.interceptor.Configurable;
import com.meidusa.venus.backend.interceptor.Interceptor;
import com.meidusa.venus.backend.interceptor.InterceptorMapping;
import com.meidusa.venus.backend.interceptor.InterceptorStack;
import com.meidusa.venus.backend.interceptor.config.InterceptorConfig;
import com.meidusa.venus.backend.network.handler.CodeMapScanner;
import com.meidusa.venus.backend.services.AbstractServiceManager;
import com.meidusa.venus.backend.services.Endpoint;
import com.meidusa.venus.backend.services.Service;
import com.meidusa.venus.backend.services.SingletonService;
import com.meidusa.venus.backend.services.xml.bean.EndpointConfig;
import com.meidusa.venus.backend.services.xml.bean.InterceptorRef;
import com.meidusa.venus.backend.services.xml.bean.InterceptorStackConfig;
import com.meidusa.venus.backend.services.xml.bean.InterceptorStackRef;
import com.meidusa.venus.backend.services.xml.bean.PerformanceLogger;
import com.meidusa.venus.backend.services.xml.bean.ServiceConfig;
import com.meidusa.venus.backend.services.xml.bean.Venus;
import com.meidusa.venus.digester.DigesterRuleParser;
import com.meidusa.venus.exception.VenusConfigException;
import com.meidusa.venus.extension.athena.AthenaExtensionResolver;
import com.meidusa.venus.service.monitor.MonitorRuntime;
import com.meidusa.venus.service.monitor.MonitorService;
import com.meidusa.venus.service.monitor.ServerStatus;
import com.meidusa.venus.service.monitor.ServiceBean;
import com.meidusa.venus.service.registry.ServiceDefinition;
import com.meidusa.venus.service.registry.ServiceRegistry;
import com.meidusa.venus.util.VenusBeanUtilsBean;

public class XmlFileServiceManager extends AbstractServiceManager implements InitializingBean, BeanFactoryAware {
    private static Logger logger = LoggerFactory.getLogger(XmlFileServiceManager.class);
    private Resource[] configFiles;
    private BeanFactory beanFactory;
    private BeanContext beanContext;

    public Resource[] getConfigFiles() {
        return configFiles;
    }

    public void setConfigFiles(Resource... configFiles) {
        this.configFiles = configFiles;
    }

    public void init() {

    }

    @Override
    public void afterPropertiesSet() throws Exception {
        beanContext = new BackendBeanContext(beanFactory);
        BeanContextBean.getInstance().setBeanContext(beanContext);
        VenusBeanUtilsBean.setInstance(new BackendBeanUtilsBean(new ConvertUtilsBean(), new PropertyUtilsBean(), beanContext));
        AthenaExtensionResolver.getInstance().resolver();
        CodeMapScanner.getCodeMap();

        List<ServiceConfig> serviceConfigList = new ArrayList<ServiceConfig>();
        Map<String, InterceptorMapping> interceptors = new HashMap<String, InterceptorMapping>();
        Map<String, InterceptorStackConfig> interceptorStacks = new HashMap<String, InterceptorStackConfig>();
        loadVenusService(serviceConfigList, interceptors, interceptorStacks);
        loadMonitorService(interceptors, interceptorStacks);
        loadRegistryService(interceptors, interceptorStacks);
        initMonitor(serviceConfigList, interceptors, interceptorStacks);
    }



    private void loadVenusService(List<ServiceConfig> serviceConfigList, Map<String, InterceptorMapping> interceptors, Map<String, InterceptorStackConfig> interceptorStacks) {
        for (Resource config : configFiles) {
            RuleSet ruleSet = new FromXmlRuleSet(this.getClass().getResource("venusServerRule.xml"), new DigesterRuleParser());
            Digester digester = new Digester();
            digester.addRuleSet(ruleSet);

            try {
                InputStream is = config.getInputStream();
                Venus venus = (Venus) digester.parse(is);
                serviceConfigList.addAll(venus.getServiceConfigs());
                interceptors.putAll(venus.getInterceptors());
                interceptorStacks.putAll(venus.getInterceptorStatcks());
            } catch (Exception e) {
                throw new ConfigurationException("can not parser xml:" + config.getFilename(), e);
            }
        }
    }

    protected void loadMonitorService(Map<String, InterceptorMapping> interceptors, Map<String, InterceptorStackConfig> interceptorStatcks) {
        ServiceConfig monitorServiceConfig = new ServiceConfig();
        monitorServiceConfig.setActive(true);
        monitorServiceConfig.setType(MonitorService.class);
        monitorServiceConfig.setInstance(new MonitorService() {

            @Override
            public List<ServiceBean> getSerivces() {
                List<ServiceBean> list = new ArrayList<ServiceBean>();
                list.addAll(MonitorRuntime.getInstance().getServiceMap().values());
                return list;
            }

            @Override
            public ServerStatus getServerStatus() {
                ServerStatus status = new ServerStatus();
                status.setUptime(MonitorRuntime.getInstance().getUptime());
                
                return status;
            }

            @Override
            public String getVersion() {
                return VenusMetaInfo.VENUS_VERSION;
            }

        });

        loadService(monitorServiceConfig, interceptors, interceptorStatcks);
    }

    protected void loadRegistryService(Map<String, InterceptorMapping> interceptors, Map<String, InterceptorStackConfig> interceptorStatcks) {
        ServiceConfig serviceConfig = new ServiceConfig();
        serviceConfig.setActive(true);
        serviceConfig.setType(ServiceRegistry.class);
        serviceConfig.setInstance(new ServiceRegistry() {

            @Override
            public List<ServiceDefinition> getServiceDefinitions() {
                List<ServiceDefinition> sdList = new ArrayList<ServiceDefinition>();
                Collection<Service> list = XmlFileServiceManager.this.getServices();
                for (Service service : list) {
                    ServiceDefinition definition = new ServiceDefinition();
                    definition.setActive(service.isActive());
                    definition.setName(service.getName());
                    definition.setDescription(service.getDescription());
                    if (service.getVersionRange() != null) {
                        definition.setVersionRange(service.getVersionRange().toString());
                    }
                    sdList.add(definition);
                }
                return sdList;
            }

            @Override
            public ServiceDefinition getServiceDefinition(String name, int version) {

                Service service = XmlFileServiceManager.this.services.get(name);
                if (service.getVersionRange().contains(version)) {
                    ServiceDefinition definition = new ServiceDefinition();
                    definition.setActive(service.isActive());
                    definition.setName(service.getName());
                    definition.setDescription(service.getDescription());
                    if (service.getVersionRange() != null) {
                        definition.setVersionRange(service.getVersionRange().toString());
                    }
                    return definition;
                }

                return null;
            }

        });

        loadService(serviceConfig, interceptors, interceptorStatcks);
    }

    private void initMonitor(List<ServiceConfig> serviceConfigList, Map<String, InterceptorMapping> interceptors, Map<String, InterceptorStackConfig> interceptorStacks) {
        for (ServiceConfig config : serviceConfigList) {
            Service service = loadService(config, interceptors, interceptorStacks);
            Map<String, Collection<Endpoint>> ends = service.getEndpoints().asMap();
            for (Map.Entry<String, Collection<Endpoint>> entry : ends.entrySet()) {
                if (entry.getValue() != null && !entry.getValue().isEmpty()) {
                    MonitorRuntime.getInstance().initEndPoint(service.getName(), entry.getValue().iterator().next().getName());
                }
            }
        }
    }

    protected Service loadService(ServiceConfig config, Map<String, InterceptorMapping> interceptors, Map<String, InterceptorStackConfig> interceptorStatcks) {
        SingletonService service = new SingletonService();
        service.setType(config.getType());
        service.setInstance(config.getInstance());
        Class<?> type = config.getType();
        type.cast(config.getInstance());
        service.setActive(config.isActive());
        service.setVersionRange(config.getVersionRange());

        com.meidusa.venus.annotations.Service serviceAnnotation = type.getAnnotation(com.meidusa.venus.annotations.Service.class);
        
        if(serviceAnnotation == null){
        	logger.error("Service annotation not found in class="+type.getClass());
        	throw new VenusConfigException("Service annotation not found in class="+type.getClass());
        }

        service.setAthenaFlag(serviceAnnotation.athenaFlag());
        
        if (!serviceAnnotation.name().isEmpty()) {
            service.setName(serviceAnnotation.name());
        } else {
            service.setName(type.getCanonicalName());
        }

        service.setDescription(serviceAnnotation.description());

        // cache all methods
        Method[] methods = service.getType().getMethods();
        Multimap<String, Endpoint> endpoints = HashMultimap.create();
        for (Method method : methods) {
            if (method.isAnnotationPresent(com.meidusa.venus.annotations.Endpoint.class)) {
                Endpoint ep = loadEndpoint(method);

                EndpointConfig endpointConfig = config.getEndpointConfig(ep.getName());

                String id = (endpointConfig == null ? config.getInterceptorStack() : endpointConfig.getInterceptorStack());
                Map<String, InterceptorConfig> interceptorConfigs = null;
                if (endpointConfig != null) {
                    ep.setActive(endpointConfig.isActive());
                    if (endpointConfig.getTimeWait() > 0) {
                        ep.setTimeWait(endpointConfig.getTimeWait());
                    }
                    interceptorConfigs = endpointConfig.getInterceptorConfigs();
                }

                // ignore 'null' or empty intercept stack name
                if (!StringUtil.isEmpty(id) && !"null".equalsIgnoreCase(id)) {
                    List<InterceptorMapping> list = new ArrayList<InterceptorMapping>();
                    InterceptorStackConfig stackConfig = interceptorStatcks.get(id);
                    if (stackConfig == null) {
                        throw new VenusConfigException("interceptor stack not found with name=" + id);
                    }
                    InterceptorStack stack = new InterceptorStack();
                    stack.setName(stackConfig.getName());

                    loadInterceptors(interceptorStatcks, interceptors, id, list, interceptorConfigs, service.getType(), ep.getName());
                    stack.setInterceptors(list);
                    ep.setInterceptorStack(stack);
                }

                PerformanceLogger pLogger = null;
                if (endpointConfig != null) {
                    pLogger = endpointConfig.getPerformanceLogger();
                }

                if (pLogger == null) {
                    PerformanceLevel pLevel = AnnotationUtil.getAnnotation(ep.getMethod().getAnnotations(), PerformanceLevel.class);
                    if (pLevel != null) {
                        pLogger = new PerformanceLogger();
                        pLogger.setError(pLevel.error());
                        pLogger.setInfo(pLevel.info());
                        pLogger.setWarn(pLevel.warn());
                        pLogger.setPrintParams(pLevel.printParams());
                        pLogger.setPrintResult(pLevel.printResult());
                    }
                }
                ep.setPerformanceLogger(pLogger);

                ep.setService(service);
                if (logger.isInfoEnabled()) {
                    logger.info("Add Endpoint: " + ep.getService().getName() + "." + ep.getName());
                }
                endpoints.put(ep.getName(), ep);
            }
        }
        service.setEndpoints(endpoints);

        this.services.put(service.getName(), service);

        // register to resolvable dependency container

        if (beanFactory instanceof ConfigurableListableBeanFactory) {
            ConfigurableListableBeanFactory cbf = (ConfigurableListableBeanFactory) beanFactory;
            cbf.registerResolvableDependency(service.getType(), service.getInstance());
        }
        return service;
    }

    protected void loadInterceptors(Map<String, InterceptorStackConfig> interceptorStatcks, Map<String, InterceptorMapping> interceptors, String id,
            List<InterceptorMapping> result, Map<String, InterceptorConfig> configs, Class<?> clazz, String ep) throws VenusConfigException {
        InterceptorStackConfig stackConfig = interceptorStatcks.get(id);
        if (stackConfig == null) {
            throw new VenusConfigException("interceptor stack not found with name=" + id);
        }
        for (Object s : stackConfig.getInterceptors()) {
            if (s instanceof InterceptorRef) {
                InterceptorMapping mapping = interceptors.get(((InterceptorRef) s).getName());
                if (mapping == null) {
                    throw new VenusConfigException("interceptor not found with name=" + s);
                }
                Interceptor interceptor = mapping.getInterceptor();
                if (configs != null) {
                    InterceptorConfig config = configs.get(mapping.getName());
                    if (config != null) {
                        if (interceptor instanceof Configurable) {
                            ((Configurable) interceptor).processConfig(clazz, ep, config);
                        }
                    }
                }
                result.add(mapping);
            } else if (s instanceof InterceptorStackRef) {
                loadInterceptors(interceptorStatcks, interceptors, ((InterceptorStackRef) s).getName(), result, configs, clazz, ep);
            } else {
                throw new VenusConfigException("unknow interceptor config with name=" + s);
            }
        }
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
    }

}
