package com.meidusa.venus.client.simple;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;

import com.meidusa.toolkit.common.util.Tuple;
import com.meidusa.venus.annotations.Endpoint;
import com.meidusa.venus.client.ServiceFactory;
import com.meidusa.venus.exception.CodedException;
import com.meidusa.venus.exception.VenusExceptionFactory;
import com.meidusa.venus.exception.XmlVenusExceptionFactory;
import com.meidusa.venus.io.authenticate.Authenticator;
import com.meidusa.venus.io.authenticate.DummyAuthenticator;
import com.meidusa.venus.io.packet.DummyAuthenPacket;

/**
 * 
 * <b>简单连接服务工厂:</b> <br/>
 * <li>没有连接池,每次请求都将创建连接,请求完毕以后关闭连接.</li> <li>只能针对某个具体的IP,Port而创建,多个地址需要创建不同对象</li> <li>不支持回调</li> <br/>
 * <b>使用场景:</b>
 * 
 * <pre>
 * 访问固定地址,并且使用频率较少的情况下.
 * 相关使用代码:
 * SimpleServiceFactory factory = new SimpleServiceFactory("127.0.0.1",16800);
 * factory.setSoTimeout(16 * 1000);//可选,默认 15秒
 * factory.setCoTimeout(5 * 1000);//可选,默认5秒
 * HelloService helloService = factory.getService(HelloService.class);
 * </pre>
 * 
 * @author structchen
 * 
 */
public class SimpleServiceFactory implements ServiceFactory {
    private VenusExceptionFactory venusExceptionFactory;
    private Authenticator authenticator;

    /**
     * 读取返回数据包的超时时间
     */
    private int soTimeout = 15 * 1000;

    /**
     * 连接超时时间
     */
    private int coTimeout = 5 * 1000;
    private Map<Class<?>, Tuple<Object, SimpleInvocationHandler>> servicesMap = new HashMap<Class<?>, Tuple<Object, SimpleInvocationHandler>>();
    private String host;
    private int port;

    public SimpleServiceFactory(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public Authenticator getAuthenticator() {
        return authenticator;
    }

    public void setAuthenticator(Authenticator authenticator) {
        this.authenticator = authenticator;
    }

    public VenusExceptionFactory getVenusExceptionFactory() {
        return venusExceptionFactory;
    }

    public void setVenusExceptionFactory(VenusExceptionFactory venusExceptionFactory) {
        this.venusExceptionFactory = venusExceptionFactory;
    }

    public int getSoTimeout() {
        return soTimeout;
    }

    public void setSoTimeout(int soTimeout) {
        this.soTimeout = soTimeout;
    }

    public int getCoTimeout() {
        return coTimeout;
    }

    public void setCoTimeout(int coTimeout) {
        this.coTimeout = coTimeout;
    }

    @Override
    public <T> T getService(Class<T> t) {
        Tuple<Object, SimpleInvocationHandler> object = servicesMap.get(t);
        if (object == null) {
            synchronized (servicesMap) {
                object = servicesMap.get(t);
                if (object == null) {
                    T obj = initService(t, host, port);
                    return obj;
                }
            }
        }
        
        return (T) object.left;
    }

    protected <T> T initService(Class<T> t, String host, int port) {
        SimpleInvocationHandler invocationHandler = new SimpleInvocationHandler(host, port, coTimeout, soTimeout);
        if(this.venusExceptionFactory == null){
        	XmlVenusExceptionFactory venusExceptionFactory = new XmlVenusExceptionFactory();
        	venusExceptionFactory.setConfigFiles(new String[]{"classpath:com/meidusa/venus/exception/VenusSystemException.xml"});
        	venusExceptionFactory.init();
        	this.venusExceptionFactory = venusExceptionFactory;
        }
        
        invocationHandler.setVenusExceptionFactory(this.getVenusExceptionFactory());
        
        if(this.getAuthenticator() == null){
        	this.authenticator = new DummyAuthenticator<DummyAuthenPacket>();
        }
        
        invocationHandler.setAuthenticator(getAuthenticator());

        T object = (T) Proxy.newProxyInstance(this.getClass().getClassLoader(), new Class[] { t }, invocationHandler);

        for (Method method : t.getMethods()) {
            Endpoint endpoint = method.getAnnotation(Endpoint.class);
            if (endpoint != null) {
                Class[] eclazz = method.getExceptionTypes();
                for (Class clazz : eclazz) {
                    if (venusExceptionFactory != null && CodedException.class.isAssignableFrom(clazz)) {
                        venusExceptionFactory.addException(clazz);
                    }
                }
            }
        }

        Tuple<Object, SimpleInvocationHandler> serviceTuple = new Tuple<Object, SimpleInvocationHandler>(object, invocationHandler);
        servicesMap.put(t, serviceTuple);
        return object;
    }

    @Override
    public void destroy() {
        // TODO Auto-generated method stub

    }

	@Override
	public <T> T getService(String name, Class<T> t) {
		return getService(t);
	}
}
