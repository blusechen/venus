package com.meidusa.venus.client.spring;

import org.springframework.beans.factory.xml.NamespaceHandlerSupport;

/**
 * Created by huawei on 5/17/16.
 */
public class VenusClientNamespaceHandler extends NamespaceHandlerSupport {
    @Override
    public void init() {

        registerBeanDefinitionParser(NodeConstants.ELEMENT_REGISTRY_NAME, new RegistryBeanDefinitionParser());
        registerBeanDefinitionParser(NodeConstants.ELEMENT_SERVICES_NAME, new ServiceBeanDefinitionParser());

    }
}
