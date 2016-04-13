package com.meidusa.venus.spring;

import java.io.IOException;
import java.util.Properties;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;

import com.meidusa.toolkit.common.bean.config.ConfigUtil;

public class VenusPropertyPlaceholderConfigurer extends PropertyPlaceholderConfigurer implements BeanDefinitionRegistryPostProcessor, InitializingBean {
    private boolean inited;
    private BeanFactory beanFactory;

    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        if (inited)
            return;
        try {
            Properties mergedProps = mergeProperties();

            // Convert the merged properties, if necessary.
            convertProperties(mergedProps);

            // Let the subclass process the properties.
            processProperties(beanFactory, mergedProps);
            inited = true;
        } catch (IOException ex) {
            throw new BeanInitializationException("Could not load properties", ex);
        }
    }

    @Override
    protected void processProperties(ConfigurableListableBeanFactory beanFactory, Properties props) throws BeansException {
        ConfigUtil.addProperties(props);
        super.processProperties(beanFactory, props);
    }

    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
        postProcessBeanFactory((ConfigurableListableBeanFactory) this.beanFactory);
    }

    public void setBeanFactory(BeanFactory beanFactory) {
        super.setBeanFactory(beanFactory);
        this.beanFactory = beanFactory;
    }

    public void afterPropertiesSet() throws Exception {
        postProcessBeanFactory((ConfigurableListableBeanFactory) this.beanFactory);
    }
}
