package com.meidusa.venus.util;

import java.security.SecureRandom;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("rawtypes")
public class VenusTracerUtil {
    private static SecureRandom numberGenerator = new SecureRandom();
    public static final String REQUEST_TRACE_ID = "REQUEST_TRACE_ID";
    private static final String REQUEST_TRACE_MSG_WITH_PARAMS = "request id={},service={},params={}";
    
    private static final String LOG_TRACE_MSG_WITH_PARAMS_DEBUG = "time={},id={},service={},params={},result={}";
    
    private static final String LOG_TRACE_MSG_WITH_PARAMS_INFO = "time={},id={},service={}";
    
    private static final String REQUEST_TRACE_MSG_WITHOUT_PARAMS = "request id={},service={}";

    private static final String ROUTER_TRACE = "router id={},service={},source={},remote={}";
    
    private static final String RECEIVE_TRACE_MSG_WITH_PARAMS = "receive id={},service={},params={}";
    private static final String RECEIVE_TRACE_MSG_WITHOUT_PARAMS = "receive id={},service={}";

    private static final String CALLBACK_TRACE_MSG_WITH_PARAMS = "callback id={},service={},params={}";
    private static final String CALLBACK_TRACE_MSG_WITHOUT_PARAMS = "callback id={},service={}";

    private static Logger REUEST_LOGGER = LoggerFactory.getLogger("venus.tracer");

    public static void clearTracerID(){
    	ThreadLocalMap.remove(VenusTracerUtil.REQUEST_TRACE_ID);
    }
    
    public static byte[] randomTracerID(){
    	byte[] tracerID = randomUUID();
    	ThreadLocalMap.put(VenusTracerUtil.REQUEST_TRACE_ID,tracerID);
    	return tracerID;
    }
    
    public static byte[] getTracerID(){
    	return (byte[])ThreadLocalMap.get(VenusTracerUtil.REQUEST_TRACE_ID);
    }
    /**
     * from java.net.UUID
     * 
     * Static factory to retrieve a type 4 (pseudo randomly generated) UUID.
     * 
     * The <code>UUID</code> is generated using a cryptographically strong pseudo random number generator.
     * 
     * 
     * @return a randomly generated <tt>UUID</tt>.
     */
    public static byte[] randomUUID() {
        SecureRandom ng = numberGenerator;

        byte[] randomBytes = new byte[16];
        ng.nextBytes(randomBytes);
        randomBytes[6] &= 0x0f; /* clear version */
        randomBytes[6] |= 0x40; /* set to version 4 */
        randomBytes[8] &= 0x3f; /* clear variant */
        randomBytes[8] |= 0x80; /* set to IETF variant */
        return randomBytes;
    }

    /**
     * 
     * @param traceId
     * @param apiName
     * @param params
     */
    public static void logRequest(byte[] traceId, String apiName, String params) {
        if (REUEST_LOGGER.isDebugEnabled()) {
            REUEST_LOGGER.debug(REQUEST_TRACE_MSG_WITH_PARAMS, new Object[] { new UUID(traceId).toString(), apiName, params });
        } else if (REUEST_LOGGER.isInfoEnabled()) {
            REUEST_LOGGER.info(REQUEST_TRACE_MSG_WITHOUT_PARAMS, new UUID(traceId).toString(), apiName);
        }
    }

    /**
     * 
     * @param traceId
     * @param apiName
     * @param params
     */
    public static void logRequest(String traceId, String apiName, String params) {
        if (REUEST_LOGGER.isDebugEnabled()) {
            REUEST_LOGGER.debug(REQUEST_TRACE_MSG_WITH_PARAMS, new Object[] { traceId, apiName, params });
        } else if (REUEST_LOGGER.isInfoEnabled()) {
            REUEST_LOGGER.info(REQUEST_TRACE_MSG_WITHOUT_PARAMS, traceId, apiName);
        }
    }
    
    /**
     * 
     * @param traceId
     * @param apiName
     * @param params
     */
    public static void logResult(long time,String traceId, String apiName, String params,String jsonObject) {
        if (REUEST_LOGGER.isDebugEnabled()) {
            REUEST_LOGGER.debug(LOG_TRACE_MSG_WITH_PARAMS_DEBUG, new Object[] {time, traceId, apiName, params,jsonObject });
        } else if (REUEST_LOGGER.isInfoEnabled()) {
            REUEST_LOGGER.info(LOG_TRACE_MSG_WITH_PARAMS_INFO, new Object[] {time, traceId, apiName});
        }
    }
    
    
    /**
     * 
     * @param traceId
     * @param apiName
     */
    public static void logRequest(byte[] traceId, String apiName) {
        if (REUEST_LOGGER.isDebugEnabled() || REUEST_LOGGER.isInfoEnabled()) {
            REUEST_LOGGER.info(REQUEST_TRACE_MSG_WITHOUT_PARAMS, new UUID(traceId).toString(), apiName);
        }
    }

    /**
     * 
     * @param traceId
     * @param apiName
     * @param params
     */
    public static void logReceive(byte[] traceId, String apiName, String params) {
        if (REUEST_LOGGER.isDebugEnabled()) {
            REUEST_LOGGER.debug(RECEIVE_TRACE_MSG_WITH_PARAMS, new Object[] { new UUID(traceId).toString(), apiName, params });
        } else if (REUEST_LOGGER.isInfoEnabled()) {
            REUEST_LOGGER.info(RECEIVE_TRACE_MSG_WITHOUT_PARAMS, new UUID(traceId).toString(), apiName);
        }
    }

    /**
     * 
     * @param traceId
     * @param apiName
     */
    public static void logReceive(byte[] traceId, String apiName) {
        if (REUEST_LOGGER.isDebugEnabled() || REUEST_LOGGER.isInfoEnabled()) {
            REUEST_LOGGER.info(RECEIVE_TRACE_MSG_WITHOUT_PARAMS, new UUID(traceId).toString(), apiName);
        }
    }

    /**
     * 
     * @param traceId
     * @param apiName
     * @param params
     */
    public static void logCallback(byte[] traceId, String apiName, String params) {
        if (REUEST_LOGGER.isDebugEnabled()) {
            REUEST_LOGGER.debug(CALLBACK_TRACE_MSG_WITH_PARAMS, new Object[] { new UUID(traceId).toString(), apiName, params });
        } else if (REUEST_LOGGER.isInfoEnabled()) {
            REUEST_LOGGER.info(CALLBACK_TRACE_MSG_WITHOUT_PARAMS, new UUID(traceId).toString(), apiName);
        }
    }
    
    public static void logRouter(byte[] traceId, String apiName,String sourceIp,String remoteIp){
    	if (REUEST_LOGGER.isDebugEnabled()) {
            REUEST_LOGGER.debug(ROUTER_TRACE, new Object[] { new UUID(traceId).toString(), apiName, sourceIp, remoteIp});
    	}else if(REUEST_LOGGER.isInfoEnabled()){
    		REUEST_LOGGER.info(ROUTER_TRACE, new Object[] { new UUID(traceId).toString(), apiName, sourceIp, remoteIp});
    	}
    }

    /**
     * 
     * @param traceId
     * @param apiName
     */
    public static void logCallback(byte[] traceId, String apiName) {
        if (REUEST_LOGGER.isDebugEnabled() || REUEST_LOGGER.isInfoEnabled()) {
            REUEST_LOGGER.info(CALLBACK_TRACE_MSG_WITHOUT_PARAMS, new UUID(traceId).toString(), apiName);
        }
    }
}
