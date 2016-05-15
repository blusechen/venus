package com.meidusa.venus.backend.network.handler;

import com.meidusa.fastbson.exception.SerializeException;
import com.meidusa.fastjson.JSONException;
import com.meidusa.venus.backend.services.Endpoint;
import com.meidusa.venus.backend.services.Service;
import com.meidusa.venus.exception.CodedException;
import com.meidusa.venus.exception.VenusExceptionCodeConstant;
import com.meidusa.venus.exception.VenusExceptionLevel;
import com.meidusa.venus.io.network.VenusFrontendConnection;
import com.meidusa.venus.io.packet.AbstractServicePacket;
import com.meidusa.venus.io.packet.AbstractServiceRequestPacket;
import com.meidusa.venus.io.packet.ErrorPacket;
import com.meidusa.venus.io.packet.serialize.SerializeServiceRequestPacket;
import com.meidusa.venus.util.Range;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by huawei on 5/15/16.
 */
public class ExceptionHandler {

    private static Logger logger = LoggerFactory.getLogger(ExceptionHandler.class);

    private static final String TIMEOUT = "waiting-timeout for execution,api=%s,ip=%s,time=%d (ms)";

    public static ErrorPacket checkActive(Endpoint endpoint, AbstractServiceRequestPacket request) {
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

    public static ErrorPacket checkVersion(Endpoint endpoint, AbstractServiceRequestPacket request) {
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

    public static ErrorPacket checkTimeout(VenusFrontendConnection conn, Endpoint endpoint, AbstractServiceRequestPacket request, long waitTime) {
        if (waitTime > endpoint.getTimeWait()) {
            ErrorPacket error = new ErrorPacket();
            AbstractServicePacket.copyHead(request, error);
            error.errorCode = VenusExceptionCodeConstant.INVOCATION_ABORT_WAIT_TIMEOUT;
            error.message = String.format(TIMEOUT, new Object[]{request.apiName, conn.getLocalHost(), waitTime});
            return error;
        }

        return null;
    }

    public static ErrorPacket getErrorPacket(Throwable e, AbstractServicePacket source) {
        ErrorPacket error = new ErrorPacket();
        AbstractServicePacket.copyHead(source, error);
        if (e instanceof CodedException || CodeMapScanner.getCodeMap().containsKey(e.getClass())) {
            if(e instanceof CodedException){
                CodedException codeEx = (CodedException) e;
                error.errorCode = codeEx.getErrorCode();
            }else{
                error.errorCode = CodeMapScanner.getCodeMap().get(e.getClass());
            }
        } else {
            if (e instanceof JSONException || e instanceof SerializeException) {
                error.errorCode = VenusExceptionCodeConstant.REQUEST_ILLEGAL;
            } else {
                error.errorCode = VenusExceptionCodeConstant.UNKNOW_EXCEPTION;
            }
        }
        error.message = e.getMessage();

        return error;
    }

    public static void logError(VenusFrontendConnection conn, SerializeServiceRequestPacket request, Exception e, String host, int port, String sourceIp) {
        if (request != null) {
            if (e instanceof VenusExceptionLevel) {
                if (((VenusExceptionLevel) e).getLevel() != null) {
                    LogHandler.logDependsOnLevel(((VenusExceptionLevel) e).getLevel(), logger, e.getMessage() + " client:{clientID=" + request.clientId
                            + ",ip=" + host + ":" + port + ",sourceIP=" + sourceIp + ", apiName=" + request.apiName
                            + "}", e);
                }
            } else {
                if (logger.isDebugEnabled()) {
                    logger.debug(e.getMessage() + " [ip=" + host + ":" + port + ",sourceIP=" + sourceIp + ", apiName="
                            + request.apiName + "]", e);
                }
            }
        }else {
            logger.error(e.getMessage() + " [ip=" + conn.getHost() + ":" + conn.getPort() + ",sourceIP=" + sourceIp  + ", apiName=" + request.apiName + "]", e);
        }
    }
}
