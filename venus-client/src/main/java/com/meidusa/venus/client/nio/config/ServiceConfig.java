package com.meidusa.venus.client.nio.config;

import com.meidusa.venus.annotations.Service;
import com.meidusa.venus.io.authenticate.Authenticator;
import org.apache.commons.lang.builder.ToStringBuilder;

import java.util.List;

/**
 * Created by huawei on 5/18/16.
 */
public class ServiceConfig {

    /**
     * 服务接口类
     */
    private Class<?> serviceInterface;

    /**
     * 服务接口注解
     */
    private Service serviceAnnotation;

    /**
     * 服务名称
     */
    private String serviceName;

    /**
     * 服务版本
     */
    private int version;

    /**
     * 是否覆盖注册中心获取到的远程地址
     */
    private boolean override = false;

    /**
     * 服务远程地址
     */
    private List<String> addresses;

    /**
     * 服务认证方式
     */
    private Authenticator authenticator;

    /**
     * 服务连接池最大激活数
     */
    private int maxActive = 20;

    /**
     * 服务连接池最大空闲数
     */
    private int maxIdle = 20;

    /**
     * 服务连接池最小空闲数
     */
    private int minIdle = 10;

    /**
     * 最小驱逐空闲连接时间(毫秒)
     */
    private int minEvictableIdleTimeMillis = 60 * 1000;

    /**
     * 执行驱逐间隔时间
     */
    private int timeBetweenEvictionRunsMillis = 60 * 1000;

    /**
     * 是否在获取连接时检测连接可用性
     */
    private boolean testOnBorrow = true;

    /**
     * 是否在连接空闲时检测可用性
     */
    private boolean testWhileIdle = true;

    /**
     * 服务超时时间, 默认30秒
     */
    private int timeWait = 30;

    public Class<?> getServiceInterface() {
        return serviceInterface;
    }

    public void setServiceInterface(Class<?> serviceInterface) {
        this.serviceInterface = serviceInterface;
    }

    public Service getServiceAnnotation() {
        return serviceAnnotation;
    }

    public void setServiceAnnotation(Service serviceAnnotation) {
        this.serviceAnnotation = serviceAnnotation;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public boolean isOverride() {
        return override;
    }

    public void setOverride(boolean override) {
        this.override = override;
    }

    public List<String> getAddresses() {
        return addresses;
    }

    public void setAddresses(List<String> addresses) {
        this.addresses = addresses;
    }

    public Authenticator getAuthenticator() {
        return authenticator;
    }

    public void setAuthenticator(Authenticator authenticator) {
        this.authenticator = authenticator;
    }

    public int getMaxActive() {
        return maxActive;
    }

    public void setMaxActive(int maxActive) {
        this.maxActive = maxActive;
    }

    public int getMaxIdle() {
        return maxIdle;
    }

    public void setMaxIdle(int maxIdle) {
        this.maxIdle = maxIdle;
    }

    public int getMinIdle() {
        return minIdle;
    }

    public void setMinIdle(int minIdle) {
        this.minIdle = minIdle;
    }

    public int getMinEvictableIdleTimeMillis() {
        return minEvictableIdleTimeMillis;
    }

    public void setMinEvictableIdleTimeMillis(int minEvictableIdleTimeMillis) {
        this.minEvictableIdleTimeMillis = minEvictableIdleTimeMillis;
    }

    public int getTimeBetweenEvictionRunsMillis() {
        return timeBetweenEvictionRunsMillis;
    }

    public void setTimeBetweenEvictionRunsMillis(int timeBetweenEvictionRunsMillis) {
        this.timeBetweenEvictionRunsMillis = timeBetweenEvictionRunsMillis;
    }

    public boolean isTestOnBorrow() {
        return testOnBorrow;
    }

    public void setTestOnBorrow(boolean testOnBorrow) {
        this.testOnBorrow = testOnBorrow;
    }

    public boolean isTestWhileIdle() {
        return testWhileIdle;
    }

    public void setTestWhileIdle(boolean testWhileIdle) {
        this.testWhileIdle = testWhileIdle;
    }

    public int getTimeWait() {
        return timeWait;
    }

    public void setTimeWait(int timeWait) {
        this.timeWait = timeWait;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}
