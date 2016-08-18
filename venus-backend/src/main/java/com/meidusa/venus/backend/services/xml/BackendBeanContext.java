package com.meidusa.venus.backend.services.xml;

import com.meidusa.toolkit.common.bean.BeanContext;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;

/**
 * Created by GodzillaHua on 7/3/16.
 */
public class BackendBeanContext implements BeanContext {

    private BeanFactory beanFactory;

    public BackendBeanContext(BeanFactory beanFactory) {
        this.beanFactory = beanFactory;
    }


    @Override
    public Object getBean(String beanName) {
        if (beanFactory != null) {
            return beanFactory.getBean(beanName);
        } else {
            return null;
        }
    }

    @Override
    public Object createBean(Class clazz) throws Exception {
        if (beanFactory instanceof AutowireCapableBeanFactory) {
            AutowireCapableBeanFactory factory = (AutowireCapableBeanFactory) beanFactory;
            Object object = factory.autowire(clazz, AutowireCapableBeanFactory.AUTOWIRE_BY_NAME, false);
            return object;
        }
        return null;
    }
}
