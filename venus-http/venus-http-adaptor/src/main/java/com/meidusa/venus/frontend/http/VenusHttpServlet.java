package com.meidusa.venus.frontend.http;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.WebApplicationContext;

import com.meidusa.fastjson.JSON;
import com.meidusa.fastjson.JSONException;
import com.meidusa.fastmark.feature.SerializerFeature;
import com.meidusa.toolkit.common.poolable.ObjectPool;
import com.meidusa.toolkit.util.StringUtil;
import com.meidusa.toolkit.util.TimeUtil;
import com.meidusa.venus.annotations.RemoteException;
import com.meidusa.venus.annotations.util.AnnotationUtil;
import com.meidusa.venus.backend.RequestInfo;
import com.meidusa.venus.backend.Response;
import com.meidusa.venus.backend.profiling.UtilTimerStack;
import com.meidusa.venus.backend.view.MediaTypes;
import com.meidusa.venus.exception.CodedException;
import com.meidusa.venus.exception.ExceptionLevel;
import com.meidusa.venus.exception.VenusExceptionCodeConstant;
import com.meidusa.venus.exception.VenusExceptionLevel;
import com.meidusa.venus.io.network.VenusBIOConnection;
import com.meidusa.venus.io.packet.AbstractServicePacket;
import com.meidusa.venus.io.packet.AbstractVenusPacket;
import com.meidusa.venus.io.packet.ErrorPacket;
import com.meidusa.venus.io.packet.PacketConstant;
import com.meidusa.venus.io.serializer.json.JsonSerializer;
import com.meidusa.venus.service.monitor.MonitorRuntime;
import com.meidusa.venus.util.UUID;
import com.meidusa.venus.util.VenusTracerUtil;

public class VenusHttpServlet extends HttpServlet {
	
	private static SerializerFeature[] JSON_FEATURE = new SerializerFeature[]{SerializerFeature.ShortString};
	
    private static final String _VENUS_TRACE_ID = "_venus_trace_id_";
    private static final String _VENUS_CLIENT_ID = "_venus_client_id_";
    private static final String VERSION="v";
    private static final String VERSION_HEADER = "version";
    private static Logger logger = LoggerFactory.getLogger(VenusHttpServlet.class);
    private static String ENDPOINT_INVOKED_TIME = "invoked Total Time: ";
    // private static String patternString = "([a-zA-Z_0-9.]+)/([a-zA-Z_0-9]+)(?:/(\\d+))*(?:/\\S*)*(?:\\?(?:.*?)*)*";
    private Pattern servicePattern = null;
    private JsonSerializer serializer = new JsonSerializer();
    private String urlPattern;
    private static final long serialVersionUID = 1L;
    private ApplicationContext context = null;
    
    private ObjectPool pool = null;

    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        String servletName = config.getServletName();

        urlPattern = config.getInitParameter("uri-pattern");

        if (StringUtils.isEmpty(urlPattern)) {
            throw new ServletException("servlet=" + servletName + "  init-param=uri-prefix cannot be null");
        }

        urlPattern = urlPattern.trim();

        servicePattern = Pattern.compile(urlPattern);

        context = (ApplicationContext) config.getServletContext().getAttribute(WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE);
        try {
            VenusPoolFactory venusPoolFactory = context.getBean(VenusPoolFactory.class);
            pool = venusPoolFactory.getPool();
        } catch (BeansException e) {

        }
    }

    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doPost(req, resp);
    }

    protected void doPost(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
        String uri = req.getRequestURI().trim();

        if (!req.getContextPath().equals("/")) {
            uri = uri.substring(req.getContextPath().length());
        }

        if (!urlPattern.endsWith("/")) {
            if (uri.endsWith("/")) {
                uri = uri.substring(0, uri.length() - 1);
            }
        }
        Matcher matcher = servicePattern.matcher(uri);

        if (!matcher.matches() || matcher.groupCount() < 2) {
            Response result = new Response();
            result.setErrorCode(VenusExceptionCodeConstant.REQUEST_ILLEGAL);
            result.setErrorMessage("requst not matcher");
            writeResponse(req, resp, result);
            return;
        }

        resp.setContentType(MediaTypes.APPLICATION_JSON);
        Map<String, Object> parameterMap = null;
        String service = matcher.group(1);
        String method = matcher.group(2);
        String clientID = req.getHeader(_VENUS_CLIENT_ID);
        String traceId = req.getHeader(_VENUS_TRACE_ID);
        String apiName = service + "." + method;
        // ResultType resultType = ResultType.RESPONSE;
        String v = req.getParameter(VERSION);
        if (StringUtils.isEmpty(v)) {
        	v = req.getHeader(VERSION_HEADER);
        }
        // String sign = req.getParameter("sign");
        int version = 0;

        try {
            version = Integer.parseInt(v == null ? "0" : v);
        } catch (java.lang.NumberFormatException e) {
            version = 0;
        }

        // check endpoint
        try {

            if (req.getContentLength() > 0) {
                byte[] message = new byte[req.getContentLength()];
                req.getInputStream().read(message);
                parameterMap = JSON.parseObject(new String(message, "UTF-8"));
            } else {
                parameterMap = new HashMap<String, Object>();
            }

            Set<String> keys = req.getParameterMap().keySet();
            for (String key : keys) {
                parameterMap.put(key, req.getParameter(key));
            }
            parameterMap.remove(VERSION);
        } catch (Exception e) {
            int errorCode = VenusExceptionCodeConstant.UNKNOW_EXCEPTION;

            if (e instanceof JSONException) {
                errorCode = VenusExceptionCodeConstant.REQUEST_ILLEGAL;
            }

            if (e instanceof CodedException) {
                CodedException codeEx = (CodedException) e;
                errorCode = codeEx.getErrorCode();
            }

            if (e instanceof VenusExceptionLevel) {
                if (((VenusExceptionLevel) e).getLevel() != null) {
                    logDependsOnLevel(((VenusExceptionLevel) e).getLevel(), logger,
                            e.getMessage() + " client:{clientID=" + clientID + ",ip=" + req.getRemoteAddr() + ", apiName=" + service + "." + method + "}", e);
                }
            } else {
                if (logger.isDebugEnabled()) {
                    logger.debug(e.getMessage() + " [ip=" + req.getRemoteAddr() + ", apiName=" + apiName + "]", e);
                }
            }

            Response result = new Response();
            result.setErrorCode(errorCode);
            result.setErrorMessage(e.getMessage());

            writeResponse(req, resp, result);
            return;
        }

        Response result = null;
        long startTime = TimeUtil.currentTimeMillis();
        boolean isError = false;
        try {
        	if(StringUtil.isEmpty(traceId)){
        		traceId = new UUID(VenusTracerUtil.randomUUID()).toString();
        	}
            // invoke service endpoint
            result = handleRequest(apiName, version, traceId, parameterMap);

           /* if (logger.isDebugEnabled()) {
                logger.debug("receive service request packet from " + req.getRemoteAddr());
                logger.debug("sending response to " + req.getRemoteAddr() + ": " + result + " ");
            }*/
        } catch (Exception e) {
            int errorCode = VenusExceptionCodeConstant.UNKNOW_EXCEPTION;
            if (e instanceof CodedException) {
                CodedException codeEx = (CodedException) e;
                errorCode = codeEx.getErrorCode();
            }
            if(errorCode >= 18000000 &&  errorCode < 19000000){
            	isError = true;
            }
            result = new Response();
            result.setErrorCode(errorCode);
            result.setErrorMessage(e.getMessage());

            logger.error("error when invoke", e);
            return;
        } finally {
            long endTime = TimeUtil.currentTimeMillis();
            VenusTracerUtil.logResult(endTime-startTime, traceId, apiName, JSON.toJSONString(parameterMap,JSON_FEATURE), JSON.toJSONString(result,JSON_FEATURE));
            writeResponse(req, resp, result);
            MonitorRuntime.getInstance().calculateAverage(service, method, endTime - startTime,isError);
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

    private void writeResponse(final HttpServletRequest req, // NOPMD by structchen on 13-10-18 上午11:17
            final HttpServletResponse resp, Response result) throws IOException {
        resp.setContentType(MediaTypes.APPLICATION_JSON);
        resp.getOutputStream().write(
                JSON.toJSONString(result, new SerializerFeature[] {SerializerFeature.PrettyFormat }).getBytes(
                        PacketConstant.PACKET_CHARSET));
    }

    private void writeResponse(final HttpServletRequest req, // NOPMD by structchen on 13-10-18 上午11:17
            final HttpServletResponse resp, String result) throws IOException {
        resp.setContentType(MediaTypes.APPLICATION_JSON);
        resp.getOutputStream().write(result.getBytes(PacketConstant.PACKET_CHARSET));
    }
    
    private Response handleRequest(String api, int version, String traceId, Map<String, Object> paramters) {

        Response response = new Response();
        VenusBIOConnection conn = null;
        try {
            UtilTimerStack.push(ENDPOINT_INVOKED_TIME);
            conn = (VenusBIOConnection) pool.borrowObject();
            JsonVenusRequestPacket request = new JsonVenusRequestPacket();
            request.apiName = api;
            request.params = JSON.toJSONString(paramters);
            request.serviceVersion = version;

            conn.write(request.toByteArray());

            byte[] bts = conn.read();

            int type = AbstractServicePacket.getType(bts);
            if (type == AbstractVenusPacket.PACKET_TYPE_ERROR) {
                ErrorPacket error = new ErrorPacket();
                error.init(bts);
                response.setErrorCode(error.errorCode);
                response.setErrorMessage(error.message);
            } else if (type == AbstractVenusPacket.PACKET_TYPE_NOTIFY_PUBLISH) {
                JsonVenusNotifyPacket notify = new JsonVenusNotifyPacket();
                notify.init(bts);
                response.setResult(notify.callbackObject);
            } else {
                JsonVenusResponsePacket responsePacket = new JsonVenusResponsePacket();
                responsePacket.init(bts);
                response.setResult(responsePacket.result);
            }

        } catch (Throwable e) {
        	RemoteException re = AnnotationUtil.getAnnotation(e.getClass().getAnnotations(), RemoteException.class);
        	if(re != null){
        		response.setErrorCode(re.errorCode());
        	}else{
        		response.setErrorCode(VenusExceptionCodeConstant.UNKNOW_EXCEPTION);
        	}
            response.setErrorMessage(e.getMessage());
            if(conn != null){
            	try {
					pool.invalidateObject(conn);
				} catch (Exception e1) {
				}
            }
            logger.error("handle handleRequest error",e);
            conn = null;
        } finally {
            if (conn != null) {
                try {
                    pool.returnObject(conn);
                } catch (Exception e) {
                }
            }
            UtilTimerStack.pop(ENDPOINT_INVOKED_TIME);
        }

        return response;
    }

    /**
     * extract request info from connection and packet
     * 
     * @param conn
     * @param packet
     * @return
     */
    private RequestInfo getRequestInfo(final HttpServletRequest req) {
        RequestInfo info = new RequestInfo();
        info.setRemoteIp(req.getRemoteHost());
        info.setProtocol(RequestInfo.Protocol.HTTP);
        info.setAccept(MediaTypes.APPLICATION_JSON);
        return info;
    }

}
