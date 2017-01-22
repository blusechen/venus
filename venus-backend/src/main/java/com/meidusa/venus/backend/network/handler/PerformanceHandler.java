package com.meidusa.venus.backend.network.handler;

import com.meidusa.fastjson.JSON;
import com.meidusa.fastmark.feature.SerializerFeature;
import com.meidusa.venus.backend.Response;
import com.meidusa.venus.backend.services.Endpoint;
import com.meidusa.venus.backend.services.xml.bean.PerformanceLogger;
import com.meidusa.venus.io.packet.ErrorPacket;
import com.meidusa.venus.io.packet.PacketConstant;
import com.meidusa.venus.io.packet.serialize.SerializeServiceRequestPacket;
import com.meidusa.venus.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by godzillahua on 7/4/16.
 */
public class PerformanceHandler {
    private static Logger performanceLogger = LoggerFactory.getLogger("venus.backend.performance");
    private static SerializerFeature[] JSON_FEATURE = new SerializerFeature[]{SerializerFeature.ShortString,SerializerFeature.IgnoreNonFieldGetter,SerializerFeature.SkipTransientField};
    public static void logPerformance(Endpoint endpoint, SerializeServiceRequestPacket request, long queuedTime,
                                      long executeTime, String remoteIp, String sourceIP, Object result) {
        String traceId;

        if (request == null) {
            traceId = UUID.toString(PacketConstant.EMPTY_TRACE_ID);
        }else {
            traceId = UUID.toString(request.traceId);
        }

        String apiName = "";
        if (request != null) {
            apiName = request.apiName;
        }

        long clientId = -1L;
        if (request != null) {
            clientId = request.clientId;
        }

        long requestId = -1L;
        if (request != null) {
            requestId = request.clientRequestId;
        }

        Map<String, Object> parameterMap = null;
        if (request != null) {
            parameterMap = request.parameterMap;
        }else {
            parameterMap = new HashMap<String, Object>();
        }


        StringBuffer buffer = new StringBuffer();
        buffer.append("[").append(queuedTime).append(",").append(executeTime).append("]ms, (*server*) traceID=").append(traceId).append(", api=").append(apiName).append(", ip=")
                .append(remoteIp).append(", sourceIP=").append(sourceIP).append(", clientID=")
                .append(clientId).append(", requestID=").append(requestId);

        PerformanceLogger pLevel = null;

        if (endpoint != null) {
            pLevel = endpoint.getPerformanceLogger();
        }

        if (pLevel != null) {

            if (pLevel.isPrintParams()) {
                buffer.append(", params=");
                buffer.append(JSON.toJSONString(parameterMap, JSON.DEFAULT_GENERATE_FEATURE,JSON_FEATURE));
            }
            if (pLevel.isPrintResult()) {
                buffer.append(", result=");
                if (result instanceof ErrorPacket) {
                    buffer.append("{ errorCode=").append(((ErrorPacket) result).errorCode);
                    buffer.append(", message=").append(((ErrorPacket) result).message);
                    buffer.append("}");
                } else if (result instanceof Response) {
                    if (((Response) result).getErrorCode() > 0) {
                        buffer.append("{ errorCode=").append(((Response) result).getErrorCode());
                        buffer.append(", message=\"").append(((Response) result).getErrorMessage()).append("\"");
                        buffer.append(", className=\"").append(((Response) result).getException().getClass().getSimpleName()).append("\"");
                        buffer.append("}");
                    } else {
                        buffer.append(JSON.toJSONString(result, JSON_FEATURE));
                    }
                }
            }

            if (queuedTime >= pLevel.getError() || executeTime >= pLevel.getError() || queuedTime + executeTime >= pLevel.getError()) {
                if (performanceLogger.isErrorEnabled()) {
                    performanceLogger.error(buffer.toString());
                }
            } else if (queuedTime >= pLevel.getWarn() || executeTime >= pLevel.getWarn() || queuedTime + executeTime >= pLevel.getWarn()) {
                if (performanceLogger.isWarnEnabled()) {
                    performanceLogger.warn(buffer.toString());
                }
            } else if (queuedTime >= pLevel.getInfo() || executeTime >= pLevel.getInfo() || queuedTime + executeTime >= pLevel.getInfo()) {
                if (performanceLogger.isInfoEnabled()) {
                    performanceLogger.info(buffer.toString());
                }
            } else {
                if (performanceLogger.isDebugEnabled()) {
                    performanceLogger.debug(buffer.toString());
                }
            }

        } else {
            if (performanceLogger.isDebugEnabled()) {
                buffer.append(", params=");
                buffer.append(JSON.toJSONString(parameterMap, new SerializerFeature[]{SerializerFeature.ShortString}));
                if (result == null) {
                    buffer.append(", result=<null>");
                } else {
                    buffer.append(", result=");
                    if (result instanceof ErrorPacket) {
                        buffer.append("{ errorCode=").append(((ErrorPacket) result).errorCode);
                        buffer.append(", message=").append(((ErrorPacket) result).message);
                        buffer.append("}");
                    } else if (result instanceof Response) {
                        if (((Response) result).getErrorCode() > 0) {
                            buffer.append("{errorCode=").append(((Response) result).getErrorCode());
                            buffer.append(", message=\"").append(((Response) result).getErrorMessage()).append("\"");
                            buffer.append(", className=\"").append(((Response) result).getException().getClass().getSimpleName()).append("\"");
                            buffer.append("}");
                        } else {
                            buffer.append(JSON.toJSONString(result, new SerializerFeature[]{SerializerFeature.ShortString}));
                        }
                    }
                }
            }
            if (queuedTime >= 30 * 1000 || executeTime >= 30 * 1000 || queuedTime + executeTime >= 30 * 1000) {
                if (performanceLogger.isErrorEnabled()) {
                    performanceLogger.error(buffer.toString());
                }
            } else if (queuedTime >= 10 * 1000 || executeTime >= 10 * 1000 || queuedTime + executeTime >= 10 * 1000) {
                if (performanceLogger.isWarnEnabled()) {
                    performanceLogger.warn(buffer.toString());
                }
            } else if (queuedTime >= 5 * 1000 || executeTime >= 5 * 1000 || queuedTime + executeTime >= 5 * 1000) {
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
}
