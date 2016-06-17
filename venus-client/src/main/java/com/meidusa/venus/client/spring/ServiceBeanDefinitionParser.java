package com.meidusa.venus.client.spring;

import com.meidusa.toolkit.util.StringUtil;
import com.meidusa.venus.annotations.Service;
import com.meidusa.venus.annotations.util.AnnotationUtil;
import com.meidusa.venus.client.nio.ServiceManager;
import com.meidusa.venus.client.nio.config.ServiceConfig;
import com.meidusa.venus.client.spring.exception.VenusServiceInterfaceNotFoundException;
import com.meidusa.venus.exception.DefaultVenusException;
import com.meidusa.venus.io.authenticate.Authenticator;
import com.meidusa.venus.io.authenticate.DummyAuthenticator;
import com.meidusa.venus.io.authenticate.UserPasswordAuthenticator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by huawei on 5/17/16.
 */
public class ServiceBeanDefinitionParser implements BeanDefinitionParser {

    private static Logger logger = LoggerFactory.getLogger(ServiceBeanDefinitionParser.class);

    @Override
    public BeanDefinition parse(Element element, ParserContext parserContext) {
        String servicesId = element.getAttribute(NodeConstants.ATTRIBUTE_SERVICES_ID);
        String refRegistryId = element.getAttribute(NodeConstants.ATTRIBUTE_SERVICES_REF_REGISTRY_ID_NAME);
        Assert.hasLength(servicesId, "服务管理ID不能为空");
        Assert.hasLength(refRegistryId, "服务注册中心ID不能为空");
        List<ServiceConfig> serviceConfigs = createServiceConfigList(element.getChildNodes());
        RootBeanDefinition rbd = new RootBeanDefinition();
        rbd.setBeanClass(ServiceManager.class);
        rbd.getPropertyValues().add("serviceConfigList", serviceConfigs);
        rbd.getPropertyValues().add("refRegistryId", refRegistryId);
        rbd.setDependsOn(new String[]{refRegistryId});
        parserContext.getRegistry().registerBeanDefinition(servicesId, rbd);
        return rbd;
    }

    private List<ServiceConfig> createServiceConfigList(NodeList childNodes) {
        List<ServiceConfig> serviceConfigs = new ArrayList<ServiceConfig>();
        if (childNodes == null || childNodes.getLength() == 0) {
            return serviceConfigs;
        }
        int size = childNodes.getLength();
        Authenticator authenticator = null;
        for (int i = 0; i < size; i++) {
            Node node = childNodes.item(i);
            if (node instanceof Element && node.getLocalName().equals(NodeConstants.ELEMENT_SERVICE_NAME)) {
                Element serviceElement = (Element) node;
                String serviceInterface = serviceElement.getAttribute(NodeConstants.ATTRIBUTE_SERVICE_INTERFACE_NAME);
                Class<?> serviceClass;
                try {
                    serviceClass = Class.forName(serviceInterface);
                } catch (ClassNotFoundException e) {
                    throw new VenusServiceInterfaceNotFoundException();
                }
                if (!serviceClass.isInterface()) {
                    throw new DefaultVenusException(0, serviceInterface + " must be interface");
                }
                Service serviceAnnotation = AnnotationUtil.getAnnotation(serviceClass.getDeclaredAnnotations(), Service.class);
                if (serviceAnnotation == null) {
                    throw new DefaultVenusException(0, "@Service annotation must be present in service interface");
                }

                String serviceName = serviceAnnotation.name();

                if (!StringUtils.hasLength(serviceName)) {
                    serviceName = serviceClass.getName();
                }

                String overrideAttr = serviceElement.getAttribute(NodeConstants.ATTRIBUTE_SERVICE_OVERRIDE_NAME);
                String addressListAttr = serviceElement.getAttribute(NodeConstants.ATTRIBUTE_SERVICE_ADDRESS_LIST_NAME);
                String maxActiveAttr = serviceElement.getAttribute(NodeConstants.ATTRIBUTE_SERVICE_MAX_ACTIVE_NAME);
                String maxIdleAttr = serviceElement.getAttribute(NodeConstants.ATTRIBUTE_SERVICE_MAX_IDLE_NAME);
                String minIdleAttr = serviceElement.getAttribute(NodeConstants.ATTRIBUTE_SERVICE_MIN_IDLE_NAME);
                String minEvictableIdleTimeMillisAttr = serviceElement.getAttribute(NodeConstants.ATTRIBUTE_SERVICE_MIN_EVICTABLE_IDLE_TIME_MILLIS_NAME);
                String timeBetweenEvictionRunsMillsAttr = serviceElement.getAttribute(NodeConstants.ATTRIBUTE_SERVICE_TIME_BETWEEN_EVICTION_RUNS_MILLS_NAME);
                String testOnBorrowAttr = serviceElement.getAttribute(NodeConstants.ATTRIBUTE_SERVICE_TEST_ON_BORROW_NAME);
                String testOnWhileIdleAttr = serviceElement.getAttribute(NodeConstants.ATTRIBUTE_SERVICE_TEST_WHILE_IDLE);
                String timeWaitAttr = serviceElement.getAttribute(NodeConstants.ATTRIBUTE_SERVICE_TIME_WAIT);

                ServiceConfig config = new ServiceConfig();
                config.setServiceInterface(serviceClass);
                config.setServiceAnnotation(serviceAnnotation);
                config.setServiceName(serviceName);
                config.setVersion(serviceAnnotation.version());
                config.setOverride(overrideAttr == null ? false : Boolean.parseBoolean(overrideAttr));

                if (config.isOverride()) {
                    if (addressListAttr == null) {
                        config.setOverride(false);
                    }else {
                        List<String> addressList = new ArrayList<String>();
                        String[] addresses = StringUtil.split(addressListAttr);
                        if (addresses == null || addresses.length == 0) {
                            config.setOverride(false);
                        }else {
                            for(String address : addresses) {
                                addressList.add(address);
                            }

                            config.setAddresses(addressList);
                        }
                    }

                }
                if (StringUtils.hasLength(maxActiveAttr)) {
                    config.setMaxActive(Integer.parseInt(maxActiveAttr));
                }
                if (StringUtils.hasLength(maxIdleAttr)) {
                    config.setMaxIdle(Integer.parseInt(maxIdleAttr));
                }
                if (StringUtils.hasLength(minIdleAttr)) {
                    config.setMinIdle(Integer.parseInt(minIdleAttr));
                }
                if (StringUtils.hasLength(minEvictableIdleTimeMillisAttr)) {
                    config.setMinEvictableIdleTimeMillis(Integer.parseInt(minEvictableIdleTimeMillisAttr));
                }

                if (StringUtils.hasLength(timeBetweenEvictionRunsMillsAttr)) {
                    config.setTimeBetweenEvictionRunsMillis(Integer.parseInt(timeBetweenEvictionRunsMillsAttr));
                }

                if (StringUtils.hasLength(testOnBorrowAttr)) {
                    config.setTestOnBorrow(Boolean.parseBoolean(testOnBorrowAttr));
                }
                if (StringUtils.hasLength(testOnWhileIdleAttr)) {
                    config.setTestWhileIdle(Boolean.parseBoolean(testOnWhileIdleAttr));
                }

                if (StringUtils.hasLength(timeWaitAttr)) {
                    config.setTimeWait(Integer.parseInt(timeWaitAttr));
                }
                serviceConfigs.add(config);
            } else if (node instanceof Element && node.getLocalName().equals(NodeConstants.ELEMENT_SERVICE_AUTHENTICATION_DUMMY_AUTHENTICATOR)) {
                authenticator = new DummyAuthenticator();
                authenticator.setSerializeType(Byte.parseByte(((Element) node).getAttribute(NodeConstants.ATTRIBUTE_SERVICE_SERIALIZER_TYPE)));
            } else if (node instanceof Element && node.getLocalName().equals(NodeConstants.ELEMENT_SERVICE_AUTHENTICATION_USERNAME_AUTHENTICATOR)) {
                authenticator = new UserPasswordAuthenticator();
                ((UserPasswordAuthenticator) authenticator).setUsername(((Element) node).getAttribute(NodeConstants.ATTRIBUTE_SERVICE_AUTHENTICATION_USERNAME_AUTHENTICATOR_USERNAME));
                ((UserPasswordAuthenticator) authenticator).setPassword(((Element) node).getAttribute(NodeConstants.ATTRIBUTE_SERVICE_AUTHENTICATION_USERNAME_AUTHENTICATOR_PASSWORD));
                authenticator.setSerializeType(Byte.parseByte(((Element) node).getAttribute(NodeConstants.ATTRIBUTE_SERVICE_SERIALIZER_TYPE)));
            }

        }
        for (ServiceConfig config : serviceConfigs) {
            config.setAuthenticator(authenticator);
            logger.debug("service config:", config);
        }
        return serviceConfigs;
    }
}
