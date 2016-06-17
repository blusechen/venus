package com.meidusa.venus.backend.network.handler;

import com.meidusa.fastjson.JSON;
import com.meidusa.fastmark.feature.SerializerFeature;
import com.meidusa.toolkit.common.bean.util.Initialisable;
import com.meidusa.toolkit.common.bean.util.InitialisationException;
import com.meidusa.toolkit.common.util.Tuple;
import com.meidusa.toolkit.net.MessageHandler;
import com.meidusa.toolkit.util.StringUtil;
import com.meidusa.toolkit.util.TimeUtil;
import com.meidusa.venus.backend.EndpointInvocation.ResultType;
import com.meidusa.venus.backend.RequestInfo;
import com.meidusa.venus.backend.VenusStatus;
import com.meidusa.venus.backend.context.RequestContext;
import com.meidusa.venus.backend.services.Endpoint;
import com.meidusa.venus.backend.services.ServiceManager;
import com.meidusa.venus.exception.ServiceVersionNotAllowException;
import com.meidusa.venus.exception.VenusExceptionCodeConstant;
import com.meidusa.venus.exception.VenusExceptionFactory;
import com.meidusa.venus.extension.monitor.MonitorConstants;
import com.meidusa.venus.extension.monitor.VenusMonitorDelegate;
import com.meidusa.venus.io.ServiceFilter;
import com.meidusa.venus.io.network.VenusFrontendConnection;
import com.meidusa.venus.io.packet.*;
import com.meidusa.venus.io.packet.serialize.SerializeServiceRequestPacket;
import com.meidusa.venus.io.serializer.Serializer;
import com.meidusa.venus.io.serializer.SerializerFactory;
import com.meidusa.venus.notify.InvocationListener;
import com.meidusa.venus.notify.ReferenceInvocationListener;
import com.meidusa.venus.util.VenusTracerUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * singleton handler
 *
 * @author structchen
 */
@SuppressWarnings({"unchecked", "rawtypes"})
public class ServiceInvokeMessageHandler implements MessageHandler<VenusFrontendConnection, Tuple<Long, byte[]>>, Initialisable {
    private static SerializerFeature[] JSON_FEATURE = new SerializerFeature[]{SerializerFeature.ShortString};

    private static Logger logger = LoggerFactory.getLogger(ServiceInvokeMessageHandler.class);

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
    public void handle(final VenusFrontendConnection conn, final Tuple<Long, byte[]> data) {

        final long waitTime = TimeUtil.currentTimeMillis() - data.left;

        byte[] message = data.right;

        int type = AbstractServicePacket.getType(message);
        VenusRouterPacket routerPacket = null;
        byte serializeType;
        String sourceIp;
        ResponseHandler responseHandler = new ResponseHandler();
        if (PacketConstant.PACKET_TYPE_ROUTER == type) {
            RouteMessageHandler handler = new RouteMessageHandler(message).init();
            routerPacket = handler.getRouterPacket();
            type = handler.getType();
            message = handler.getMessage();
            serializeType = handler.getSerializeType();
            sourceIp = handler.getSourceIp();
        } else {
            serializeType = conn.getSerializeType();
            sourceIp = conn.getHost();
        }

        final byte packetSerializeType = serializeType;
        switch (type) {
            case PacketConstant.PACKET_TYPE_PING:
                PingPacket ping = new PingPacket();
                ping.init(message);
                PongPacket pong = new PongPacket();
                AbstractServicePacket.copyHead(ping, pong);
                responseHandler.postMessageBack(conn, null, ping, pong);
                if (logger.isDebugEnabled()) {
                    logger.debug("receive ping packet from " + conn.getHost() + ":" + conn.getPort());
                }
                break;
            case PacketConstant.PACKET_TYPE_PONG:
                break;
            case PacketConstant.PACKET_TYPE_VENUS_STATUS_REQUEST:
                VenusStatusRequestPacket sr = new VenusStatusRequestPacket();
                sr.init(message);
                VenusStatusResponsePacket response = new VenusStatusResponsePacket();
                AbstractServicePacket.copyHead(sr, response);
                if (sr.newStatus != 0) {
                    VenusStatus.getInstance().setStatus(sr.newStatus);
                }
                response.status = VenusStatus.getInstance().getStatus();
                responseHandler.postMessageBack(conn, null, sr, response);
                break;
            case PacketConstant.PACKET_TYPE_SERVICE_REQUEST:
                RequestHandler requestHandler = new RequestHandler();
                SerializeServiceRequestPacket request = null;
                Endpoint ep = null;
                ServiceAPIPacket apiPacket = new ServiceAPIPacket();
                try {
                    ServicePacketBuffer packetBuffer = new ServicePacketBuffer(message);
                    apiPacket.init(packetBuffer);
                    VenusMonitorDelegate.getInstance().reportMetric(VenusMonitorDelegate.getInvokeKey(apiPacket.apiName), MonitorConstants.metricCount);
                    ep = getServiceManager().getEndpoint(apiPacket.apiName);
                    Serializer serializer = SerializerFactory.getSerializer(packetSerializeType);
                    request = new SerializeServiceRequestPacket(serializer, ep.getParameterTypeDict());
                    packetBuffer.setPosition(0);
                    request.init(packetBuffer);
                    VenusTracerUtil.logReceive(request.traceId, request.apiName, JSON.toJSONString(request.parameterMap, JSON_FEATURE));
                } catch (Exception e) {
                    VenusMonitorDelegate.getInstance().reportMetric(VenusMonitorDelegate.getParseErrorKey(apiPacket.apiName), MonitorConstants.metricCount);
                    VenusMonitorDelegate.getInstance().reportError("解析request请求异常", e);
                    ErrorPacket error = ExceptionHandler.getErrorPacket(e, apiPacket);
                    if (filter != null) {
                        filter.before(request);
                    }
                    responseHandler.postMessageBack(conn, routerPacket, request, error);
                    if (filter != null) {
                        filter.after(error);
                    }

                    String host = conn.getHost();
                    int port = conn.getPort();
                    PerformanceHandler.logPerformance(ep, request, waitTime, 0, host, sourceIp, error);
                    ExceptionHandler.logError(conn, request, e, host, port, sourceIp);
                    return;
                }

                final Endpoint endpoint = ep;

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
                    errorPacket = ExceptionHandler.checkVersion(endpoint, request);
                }
                if (errorPacket == null) {
                    errorPacket = ExceptionHandler.checkActive(endpoint, request);
                }

                /**
                 * 判断超时
                 */
                boolean isTimeout = false;
                if (errorPacket == null) {
                    errorPacket = ExceptionHandler.checkTimeout(conn, endpoint, request, waitTime);
                    if (errorPacket != null) {
                        isTimeout = true;
                    }
                }

                __TIMEOUT:
                {

                    if (errorPacket != null) {
                        if (resultType == ResultType.NOTIFY) {
                            if (isTimeout) {
                                VenusMonitorDelegate.getInstance().reportMetric(VenusMonitorDelegate.getQueueTimeoutKey(request.apiName), MonitorConstants.metricCount);
                                break __TIMEOUT;
                            }
                            if (invocationListener != null) {
                                invocationListener.onException(new ServiceVersionNotAllowException(errorPacket.message));
                            } else {
                                responseHandler.postMessageBack(conn, routerPacket, request, errorPacket);
                            }
                        } else {
                            VenusMonitorDelegate.getInstance().reportMetric(VenusMonitorDelegate.getQueueTimeoutKey(request.apiName), MonitorConstants.metricCount);
                            responseHandler.postMessageBack(conn, routerPacket, request, errorPacket);
                        }
                        if (filter != null) {
                            filter.before(request);
                        }
                        PerformanceHandler.logPerformance(endpoint, request, waitTime, 0, conn.getHost(), sourceIp, errorPacket);
                        if (filter != null) {
                            filter.after(errorPacket);
                        }
                        return;
                    }
                }
                RequestInfo requestInfo = requestHandler.getRequestInfo(packetSerializeType, conn, routerPacket);
                RequestContext context = requestHandler.createContext(requestInfo, conn, endpoint, request.parameterMap);
                ServiceRunnable runnable = new ServiceRunnable(conn, endpoint, context,
                        resultType, filter, routerPacket,
                        request, serializeType, invocationListener,
                        venusExceptionFactory, data);
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
                responseHandler.postMessageBack(conn, routerPacket, head, error);

        }

    }

    @Override
    public void init() throws InitialisationException {
        if (executor == null && executorEnabled && !useThreadLocalExecutor && maxExecutionThread > 0) {
            executor = Executors.newFixedThreadPool(maxExecutionThread);
        }
    }
}
