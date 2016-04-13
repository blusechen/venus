package com.meidusa.venus.backend.interceptor;

import java.util.HashMap;
import java.util.Map;

import ognl.OgnlException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.meidusa.toolkit.common.bean.util.InitialisationException;
import com.meidusa.venus.backend.EndpointInvocation;
import com.meidusa.venus.backend.interceptor.config.CacheableInterceptorConfig;
import com.meidusa.venus.backend.interceptor.config.InterceptorConfig;
import com.meidusa.venus.cache.CacheClient;
import com.meidusa.venus.util.OgnlUtil;
import com.meidusa.venus.util.PropertyStringGenerator;

/**
 * TODO
 * 
 * @author Struct
 * 
 */
public class CacheableInterceptor extends AbstractInterceptor implements Configurable {

    private static Logger logger = LoggerFactory.getLogger(CacheableInterceptor.class);
    private static CacheKeyGenerator keyGenerator = new CacheKeyGenerator("%{", "}");

    Map<Class<?>, Map<String, CacheableInterceptorConfig>> configMap;

    public static class CacheKeyGenerator extends PropertyStringGenerator {

        public CacheKeyGenerator(String prefix, String suffix) {
            super(prefix, suffix);
        }

        @Override
        protected String getString(String key, Object object) {
            String str = null;
            try {
                str = OgnlUtil.findString(object, key);
                return str;
            } catch (OgnlException e) {
                logger.error("can't get cache key for parameter map with expression: " + key + ", map: " + object + ".");

            }
            return "";
        }

    }

    private CacheClient cacheClient;

    public CacheClient getCacheClient() {
        return cacheClient;
    }

    public void setCacheClient(CacheClient cacheClient) {
        this.cacheClient = cacheClient;
    }

    @Override
    public Object intercept(EndpointInvocation invocation) {
        Class<?> serviceType = invocation.getEndpoint().getService().getType();
        String ep = invocation.getEndpoint().getName();

        CacheableInterceptorConfig config = this.getEndpointConfig(serviceType, ep);
        if (config == null) {
            return invocation.invoke();
        }
        String key = this.generateKey(config.getKey(), invocation.getContext().getParameters());

        switch (config.getOperation()) {
            case GET:
                Object result = null;
                try {
                    result = this.cacheClient.get(key);
                } catch (Exception e) {
                    logger.error("Cache Operation Error:", e);
                }

                if (result != null) {
                    return result;
                }
                result = invocation.invoke();
                try {
                    cacheClient.set(key, result, config.getExpired());
                } catch (Exception e) {
                    logger.error("Cache Operation Error:", e);
                }
                return result;
            case DELETE:
                try {

                    cacheClient.delete(key);
                } catch (Exception e) {
                    logger.error("Cache Operation Error:", e);
                }
                return invocation.invoke();
            default:
                return invocation.invoke();
        }

    }

    private String generateKey(String key, final Map<String, Object> parameterMap) {
        return keyGenerator.getKey(key, parameterMap);
    }

    private CacheableInterceptorConfig getEndpointConfig(Class<?> clazz, String ep) {
        Map<String, CacheableInterceptorConfig> serviceConfigMap = this.configMap.get(clazz);
        if (serviceConfigMap == null) {
            return null;
        }
        return serviceConfigMap.get(ep);

    }

    @Override
    public void processConfig(Class<?> clazz, String ep, InterceptorConfig config) {
        Map<String, CacheableInterceptorConfig> serviceConfigMap = this.configMap.get(clazz);
        if (serviceConfigMap == null) {
            serviceConfigMap = new HashMap<String, CacheableInterceptorConfig>();
            this.configMap.put(clazz, serviceConfigMap);
        }
        if (config instanceof CacheableInterceptorConfig) {
            serviceConfigMap.put(ep, (CacheableInterceptorConfig) config);
        }
    }

    @Override
    public void init() throws InitialisationException {
        this.configMap = new HashMap<Class<?>, Map<String, CacheableInterceptorConfig>>();
        super.init();
    }

}
