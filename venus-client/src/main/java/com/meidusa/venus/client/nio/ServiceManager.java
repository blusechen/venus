package com.meidusa.venus.client.nio;

import com.meidusa.toolkit.common.poolable.MultipleLoadBalanceObjectPool;
import com.meidusa.toolkit.net.BackendConnectionPool;
import com.meidusa.toolkit.net.MultipleLoadBalanceBackendConnectionPool;
import com.meidusa.toolkit.net.PollingBackendConnectionPool;
import com.meidusa.venus.annotations.Endpoint;
import com.meidusa.venus.annotations.Service;
import com.meidusa.venus.client.RemoteContainer;
import com.meidusa.venus.client.VenusInvocationHandler;
import com.meidusa.venus.io.network.VenusBackendConnectionFactory;
import com.meidusa.venus.metainfo.EndpointParameter;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.util.Assert;

import javax.annotation.PostConstruct;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.RunnableFuture;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by huawei on 5/17/16.
 */
public class ServiceManager implements BeanFactoryPostProcessor {

    private List<Class<?>> serviceClassNameList;

    private String refRegistryManagerName;

    private RegistryManager registryManager;

    class NioServiceInvocationHandler extends VenusInvocationHandler implements Runnable {

        private ScheduledExecutorService executorService;
        private RegistryManager registryManager;
        private String serviceName;

        private List<String> currentRemoteServiceIpList;

        RemoteContainer container = new RemoteContainer();

        private BackendConnectionPool pool;

        public NioServiceInvocationHandler(ScheduledExecutorService executorService, RegistryManager registryManager, String serviceName) {
            Assert.notNull(executorService, "");
            Assert.notNull(registryManager, "");
            Assert.hasLength(serviceName, "");
            this.executorService = executorService;
            this.executorService.scheduleAtFixedRate(this, 1000L, 1, TimeUnit.MINUTES);
        }

        public NioServiceInvocationHandler init() {
            List<String> remoteServiceIpList = registryManager.getRemote(serviceName);
            int size = remoteServiceIpList.size();

            createNioPool(remoteServiceIpList);

            return this;
        }

        private void createNioPool(List<String> remoteServiceIpList) {
            int size = remoteServiceIpList.size();
            if (size > 1) {

            }else {

            }
        }

        private void reCreateNioPool(List<String> remoteServiceIpList) {
            createNioPool(remoteServiceIpList);
        }

        @Override
        protected Object invokeRemoteService(Service service, Endpoint endpoint, Method method, EndpointParameter[] params, Object[] args) throws Exception {
            return null;
        }

        @Override
        public void run() {

            try{
                List<String> remoteServiceIpList = registryManager.getRemote(serviceName);

                if (remoteServiceIpList.equals(currentRemoteServiceIpList)) {
                    //build new connection pool
                    reCreateNioPool(remoteServiceIpList);
                }
            }catch (Exception e) {

            }
        }
    }




    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        System.out.println("post");
        registryManager = beanFactory.getBean(refRegistryManagerName, RegistryManager.class);
        int size = serviceClassNameList.size();
        ScheduledExecutorService executorService = Executors.newScheduledThreadPool(size);

        for(int i = 0;i < size; i++) {
            Object serviceInstance = Proxy.newProxyInstance(this.getClass().getClassLoader(), new Class[]{serviceClassNameList.get(i)}, new InvocationHandler() {
                @Override
                public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                    return null;
                }
            });

            beanFactory.registerSingleton("", serviceInstance);
        }


    }
}
