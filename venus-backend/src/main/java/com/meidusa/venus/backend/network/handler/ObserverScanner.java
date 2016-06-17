package com.meidusa.venus.backend.network.handler;

import com.meidusa.toolkit.common.bean.BeanContextBean;
import com.meidusa.venus.annotations.Observer;
import com.meidusa.venus.backend.InvocationObserver;
import com.meidusa.venus.util.ClasspathAnnotationScanner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by godzillahua on 6/13/16.
 */
public class ObserverScanner {

    private static Logger logger = LoggerFactory.getLogger(ObserverScanner.class);

    private static List<InvocationObserver> invocationObservers = new ArrayList<InvocationObserver>();

    public static void registryObserver (){
        Map<Class<?>, Observer> observerMap =  ClasspathAnnotationScanner.find(InvocationObserver.class, Observer.class);

        try {
            for(Class<?> observerClass : observerMap.keySet()) {

                InvocationObserver bean = (InvocationObserver)BeanContextBean.getInstance().getBeanContext().createBean(observerClass);

                invocationObservers.add(bean);

            }
        } catch (Exception e) {
            logger.error("", e);
        }

    }

    public static List<InvocationObserver> getInvocationObservers() {
        return invocationObservers;
    }
}
