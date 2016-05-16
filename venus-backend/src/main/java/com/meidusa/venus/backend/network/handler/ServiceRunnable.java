package com.meidusa.venus.backend.network.handler;

import com.meidusa.toolkit.common.util.Tuple;
import com.meidusa.toolkit.net.util.InetAddressUtil;
import com.meidusa.toolkit.util.TimeUtil;
import com.meidusa.venus.backend.DefaultEndpointInvocation;
import com.meidusa.venus.backend.EndpointInvocation;
import com.meidusa.venus.backend.Response;
import com.meidusa.venus.backend.VenusStatus;
import com.meidusa.venus.backend.context.RequestContext;
import com.meidusa.venus.backend.profiling.UtilTimerStack;
import com.meidusa.venus.backend.services.Endpoint;
import com.meidusa.venus.backend.services.Service;
import com.meidusa.venus.exception.*;
import com.meidusa.venus.extension.monitor.MonitorConstants;
import com.meidusa.venus.extension.monitor.VenusMonitorDelegate;
import com.meidusa.venus.io.ServiceFilter;
import com.meidusa.venus.io.network.VenusFrontendConnection;
import com.meidusa.venus.io.packet.*;
import com.meidusa.venus.io.packet.serialize.SerializeServiceRequestPacket;
import com.meidusa.venus.io.packet.serialize.SerializeServiceResponsePacket;
import com.meidusa.venus.io.serializer.Serializer;
import com.meidusa.venus.io.serializer.SerializerFactory;
import com.meidusa.venus.service.monitor.MonitorRuntime;
import com.meidusa.venus.util.ThreadLocalConstant;
import com.meidusa.venus.util.ThreadLocalMap;
import com.meidusa.venus.util.Utils;
import com.meidusa.venus.util.VenusTracerUtil;
import com.meidusa.venus.util.concurrent.MultiQueueRunnable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.beans.PropertyDescriptor;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by huawei on 5/15/16.
 */
public class ServiceRunnable extends MultiQueueRunnable {

    private static Logger logger = LoggerFactory.getLogger(ServiceRunnable.class);
    private static Logger performanceLogger = LoggerFactory.getLogger("venus.backend.performance");
    private static Logger INVOKER_LOGGER = LoggerFactory.getLogger("venus.service.invoker");
    private static String ENDPOINT_INVOKED_TIME = "invoked Totle Time: ";

    private Endpoint endpoint;
    private VenusFrontendConnection conn;
    private RequestContext context;
    private EndpointInvocation.ResultType resultType;
    private ServiceFilter filter;
    private byte[] traceID;
    private SerializeServiceRequestPacket request;
    private short serializeType;
    private VenusRouterPacket routerPacket;
    private RemotingInvocationListener<Serializable> invocationListener;
    private VenusExceptionFactory venusExceptionFactory;
    private Tuple<Long, byte[]> data;
    private String apiName;
    private String sourceIp;

    public ServiceRunnable(VenusFrontendConnection conn, Endpoint endpoint, RequestContext context, EndpointInvocation.ResultType resultType, ServiceFilter filter,
                           VenusRouterPacket routerPacket, SerializeServiceRequestPacket request,
                           short serializeType, RemotingInvocationListener<Serializable> invocationListener,
                           VenusExceptionFactory venusExceptionFactory, Tuple<Long, byte[]> data) {
        this.conn = conn;
        this.endpoint = endpoint;
        this.context = context;
        this.resultType = resultType;
        this.filter = filter;
        this.request = request;
        this.traceID = request.traceId;
        this.serializeType = serializeType;
        this.routerPacket = routerPacket;
        this.invocationListener = invocationListener;
        this.venusExceptionFactory = venusExceptionFactory;
        this.data = data;
        this.apiName = request.apiName;
        this.sourceIp = conn.getHost();
        if (routerPacket != null) {
            sourceIp = InetAddressUtil.intToAddress(routerPacket.srcIP);
        }
    }

    @Override
    public void doRun() {
        AbstractServicePacket resultPacket = null;
        ResponseHandler responseHandler = new ResponseHandler();
        long startRunTime = TimeUtil.currentTimeMillis();
        Response result = null;
        if (conn.isClosed() && resultType == EndpointInvocation.ResultType.RESPONSE) {
            return;
        }
        try {
            ThreadLocalMap.put(VenusTracerUtil.REQUEST_TRACE_ID, traceID);
            ThreadLocalMap.put(ThreadLocalConstant.REQUEST_CONTEXT, context);

            if (filter != null) {
                filter.before(request);
            }
            // invoke service endpoint
            result = handleRequest(context, endpoint);

            if (result.getErrorCode() == 0) {
                if (resultType == EndpointInvocation.ResultType.RESPONSE) {
                    Serializer serializer = SerializerFactory.getSerializer(serializeType);
                    ServiceResponsePacket response = new SerializeServiceResponsePacket(serializer, endpoint.getMethod()
                            .getGenericReturnType());
                    AbstractServicePacket.copyHead(request, response);
                    response.result = result.getResult();
                    resultPacket = response;
                    responseHandler.postMessageBack(conn, routerPacket, request, response);
                } else if (resultType == EndpointInvocation.ResultType.OK) {
                    OKPacket ok = new OKPacket();
                    AbstractServicePacket.copyHead(request, ok);
                    resultPacket = ok;
                    responseHandler.postMessageBack(conn, routerPacket, request, ok);
                } else if (resultType == EndpointInvocation.ResultType.NOTIFY) {
                    if (invocationListener != null && !invocationListener.isResponsed()) {
                        invocationListener.onException(new ServiceNotCallbackException("Server side not call back error"));
                    }
                }
            } else {
                if (resultType == EndpointInvocation.ResultType.RESPONSE || resultType == EndpointInvocation.ResultType.OK) {
                    ErrorPacket error = new ErrorPacket();
                    AbstractServicePacket.copyHead(request, error);
                    error.errorCode = result.getErrorCode();
                    error.message = result.getErrorMessage();
                    Throwable throwable = result.getException();
                    if (throwable != null) {
                        Serializer serializer = SerializerFactory.getSerializer(serializeType);
                        Map<String, PropertyDescriptor> mpd = Utils.getBeanPropertyDescriptor(throwable.getClass());
                        Map<String, Object> additionalData = new HashMap<String, Object>();

                        for (Map.Entry<String, PropertyDescriptor> entry : mpd.entrySet()) {
                            additionalData.put(entry.getKey(), entry.getValue().getReadMethod().invoke(throwable));
                        }
                        error.additionalData = serializer.encode(additionalData);
                    }
                    resultPacket = error;
                    responseHandler.postMessageBack(conn, routerPacket, request, error);
                } else if (resultType == EndpointInvocation.ResultType.NOTIFY) {
                    if (invocationListener != null && !invocationListener.isResponsed()) {
                        if (result.getException() == null) {
                            invocationListener.onException(new DefaultVenusException(result.getErrorCode(), result.getErrorMessage()));
                        } else {
                            invocationListener.onException(result.getException());
                        }
                    }
                }
            }

        } catch (Exception e) {
            ErrorPacket error = new ErrorPacket();
            AbstractServicePacket.copyHead(request, error);
            Integer code = CodeMapScanner.getCodeMap().get(e.getClass());
            if (code != null) {
                error.errorCode = code;
            } else {
                if (e instanceof CodedException) {
                    CodedException codeEx = (CodedException) e;
                    error.errorCode = codeEx.getErrorCode();
                    if (logger.isDebugEnabled()) {
                        logger.debug("error when invoke", e);
                    }
                } else {
                    try {
                        Method method = e.getClass().getMethod("getErrorCode", new Class[]{});
                        int i = (Integer) method.invoke(e);
                        error.errorCode = i;
                        if (logger.isDebugEnabled()) {
                            logger.debug("error when invoke", e);
                        }
                    } catch (Exception e1) {
                        error.errorCode = VenusExceptionCodeConstant.UNKNOW_EXCEPTION;
                        if (logger.isWarnEnabled()) {
                            logger.warn("error when invoke", e);
                        }
                    }
                }
            }
            resultPacket = error;
            error.message = e.getMessage();
            responseHandler.postMessageBack(conn, routerPacket, request, error);

            return;
        } catch (OutOfMemoryError e) {
            ErrorPacket error = new ErrorPacket();
            AbstractServicePacket.copyHead(request, error);
            error.errorCode = VenusExceptionCodeConstant.SERVICE_UNAVAILABLE_EXCEPTION;
            error.message = e.getMessage();
            resultPacket = error;
            responseHandler.postMessageBack(conn, routerPacket, request, error);
            VenusStatus.getInstance().setStatus(PacketConstant.VENUS_STATUS_OUT_OF_MEMORY);
            logger.error("error when invoke", e);
            throw e;
        } catch (Error e) {
            ErrorPacket error = new ErrorPacket();
            AbstractServicePacket.copyHead(request, error);
            error.errorCode = VenusExceptionCodeConstant.SERVICE_UNAVAILABLE_EXCEPTION;
            error.message = e.getMessage();
            resultPacket = error;
            responseHandler.postMessageBack(conn, routerPacket, request, error);
            logger.error("error when invoke", e);
            return;
        } finally {
            long endRunTime = TimeUtil.currentTimeMillis();
            long queuedTime = startRunTime - data.left;
            long executeTime = endRunTime - startRunTime;
            if (endpoint.getTimeWait() > ((queuedTime + executeTime)/1000)) {
                VenusMonitorDelegate.getInstance().reportMetric(VenusMonitorDelegate.getExecuteTimeoutKey(request.apiName), MonitorConstants.metricCount);
            }
            MonitorRuntime.getInstance().calculateAverage(endpoint.getService().getName(), endpoint.getName(), executeTime, false);
            PerformanceHandler.logPerformance(endpoint, request, queuedTime, executeTime, conn.getHost(), sourceIp, result);
            if (filter != null) {
                filter.after(resultPacket);
            }
            ThreadLocalMap.remove(ThreadLocalConstant.REQUEST_CONTEXT);
            ThreadLocalMap.remove(VenusTracerUtil.REQUEST_TRACE_ID);
        }

    }

    @Override
    public String getName() {
        return apiName;
    }


    private Response handleRequest(RequestContext context, Endpoint endpoint) {
        Response response = new Response();
        DefaultEndpointInvocation invocation = new DefaultEndpointInvocation(context, endpoint);
        try {
            UtilTimerStack.push(ENDPOINT_INVOKED_TIME);
            response.setResult(invocation.invoke());
        } catch (Throwable e) {
            VenusMonitorDelegate.getInstance().reportError(e.getMessage(), e);
            if (e instanceof ServiceInvokeException) {
                e = ((ServiceInvokeException) e).getTargetException();
            }
            if (e instanceof Exception) {
                response.setException((Exception) e);
            } else {
                response.setException(new DefaultVenusException(e.getMessage(), e));
            }

            Integer code = CodeMapScanner.getCodeMap().get(e.getClass());

            if (code != null) {
                response.setErrorCode(code);
                response.setErrorMessage(e.getMessage());
            } else {
                response.setError(e, venusExceptionFactory);
            }

            Service service = endpoint.getService();
            if (e instanceof VenusExceptionLevel) {
                if (((VenusExceptionLevel) e).getLevel() != null) {
                    LogHandler.logDependsOnLevel(((VenusExceptionLevel) e).getLevel(), INVOKER_LOGGER, e.getMessage() + " " + context.getRequestInfo().getRemoteIp() + " "
                            + service.getName() + ":" + endpoint.getMethod().getName() + " " + Utils.toString(context.getParameters()), e);
                }
            } else {
                if (e instanceof RuntimeException && !(e instanceof CodedException)) {
                    INVOKER_LOGGER.error(e.getMessage() + " " + context.getRequestInfo().getRemoteIp() + " " + service.getName() + ":" + endpoint.getMethod().getName()
                            + " " + Utils.toString(context.getParameters()), e);
                } else {
                    if (endpoint.isAsync()) {
                        if (INVOKER_LOGGER.isErrorEnabled()) {

                            INVOKER_LOGGER.error(e.getMessage() + " " + context.getRequestInfo().getRemoteIp() + " " + service.getName() + ":"
                                    + endpoint.getMethod().getName() + " " + Utils.toString(context.getParameters()), e);
                        }
                    } else {
                        if (INVOKER_LOGGER.isDebugEnabled()) {
                            INVOKER_LOGGER.debug(e.getMessage() + " " + context.getRequestInfo().getRemoteIp() + " " + service.getName() + ":"
                                    + endpoint.getMethod().getName() + " " + Utils.toString(context.getParameters()), e);
                        }
                    }
                }
            }
        } finally {
            UtilTimerStack.pop(ENDPOINT_INVOKED_TIME);
        }

        return response;
    }

}
