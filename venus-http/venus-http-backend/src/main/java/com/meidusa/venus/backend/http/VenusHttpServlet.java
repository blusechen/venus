package com.meidusa.venus.backend.http;

import com.meidusa.fastjson.JSONException;
import com.meidusa.toolkit.util.TimeUtil;
import com.meidusa.venus.backend.DefaultEndpointInvocation;
import com.meidusa.venus.backend.RequestInfo;
import com.meidusa.venus.backend.Response;
import com.meidusa.venus.backend.context.RequestContext;
import com.meidusa.venus.backend.network.handler.LogHandler;
import com.meidusa.venus.backend.profiling.UtilTimerStack;
import com.meidusa.venus.backend.services.Endpoint;
import com.meidusa.venus.backend.services.Service;
import com.meidusa.venus.backend.services.ServiceManager;
import com.meidusa.venus.backend.view.MediaTypes;
import com.meidusa.venus.convert.ConvertService;
import com.meidusa.venus.convert.DefaultConvertService;
import com.meidusa.venus.exception.*;
import com.meidusa.venus.io.packet.PacketConstant;
import com.meidusa.venus.io.serializer.Serializer;
import com.meidusa.venus.io.serializer.SerializerFactory;
import com.meidusa.venus.service.monitor.MonitorRuntime;
import com.meidusa.venus.util.Range;
import com.meidusa.venus.util.Utils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;
import org.springframework.web.servlet.FrameworkServlet;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class VenusHttpServlet extends HttpServlet {
    private static Logger logger = LoggerFactory.getLogger(VenusHttpServlet.class);
    private static String ENDPOINT_INVOKED_TIME = "invoked Total Time: ";
    // private static String patternString = "([a-zA-Z_0-9.]+)/([a-zA-Z_0-9]+)(?:/(\\d+))*(?:/\\S*)*(?:\\?(?:.*?)*)*";
    private transient ServiceManager serviceManager;
    private transient VenusExceptionFactory venusExceptionFactory;
    private transient Pattern servicePattern = null;
    private transient Serializer serializer = SerializerFactory.getSerializer(PacketConstant.CONTENT_TYPE_JSON);
    private transient ConvertService convertService = new DefaultConvertService();
    private String urlPattern;
    private static final long serialVersionUID = 1L;
    private String springServletName;
    
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        String servletName = config.getServletName();

        urlPattern = config.getInitParameter("uri-pattern");

        if (StringUtils.isEmpty(urlPattern)) {
            throw new ServletException("servlet=" + servletName + "  init-param=uri-prefix cannot be null");
        }

        urlPattern = urlPattern.trim();
        springServletName = config.getInitParameter("spring-servlet-name");
        /*
         * if(urlPattern.endsWith("*")){ urlPattern = urlPattern.substring(0,urlPattern.length()-1); }
         * if(urlPattern.endsWith("/")){ urlPattern = urlPattern.substring(0,urlPattern.length()-1); }
         */

        servicePattern = Pattern.compile(urlPattern);
        ApplicationContext context = null;
        if(springServletName != null){
            String name = FrameworkServlet.SERVLET_CONTEXT_PREFIX+springServletName;
            WebApplicationContext wac = WebApplicationContextUtils.getWebApplicationContext(getServletContext(), name);
            if(wac != null){
                context = wac;
            }
        }else{
            context = (ApplicationContext) config.getServletContext().getAttribute(WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE);
        }
        
        serviceManager = context.getBean(ServiceManager.class);
        try {
            venusExceptionFactory = context.getBean(VenusExceptionFactory.class);
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
        String clientID = req.getHeader("_venus_client_id_");
        String apiName = service + "." + method;
        Endpoint endpoint = null;
        // ResultType resultType = ResultType.RESPONSE;
        String v = req.getParameter("v");
        // String sign = req.getParameter("sign");
        int version = 0;

        try {
            version = Integer.parseInt(v == null ? "0" : v);
        } catch (java.lang.NumberFormatException e) {
            version = 0;
        }

        // check endpoint
        try {

            endpoint = serviceManager.getEndpoint(apiName);

            if (req.getContentLength() > 0) {
                byte[] message = new byte[req.getContentLength()];
                req.getInputStream().read(message);
                parameterMap = serializer.decode(message, endpoint.getParameterTypeDict());
            } else {
                parameterMap = new HashMap<String, Object>();
            }

            Set<String> keys = req.getParameterMap().keySet();
            for (String key : keys) {
                Type type = endpoint.getParameterTypeDict().get(key);
                if (type != null) {
                    parameterMap.put(key, convertService.convert(req.getParameter(key), type));
                } else {
                    parameterMap.put(key, req.getParameter(key));
                }
            }

            /*
             * if (endpoint.isVoid()) { resultType = ResultType.OK; if (endpoint.isAsync()) { resultType =
             * ResultType.NONE; } for (Class clazz : endpoint.getMethod().getParameterTypes()) { if
             * (InvocationListener.class.isAssignableFrom(clazz)) { resultType = ResultType.NOTIFY; break; } } }
             */
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
                    LogHandler.logDependsOnLevel(((VenusExceptionLevel) e).getLevel(), logger,
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

            // invoke service
            if ((result = checkActive(endpoint)) != null || (result = checkVersion(endpoint, version)) != null) {
                return;
            }

            // invoke service endpoint
            result = handleRequest(getRequestInfo(req), endpoint, parameterMap);

            if (logger.isDebugEnabled()) {
                logger.debug("receive service request packet from " + req.getRemoteAddr());
                logger.debug("sending response to " + req.getRemoteAddr() + ": " + result + " ");
            }
        } catch (Exception e) {
            int errorCode = VenusExceptionCodeConstant.UNKNOW_EXCEPTION;
            if (e instanceof CodedException) {
                CodedException codeEx = (CodedException) e;
                errorCode = codeEx.getErrorCode();
            }

            result = new Response();
            result.setErrorCode(errorCode);
            result.setErrorMessage(e.getMessage());
            
            if(errorCode >= 18000000 &&  errorCode < 19000000){
            	isError = true;
            }
            logger.error("error when invoke", e);
            return;
        } finally {
            long endTime = TimeUtil.currentTimeMillis();

            writeResponse(req, resp, result);
            MonitorRuntime.getInstance().calculateAverage(service, method, endTime - startTime,isError);
        }

    }



    private static Response checkActive(Endpoint endpoint) {
        Service service = endpoint.getService();
        if (!service.isActive() || !endpoint.isActive()) {
            Response result = new Response();
            result.setErrorCode(VenusExceptionCodeConstant.SERVICE_INACTIVE_EXCEPTION);
            StringBuffer buffer = new StringBuffer();
            buffer.append("Service=").append(endpoint.getService().getName());
            if (!service.isActive()) {
                buffer.append(" is not active");
            }

            if (!endpoint.isActive()) {
                buffer.append(", endpoint=").append(endpoint.getName()).append(" is not active");
            }
            result.setErrorMessage(buffer.toString());
            return result;
        }

        return null;
    }

    private static Response checkVersion(Endpoint endpoint, int version) {
        Service service = endpoint.getService();

        // service version check
        Range range = service.getVersionRange();
        if (version <= 0 || range == null || range.contains(version)) {
            return null;
        } else {
            Response result = new Response();
            result.setErrorCode(VenusExceptionCodeConstant.SERVICE_VERSION_NOT_ALLOWD_EXCEPTION);
            result.setErrorMessage("Service=" + endpoint.getService().getName() + ",version=" + version + " not allow");
            return result;
        }
    }

    private void writeResponse(final HttpServletRequest req, // NOPMD by structchen on 13-10-18 上午11:17
            final HttpServletResponse resp, Response result) throws IOException {
        resp.setContentType(MediaTypes.APPLICATION_JSON);
        resp.getOutputStream().write(serializer.encode(result));
    }

    private Response handleRequest(RequestInfo info, Endpoint endpoint, Map<String, Object> paramters) {

        RequestContext context = new RequestContext();
        context.setParameters(paramters);
        context.setEndPointer(endpoint);
        context.setRequestInfo(info);
        Response response = new Response();

        DefaultEndpointInvocation invocation = new DefaultEndpointInvocation(context, endpoint);

        try {
            UtilTimerStack.push(ENDPOINT_INVOKED_TIME);
            response.setResult(invocation.invoke());
        } catch (Throwable e) {
            if (e instanceof ServiceInvokeException) {
                e = ((ServiceInvokeException) e).getTargetException();
            }

            if (e instanceof CodedException) {
                response.setErrorCode(((CodedException) e).getErrorCode());
                response.setErrorMessage(((CodedException) e).getMessage());
            } else {
                int errorCode = 0;
                if (venusExceptionFactory != null) {
                    errorCode = venusExceptionFactory.getErrorCode(e.getClass());
                    if (errorCode != 0) {
                        response.setErrorCode(errorCode);
                    } else {
                        // unknowable exception
                        response.setErrorCode(VenusExceptionCodeConstant.UNKNOW_EXCEPTION);
                    }
                } else {
                    // unknowable exception
                    response.setErrorCode(VenusExceptionCodeConstant.UNKNOW_EXCEPTION);
                }
                response.setErrorMessage(e.getMessage());
            }
            Service service = endpoint.getService();
            if (e instanceof VenusExceptionLevel) {
                if (((VenusExceptionLevel) e).getLevel() != null) {
                    LogHandler.logDependsOnLevel(((VenusExceptionLevel) e).getLevel(), logger, e.getMessage() + ",ip=" + context.getRequestInfo().getRemoteIp() + " ,api="
                            + service.getName() + "." + endpoint.getMethod().getName() + " , params=" + Utils.toString(context.getParameters()), e);
                }
            } else {
                if (e instanceof RuntimeException && !(e instanceof CodedException)) {
                    logger.error(e.getMessage() + ",ip=" + context.getRequestInfo().getRemoteIp() + " ,api=" + service.getName() + "."
                            + endpoint.getMethod().getName() + " , params=" + Utils.toString(context.getParameters()), e);
                } else {
                    if (logger.isDebugEnabled()) {
                        logger.debug(e.getMessage() + ",ip=" + context.getRequestInfo().getRemoteIp() + " ,api=" + service.getName() + "."
                                + endpoint.getMethod().getName() + " , params=" + Utils.toString(context.getParameters()), e);
                    }
                }
            }
        } finally {
            UtilTimerStack.pop(ENDPOINT_INVOKED_TIME);
        }

        return response;
    }

    /**
     * extract request info from connection and packet
     * 
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
