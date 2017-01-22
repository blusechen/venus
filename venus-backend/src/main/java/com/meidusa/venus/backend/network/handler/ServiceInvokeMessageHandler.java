package com.meidusa.venus.backend.network.handler;

import java.beans.PropertyDescriptor;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.meidusa.fastbson.exception.SerializeException;
import com.meidusa.fastjson.JSON;
import com.meidusa.fastjson.JSONException;
import com.meidusa.fastmark.feature.SerializerFeature;
import com.meidusa.toolkit.common.bean.util.Initialisable;
import com.meidusa.toolkit.common.bean.util.InitialisationException;
import com.meidusa.toolkit.common.util.Tuple;
import com.meidusa.toolkit.net.Connection;
import com.meidusa.toolkit.net.MessageHandler;
import com.meidusa.toolkit.net.util.InetAddressUtil;
import com.meidusa.toolkit.util.StringUtil;
import com.meidusa.toolkit.util.TimeUtil;
import com.meidusa.venus.annotations.ExceptionCode;
import com.meidusa.venus.annotations.RemoteException;
import com.meidusa.venus.backend.DefaultEndpointInvocation;
import com.meidusa.venus.backend.EndpointInvocation.ResultType;
import com.meidusa.venus.backend.RequestInfo;
import com.meidusa.venus.backend.Response;
import com.meidusa.venus.backend.VenusStatus;
import com.meidusa.venus.backend.context.RequestContext;
import com.meidusa.venus.backend.profiling.UtilTimerStack;
import com.meidusa.venus.backend.services.Endpoint;
import com.meidusa.venus.backend.services.Service;
import com.meidusa.venus.backend.services.ServiceManager;
import com.meidusa.venus.backend.services.xml.bean.PerformanceLogger;
import com.meidusa.venus.backend.view.MediaTypes;
import com.meidusa.venus.exception.CodedException;
import com.meidusa.venus.exception.DefaultVenusException;
import com.meidusa.venus.exception.ExceptionLevel;
import com.meidusa.venus.exception.ServiceInvokeException;
import com.meidusa.venus.exception.ServiceNotCallbackException;
import com.meidusa.venus.exception.ServiceVersionNotAllowException;
import com.meidusa.venus.exception.VenusExceptionCodeConstant;
import com.meidusa.venus.exception.VenusExceptionFactory;
import com.meidusa.venus.exception.VenusExceptionLevel;
import com.meidusa.venus.io.ServiceFilter;
import com.meidusa.venus.io.network.VenusFrontendConnection;
import com.meidusa.venus.io.packet.AbstractServicePacket;
import com.meidusa.venus.io.packet.AbstractServiceRequestPacket;
import com.meidusa.venus.io.packet.ErrorPacket;
import com.meidusa.venus.io.packet.OKPacket;
import com.meidusa.venus.io.packet.PacketConstant;
import com.meidusa.venus.io.packet.PingPacket;
import com.meidusa.venus.io.packet.PongPacket;
import com.meidusa.venus.io.packet.ServiceAPIPacket;
import com.meidusa.venus.io.packet.ServiceHeadPacket;
import com.meidusa.venus.io.packet.ServicePacketBuffer;
import com.meidusa.venus.io.packet.ServiceResponsePacket;
import com.meidusa.venus.io.packet.VenusRouterPacket;
import com.meidusa.venus.io.packet.VenusStatusRequestPacket;
import com.meidusa.venus.io.packet.VenusStatusResponsePacket;
import com.meidusa.venus.io.packet.serialize.SerializeServiceRequestPacket;
import com.meidusa.venus.io.packet.serialize.SerializeServiceResponsePacket;
import com.meidusa.venus.io.serializer.Serializer;
import com.meidusa.venus.io.serializer.SerializerFactory;
import com.meidusa.venus.notify.InvocationListener;
import com.meidusa.venus.notify.ReferenceInvocationListener;
import com.meidusa.venus.service.monitor.MonitorRuntime;
import com.meidusa.venus.util.ClasspathAnnotationScanner;
import com.meidusa.venus.util.Range;
import com.meidusa.venus.util.ThreadLocalConstant;
import com.meidusa.venus.util.ThreadLocalMap;
import com.meidusa.venus.util.UUID;
import com.meidusa.venus.util.Utils;
import com.meidusa.venus.util.VenusTracerUtil;
import com.meidusa.venus.util.concurrent.MultiQueueRunnable;

/**
 * singleton handler
 * @author structchen
 *
 */
@SuppressWarnings({ "unchecked", "rawtypes" })
public class ServiceInvokeMessageHandler implements MessageHandler<VenusFrontendConnection, Tuple<Long, byte[]>>, Initialisable {
	private static SerializerFeature[] JSON_FEATURE = new SerializerFeature[]{SerializerFeature.ShortString,SerializerFeature.IgnoreNonFieldGetter,SerializerFeature.SkipTransientField};
    private static final String TIMEOUT = "waiting-timeout for execution,api=%s,ip=%s,time=%d (ms)";
    static Map<Class<?>,Integer> codeMap = new HashMap<Class<?>,Integer>();
    static {
        Map<Class<?>,ExceptionCode>  map = ClasspathAnnotationScanner.find(Exception.class,ExceptionCode.class);
        if(map != null){
            for(Map.Entry<Class<?>, ExceptionCode> entry:map.entrySet()){
                codeMap.put(entry.getKey(), entry.getValue().errorCode());
            }
        }
        
        Map<Class<?>,RemoteException> rmap = ClasspathAnnotationScanner.find(Exception.class,RemoteException.class);
        
        if(rmap != null){
            for(Map.Entry<Class<?>, RemoteException> entry:rmap.entrySet()){
                codeMap.put(entry.getKey(), entry.getValue().errorCode());
            }
        }
    }
    private static String ENDPOINT_INVOKED_TIME = "invoked Totle Time: ";
    private static Logger logger = LoggerFactory.getLogger(ServiceInvokeMessageHandler.class);
    private static Logger INVOKER_LOGGER = LoggerFactory.getLogger("venus.service.invoker");
    private static Logger performanceLogger = LoggerFactory.getLogger("venus.backend.performance");
    private static Logger performancePrintResultLogger = LoggerFactory.getLogger("venus.backend.print.result");
    private static Logger performancePrintParamsLogger = LoggerFactory.getLogger("venus.backend.print.params");
    private int maxExecutionThread;
    private int threadLiveTime = 30;
    private boolean executorEnabled = false;
    private boolean executorProtected;
    private boolean useThreadLocalExecutor;

    private ServiceFilter filter;
    private Executor executor;
    private VenusExceptionFactory venusExceptionFactory;
    @Autowired
    private ServiceManager serviceManager;
    
    public boolean isExecutorEnabled() {
        return executorEnabled;
    }

    public void setExecutorEnabled(boolean executorEnabled) {
        this.executorEnabled = executorEnabled;
    }

    public boolean isExecutorProtected() {
        return executorProtected;
    }

    public boolean isUseThreadLocalExecutor() {
        return useThreadLocalExecutor;
    }

    public void setUseThreadLocalExecutor(boolean useThreadLocalExecutor) {
        this.useThreadLocalExecutor = useThreadLocalExecutor;
    }

    public void setExecutorProtected(boolean executorProtected) {
        this.executorProtected = executorProtected;
    }

    public int getThreadLiveTime() {
        return threadLiveTime;
    }

    public void setThreadLiveTime(int threadLiveTime) {
        this.threadLiveTime = threadLiveTime;
    }

    public int getMaxExecutionThread() {
        return maxExecutionThread;
    }

    public void setMaxExecutionThread(int maxExecutionThread) {
        this.maxExecutionThread = maxExecutionThread;
    }

    public VenusExceptionFactory getVenusExceptionFactory() {
        return venusExceptionFactory;
    }

    public Executor getExecutor() {
        return executor;
    }

    public void setExecutor(Executor executor) {
        this.executor = executor;
    }

    public void setVenusExceptionFactory(VenusExceptionFactory venusExceptionFactory) {
        this.venusExceptionFactory = venusExceptionFactory;
    }

    public ServiceManager getServiceManager() {
        return serviceManager;
    }

    public void setServiceManager(ServiceManager serviceManager) {
        this.serviceManager = serviceManager;
    }

    @Override
    public void handle(final VenusFrontendConnection conn,final Tuple<Long, byte[]> data) {
        final long waitTime = TimeUtil.currentTimeMillis() - data.left;

        byte[] message = data.right;

        int type = AbstractServicePacket.getType(message);
        VenusRouterPacket routerPacket = null;
        byte serializeType = conn.getSerializeType();
        String sourceIp = conn.getHost();
        if (PacketConstant.PACKET_TYPE_ROUTER == type) {
            routerPacket = new VenusRouterPacket();
            routerPacket.original = message;
            routerPacket.init(message);
            type = AbstractServicePacket.getType(routerPacket.data);
            message = routerPacket.data;
            serializeType = routerPacket.serializeType;
            sourceIp = InetAddressUtil.intToAddress(routerPacket.srcIP);
        }

        final byte packetSerializeType = serializeType;
        final String finalSourceIp = sourceIp;
        switch (type) {
            case PacketConstant.PACKET_TYPE_PING:
                PingPacket ping = new PingPacket();
                ping.init(message);
                PongPacket pong = new PongPacket();
                AbstractServicePacket.copyHead(ping, pong);
                postMessageBack(conn, null, ping, pong);
                if (logger.isDebugEnabled()) {
                    logger.debug("receive ping packet from " + conn.getHost() + ":" + conn.getPort());
                }
                break;

            // ignore this packet
            case PacketConstant.PACKET_TYPE_PONG:
                break;
            case PacketConstant.PACKET_TYPE_VENUS_STATUS_REQUEST:
                VenusStatusRequestPacket sr = new VenusStatusRequestPacket();
                sr.init(message);
                VenusStatusResponsePacket response = new VenusStatusResponsePacket();
                AbstractServicePacket.copyHead(sr, response);
                if(sr.newStatus != 0){
                	VenusStatus.getInstance().setStatus(sr.newStatus);
                }
                
                response.status = VenusStatus.getInstance().getStatus();
                postMessageBack(conn, null, sr, response);

                break;
            case PacketConstant.PACKET_TYPE_SERVICE_REQUEST:
                SerializeServiceRequestPacket request = null;
                Endpoint ep = null;

                ServiceAPIPacket apiPacket = new ServiceAPIPacket();
                try {
                    ServicePacketBuffer packetBuffer = new ServicePacketBuffer(message);
                    apiPacket.init(packetBuffer);

                    ep = getServiceManager().getEndpoint(apiPacket.apiName);

                    Serializer serializer = SerializerFactory.getSerializer(packetSerializeType);
                    request = new SerializeServiceRequestPacket(serializer, ep.getParameterTypeDict());

                    packetBuffer.setPosition(0);
                    request.init(packetBuffer);
                    VenusTracerUtil.logReceive(request.traceId, request.apiName,JSON.toJSONString(request.parameterMap,JSON_FEATURE) );

                } catch (Exception e) {
                    ErrorPacket error = new ErrorPacket();
                    AbstractServicePacket.copyHead(apiPacket, error);
                    if (e instanceof CodedException || codeMap.containsKey(e.getClass())) {
                    	if(e instanceof CodedException){
                    		CodedException codeEx = (CodedException) e;
                    		error.errorCode = codeEx.getErrorCode();
                    	}else{
                    		error.errorCode = codeMap.get(e.getClass());
                    	}
                    } else {
                        if (e instanceof JSONException || e instanceof SerializeException) {
                            error.errorCode = VenusExceptionCodeConstant.REQUEST_ILLEGAL;
                        } else {
                            error.errorCode = VenusExceptionCodeConstant.UNKNOW_EXCEPTION;
                        }
                    }
                    error.message = e.getMessage();
                    if(filter != null){
                    	filter.before(request);
                    }
                    postMessageBack(conn, routerPacket, request, error);
                    if(filter != null){
                    	filter.after(error);
                    }
                    
                    if(request != null){
                    	logPerformance(ep,request.traceId == null ? UUID.toString(PacketConstant.EMPTY_TRACE_ID) : UUID.toString(request.traceId),apiPacket.apiName,waitTime,0,conn.getHost(),finalSourceIp,request.clientId,request.clientRequestId,request.parameterMap,(Object)error);
                    	
                    	if (e instanceof VenusExceptionLevel) {
                            if (((VenusExceptionLevel) e).getLevel() != null) {
                                logDependsOnLevel(((VenusExceptionLevel) e).getLevel(), logger, e.getMessage() + " client:{clientID=" + apiPacket.clientId
                                        + ",ip=" + conn.getHost() + ":" + conn.getPort() + ",sourceIP=" + finalSourceIp + ", apiName=" + apiPacket.apiName
                                        + "}", e);
                            }
                        } else {
                        	logger.error(e.getMessage() + " [ip=" + conn.getHost() + ":" + conn.getPort() + ",sourceIP=" + finalSourceIp + ", apiName="+ apiPacket.apiName + "]", e);
                        }
                    }else{
                        logger.error(e.getMessage() + " [ip=" + conn.getHost() + ":" + conn.getPort() + ",sourceIP=" + finalSourceIp + ", apiName="+ apiPacket.apiName + "]", e);
                    }
                    
                    
                    return;
                }

                final String apiName = request.apiName;
                /*int index = apiName.lastIndexOf(".");
                String serviceName = request.apiName.substring(0, index);
                String methodName = request.apiName.substring(index + 1);*/
                // RequestInfo info = getRequestInfo(conn, request);

                final Endpoint endpoint = ep;//getServiceManager().getEndpoint(serviceName, methodName, request.parameterMap.keySet().toArray(new String[] {}));

                ResultType resultType = ResultType.RESPONSE;
                RemotingInvocationListener<Serializable> invocationListener = null;
                if (endpoint.isVoid()) {
                    resultType = ResultType.OK;
                    if (endpoint.isAsync()) {
                        resultType = ResultType.NONE;
                    }

                    for (Class clazz : endpoint.getMethod().getParameterTypes()) {
                        if (InvocationListener.class.isAssignableFrom(clazz)) {
                            resultType = ResultType.NOTIFY;
                            break;
                        }
                    }
                }

                for (Map.Entry<String, Object> entry : request.parameterMap.entrySet()) {
                    if (entry.getValue() instanceof ReferenceInvocationListener) {
                        invocationListener = new RemotingInvocationListener<Serializable>(conn, (ReferenceInvocationListener) entry.getValue(), request,
                                routerPacket);
                        request.parameterMap.put(entry.getKey(), invocationListener);
                    }
                }

                // service version error
                ErrorPacket errorPacket = null;

                if (errorPacket == null) {
                    errorPacket = checkVersion(endpoint, request);
                }
                if (errorPacket == null) {
                    errorPacket = checkActive(endpoint, request);
                }

                /**
                 * 判断超时
                 */
                boolean isTimeout = false;
                if (errorPacket == null) {
                    errorPacket = checkTimeout(conn,endpoint, request, waitTime);
                    if(errorPacket != null){
                    	isTimeout = true;
                    }
                }

                __TIMEOUT:{
                
	                if (errorPacket != null) {
	                    if (resultType == ResultType.NOTIFY) {
	                    	if(isTimeout){
	                    		break __TIMEOUT;
	                    	}
	                        if (invocationListener != null) {
	                            invocationListener.onException(new ServiceVersionNotAllowException(errorPacket.message));
	                        } else {
	                            postMessageBack(conn, routerPacket, request, errorPacket);
	                        }
	                    } else {
	                        postMessageBack(conn, routerPacket, request, errorPacket);
	                    }
	                    if(filter != null){
	                    	filter.before(request);
	                    }
	                    logPerformance(endpoint,UUID.toString(request.traceId),apiName,waitTime,0,conn.getHost(),finalSourceIp,request.clientId,request.clientRequestId,request.parameterMap,(Object)errorPacket);
	                    if(filter != null){
	                    	filter.after(errorPacket);
	                    }
	                    return;
	                }
                }

                RequestHandler requestHandler = new RequestHandler();

                RequestInfo requestInfo = requestHandler.getRequestInfo(packetSerializeType, conn, routerPacket);
                RequestContext context = requestHandler.createContext(requestInfo, endpoint, request);

                ServiceRunnable runnable = new ServiceRunnable(conn, endpoint,
                        context, resultType,
                        filter, routerPacket,
                        request, serializeType,
                        invocationListener, venusExceptionFactory,
                        data);

                if (executor == null) {
                    runnable.run();
                } else {
                    executor.execute(runnable);
                }

                break;
            default:
                StringBuilder buffer = new StringBuilder("receive unknown packet type=" + type + "  from ");
                buffer.append(conn.getHost() + ":" + conn.getPort()).append("\n");
                buffer.append("-------------------------------").append("\n");
                buffer.append(StringUtil.dumpAsHex(message, message.length)).append("\n");
                buffer.append("-------------------------------").append("\n");
                ServiceHeadPacket head = new ServiceHeadPacket();
                head.init(message);
                ErrorPacket error = new ErrorPacket();

                AbstractServicePacket.copyHead(head, error);
                error.errorCode = VenusExceptionCodeConstant.PACKET_DECODE_EXCEPTION;
                error.message = "receive unknown packet type=" + type + "  from " + conn.getHost() + ":" + conn.getPort();
                postMessageBack(conn, routerPacket, head, error);

        }

    }

    protected void logPerformance(Endpoint endpoint,String traceId,String apiName,long queuedTime,
    				long executTime,String remoteIp,String sourceIP, long clientId,long requestId,
    				Map<String,Object > parameterMap,Object result){
    	StringBuffer buffer = new StringBuffer();
        buffer.append("[").append(queuedTime).append(",").append(executTime).append("]ms, (*server*) traceID=").append(traceId).append(", api=").append(apiName).append(", ip=")
                .append(remoteIp).append(", sourceIP=").append(sourceIP).append(", clientID=")
                .append(clientId).append(", requestID=").append(requestId); 

        PerformanceLogger pLevel = null;
        
        if(endpoint != null){		
        	pLevel = endpoint.getPerformanceLogger();
        }
        
        if (pLevel != null) {

            if (pLevel.isPrintParams()) {
                buffer.append(", params=");
                buffer.append(JSON.toJSONString(parameterMap,JSON_FEATURE));
            }
            if (pLevel.isPrintResult()) {
            	buffer.append(", result=");
            	if(result instanceof ErrorPacket){
            		buffer.append("{ errorCode=").append(((ErrorPacket) result).errorCode);
            		buffer.append(", message=").append(((ErrorPacket) result).message);
            		buffer.append("}");
            	}else if(result instanceof Response){
            		if(((Response) result).getErrorCode()>0){
            			buffer.append("{ errorCode=").append(((Response) result).getErrorCode());
                		buffer.append(", message=\"").append(((Response) result).getErrorMessage()).append("\"");
                		buffer.append(", className=\"").append(((Response) result).getException().getClass().getSimpleName()).append("\"");
                		buffer.append("}");
            		}else{
            			buffer.append(JSON.toJSONString(result,JSON_FEATURE));
            		}
            	}
            }

            if (queuedTime >= pLevel.getError() || executTime >= pLevel.getError() || queuedTime + executTime >= pLevel.getError()) {
                if (performanceLogger.isErrorEnabled()) {
                    performanceLogger.error(buffer.toString());
                }
            } else if (queuedTime >= pLevel.getWarn() || executTime >= pLevel.getWarn() || queuedTime + executTime >= pLevel.getWarn()) {
                if (performanceLogger.isWarnEnabled()) {
                    performanceLogger.warn(buffer.toString());
                }
            } else if (queuedTime >= pLevel.getInfo() || executTime >= pLevel.getInfo() || queuedTime + executTime >= pLevel.getInfo()) {
                if (performanceLogger.isInfoEnabled()) {
                    performanceLogger.info(buffer.toString());
                }
            } else {
                if (performanceLogger.isDebugEnabled()) {
                    performanceLogger.debug(buffer.toString());
                }
            }

        } else {
	        	buffer.append(", params=");
	        	if (performancePrintParamsLogger.isDebugEnabled()) {
					buffer.append(JSON.toJSONString(parameterMap,JSON_FEATURE));
				}else{
					buffer.append("{print.params:disabled}");
				}
        	
	            if (result == null) {
	                buffer.append(", result=<null>");
	            } else {
	            	buffer.append(", result=");
	            	if(result instanceof ErrorPacket){
	            		buffer.append("{ errorCode=").append(((ErrorPacket) result).errorCode);
	            		buffer.append(", message=").append(((ErrorPacket) result).message);
	            		buffer.append("}");
	            	}else if(result instanceof Response){
	            		if(((Response) result).getErrorCode()>0){
	            			buffer.append("{errorCode=").append(((Response) result).getErrorCode());
	            			buffer.append(", message=\"").append(((Response) result).getErrorMessage()).append("\"");
	                		buffer.append(", className=\"").append(((Response) result).getException().getClass().getSimpleName()).append("\"");
	                		buffer.append("}");
	            		}else{
	            			if (performancePrintResultLogger.isDebugEnabled()) {
	            				buffer.append(JSON.toJSONString(result,new SerializerFeature[]{SerializerFeature.ShortString}));
	            			}else{
	            				buffer.append("{print.result:disabled}");
	            			}
	            		}
	            	}
	            }
            if (queuedTime >= 5 * 1000 || executTime >= 5 * 1000 || queuedTime + executTime >= 5 * 1000) {
                if (performanceLogger.isErrorEnabled()) {
                    performanceLogger.error(buffer.toString());
                }
            } else if (queuedTime >= 3 * 1000 || executTime >= 3 * 1000 || queuedTime + executTime >= 3 * 1000) {
                if (performanceLogger.isWarnEnabled()) {
                    performanceLogger.warn(buffer.toString());
                }
            } else if (queuedTime >= 1 * 1000 || executTime >= 1 * 1000 || queuedTime + executTime >= 1 * 1000) {
                if (performanceLogger.isInfoEnabled()) {
                    performanceLogger.info(buffer.toString());
                }
            } else {
                if (performanceLogger.isDebugEnabled()) {
                    performanceLogger.debug(buffer.toString());
                }
            }
        }
    }
    
    private void logDependsOnLevel(ExceptionLevel level, Logger specifiedLogger, String msg, Throwable e) {
        switch (level) {
            case DEBUG:
                specifiedLogger.debug(msg, e);
                break;
            case INFO:
                specifiedLogger.info(msg, e);
                break;
            case TRACE:
                specifiedLogger.trace(msg, e);
                break;
            case WARN:
                specifiedLogger.warn(msg, e);
                break;
            case ERROR:
                specifiedLogger.error(msg, e);
                break;
            default:
                break;
        }
    }

    private static ErrorPacket checkTimeout(VenusFrontendConnection conn,Endpoint endpoint, AbstractServiceRequestPacket request, long waitTime) {
        if (waitTime > endpoint.getTimeWait()) {
            ErrorPacket error = new ErrorPacket();
            AbstractServicePacket.copyHead(request, error);
            error.errorCode = VenusExceptionCodeConstant.INVOCATION_ABORT_WAIT_TIMEOUT;
            error.message = String.format(TIMEOUT,new Object[]{request.apiName,conn.getLocalHost(),waitTime});
            return error;
        }

        return null;
    }

    private static ErrorPacket checkActive(Endpoint endpoint, AbstractServiceRequestPacket request) {
        Service service = endpoint.getService();
        if (!service.isActive() || !endpoint.isActive()) {
            ErrorPacket error = new ErrorPacket();
            AbstractServicePacket.copyHead(request, error);
            error.errorCode = VenusExceptionCodeConstant.SERVICE_INACTIVE_EXCEPTION;
            StringBuffer buffer = new StringBuffer();
            buffer.append("Service=").append(endpoint.getService().getName());
            if (!service.isActive()) {
                buffer.append(" is not active");
            }

            if (!endpoint.isActive()) {
                buffer.append(", endpoint=").append(endpoint.getName()).append(" is not active");
            }

            error.message = buffer.toString();
            return error;
        }

        return null;
    }

    private static ErrorPacket checkVersion(Endpoint endpoint, AbstractServiceRequestPacket request) {
        Service service = endpoint.getService();

        // service version check
        Range range = service.getVersionRange();
        if (range == null || range.contains(request.serviceVersion)) {
            return null;
        } else {
            ErrorPacket error = new ErrorPacket();
            AbstractServicePacket.copyHead(request, error);
            error.errorCode = VenusExceptionCodeConstant.SERVICE_VERSION_NOT_ALLOWD_EXCEPTION;
            error.message = "Service=" + endpoint.getService().getName() + ",version=" + request.serviceVersion + " not allow";
            return error;
        }
    }

    @Override
    public void init() throws InitialisationException {
        if (executor == null && executorEnabled && !useThreadLocalExecutor && maxExecutionThread > 0) {
            executor = Executors.newFixedThreadPool(maxExecutionThread);
        }
    }

    public void postMessageBack(Connection conn, VenusRouterPacket routerPacket, AbstractServicePacket source, AbstractServicePacket result) {
        if (routerPacket == null) {
            conn.write(result.toByteBuffer());
        } else {
            routerPacket.data = result.toByteArray();
            conn.write(routerPacket.toByteBuffer());
        }
    }
}
