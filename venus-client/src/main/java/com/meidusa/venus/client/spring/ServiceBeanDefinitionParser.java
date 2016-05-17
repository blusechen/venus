package com.meidusa.venus.client.spring;

import com.meidusa.venus.client.nio.ServiceManager;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

/**
 * Created by huawei on 5/17/16.
 */
public class ServiceBeanDefinitionParser implements BeanDefinitionParser {
    @Override
    public BeanDefinition parse(Element element, ParserContext parserContext) {

        RootBeanDefinition rbd = new RootBeanDefinition();
        rbd.setBeanClass(ServiceManager.class);
        rbd.setDependsOn(new String[]{"test"});
        parserContext.getRegistry().registerBeanDefinition(ServiceManager.class.getName(), rbd);
        return rbd;
    }
}
