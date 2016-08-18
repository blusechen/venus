package com.meidusa.venus.backend.network.handler;

import com.meidusa.toolkit.common.bean.util.Initialisable;
import com.meidusa.toolkit.common.bean.util.InitialisationException;
import com.meidusa.toolkit.common.util.Tuple;
import com.meidusa.toolkit.net.MessageHandler;
import com.meidusa.venus.io.network.VenusFrontendConnection;

/**
 * singleton handler
 * @author structchen
 *
 */
@SuppressWarnings({ "unchecked", "rawtypes" })
public class ServiceInvokeProtectionMessageHandler implements MessageHandler<VenusFrontendConnection, Tuple<Long, byte[]>>, Initialisable {
    @Override
    public void init() throws InitialisationException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void handle(VenusFrontendConnection conn, Tuple<Long, byte[]> data) {
        throw new UnsupportedOperationException();
    }
    /*private static final String TIMEOUT = "The server is busy,waiting-timeout for execution";
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
    private static Logger logger = LoggerFactory.getLogger(ServiceInvokeProtectionMessageHandler.class);
    private static Logger INVOKER_LOGGER = LoggerFactory.getLogger("venus.service.invoker");
    private static Logger REPORT_LOGGER = LoggerFactory.getLogger("EndpointMonitor");
    private static Logger performanceLogger = LoggerFactory.getLogger("venus.backend.performance");

    private ThreadLocal<Executor> threadLocal = new ThreadLocal<Executor>() {
        public Executor initialValue() {
            Executor executor = null;
            if (executorEnabled) {
                if (maxExecutionThread > 0) {
                    int threadLocalExecutionThread = maxExecutionThread / Runtime.getRuntime().availableProcessors();
                    if (threadLocalExecutionThread == 0)
                        threadLocalExecutionThread = 1;
                    if (executorProtected) {
                        final DefaultQueueConfigManager manager = new DefaultQueueConfigManager(threadLocalExecutionThread);
                        final MultiBlockingQueue wrapper = new MultiBlockingQueue(manager);

                        MultiBlockingQueueExecutor multiBlockingQueueExecutor = new MultiBlockingQueueExecutor(threadLocalExecutionThread, threadLiveTime,
                                TimeUnit.MINUTES, wrapper, new CallerRunsPolicy());
                        manager.setExecutor(multiBlockingQueueExecutor);
                        manager.init();
                        executor = multiBlockingQueueExecutor;
                    } else {
                        executor = Executors.newFixedThreadPool(threadLocalExecutionThread);
                    }
                }
            }
            return executor;
        }
    };

    class DefaultQueueConfigManager extends DefaultMultiQueueManager {
        final List list = new ArrayList<Tuple<QueueConfig, Queue>>();
        int maxThread;
        private MultiBlockingQueueExecutor executor = null;

        public DefaultQueueConfigManager(int maxThread) {
            this.maxThread = maxThread;
        }

        public void setExecutor(MultiBlockingQueueExecutor executor) {
            this.executor = executor;
        }

        public Tuple<QueueConfig, Queue> newTuple(Named named) {
            Tuple<QueueConfig, Queue> tuple = super.newTuple(named);
            list.add(tuple);
            adjustMaxActive(tuple);
            return tuple;
        }

        public Queue createQueue(QueueConfig config) {
            return new LinkedBlockingQueue(config.getMaxQueue());
        }

        public QueueConfig getConfig(Named named) {
            QueueConfig config = new QueueConfig();
            config.setMaxQueue(10000);
            config.setName(named.getName());
            return config;
        }

        public int getIdleSize() {
            if (executor == null) {
                return 0;
            }
            return maxThread - executor.getRunningSize();
        }

        private void adjustMaxActive(Tuple<QueueConfig, Queue> tuple) {
            int maxActive = 0;
            if (tuple.left.getAverageLatencyTime() <= 10) {
                maxActive = (int) (0.9 * maxThread);
            } else if (tuple.left.getAverageLatencyTime() < 100) {
                maxActive = (int) (0.8 * maxThread);
            } else if (tuple.left.getAverageLatencyTime() <= 1000) {
                maxActive = (int) (0.5 * maxThread) + getIdleSize();
            } else if (tuple.left.getAverageLatencyTime() <= 5000) {
                maxActive = (int) (0.2 * maxThread) + getIdleSize();
            } else if (tuple.left.getAverageLatencyTime() <= 10000) {
                maxActive = (int) ((0.1 * maxThread) + (0.7 * getIdleSize()));
            } else {
                maxActive = (int) ((0.05 * maxThread) + (0.5 * getIdleSize()));
            }

            if (tuple.left.getMaxActive() > 0 && this.getIdleSize() <= 0.05 * maxThread && tuple.left.getRunningSize() >= 0.9 * tuple.left.getMaxActive()) {
                maxActive = (int) (maxActive - 0.1 * maxThread);
            }

            if (maxActive == 0) {
                maxActive = 1;
            } else if (maxActive >= maxThread) {
                maxActive = (int) (0.9 * maxThread);
                if (maxActive == 0) {
                    maxActive = 1;
                }
            }

            tuple.left.setMaxActive(maxActive);
        }

        public void init() {
            new Thread() {
                {
                    this.setDaemon(true);
                    this.setName("endPoint-Thread-adjust--" + Thread.currentThread().getName());
                }

                public void run() {
                    while (true) {
                        try {
                            Thread.sleep(5 * 1000L);
                        } catch (InterruptedException e) {
                        }
                        if (list.size() < 1)
                            continue;
                        List<Tuple<QueueConfig, Queue>> temp = new ArrayList<Tuple<QueueConfig, Queue>>();
                        temp.addAll(list);
                        REPORT_LOGGER.info("-----" + Thread.currentThread().getName() + ",total=" + maxThread + ",idle=" + getIdleSize() + "----------");
                        PriorityQueue<QueueConfig> queue = new PriorityQueue<QueueConfig>(temp.size(), new Comparator<QueueConfig>() {
                            @Override
                            public int compare(QueueConfig o1, QueueConfig o2) {
                                return (int) (o2.getAverageLatencyTime() - o1.getAverageLatencyTime());
                            }

                        });
                        for (Iterator<Tuple<QueueConfig, Queue>> it = temp.iterator(); it.hasNext();) {
                            Tuple<QueueConfig, Queue> tuple = it.next();
                            REPORT_LOGGER.info("name=" + tuple.left.getName() + ", runningThread=" + tuple.left.getRunningSize() + ", maxThread="
                                    + tuple.left.getMaxActive() + ", averageLatency=" + tuple.left.getAverageLatencyTime() + ", size=" + tuple.right.size());
                            adjustMaxActive(tuple);

                            if (tuple.left.getRunningSize() > 0 && tuple.left.getAverageLatencyTime() > 0) {
                                queue.add(tuple.left);
                            }
                        }

                        if (getIdleSize() < 0.1 * DefaultQueueConfigManager.this.maxThread) {
                            QueueConfig config = null;
                            int targetIdleSize = (int) (0.1 * DefaultQueueConfigManager.this.maxThread);
                            if (targetIdleSize == 0) {
                                targetIdleSize = 1;
                            }
                            while (queue.size() > 0 && (config = queue.remove()) != null && targetIdleSize > 0) {
                                if (config.getRunningSize() > targetIdleSize) {
                                    int thisDown = (int) (targetIdleSize / (queue.size() + 1));
                                    config.setMaxActive(config.getRunningSize() - thisDown);
                                    targetIdleSize = targetIdleSize - thisDown;
                                }
                            }
                        }

                    }
                }

            }.start();
        }
    }

    private int maxExecutionThread;
    private int threadLiveTime = 30;
    private boolean executorEnabled;
    private boolean executorProtected;
    private boolean useThreadLocalExecutor;

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

    private Executor executor;

    private VenusExceptionFactory venusExceptionFactory;

    @Autowired
    private ServiceManager serviceManager;

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
    public void handle(final VenusFrontendConnection conn, Tuple<Long, byte[]> data) {
        long waitTime = TimeUtil.currentTimeMillis() - data.left;

        byte[] message = data.right;
        final long received = TimeUtil.currentTimeMillis();

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

                response.status = VenusStatus.getInstance().getStatus();
                postMessageBack(conn, null, sr, response);

                break;
            case PacketConstant.PACKET_TYPE_SERVICE_REQUEST:
                SerializeServiceRequestPacket request = null;
                try {
                    ServiceAPIPacket apiPacket = new ServiceAPIPacket();
                    try {
                        ServicePacketBuffer packetBuffer = new ServicePacketBuffer(message);
                        apiPacket.init(packetBuffer);

                        Endpoint ep = getServiceManager().getEndpoint(apiPacket.apiName);

                        Serializer serializer = SerializerFactory.getSerializer(packetSerializeType);
                        request = new SerializeServiceRequestPacket(serializer, ep.getParameterTypeDict());

                        packetBuffer.setPosition(0);
                        request.init(packetBuffer);

                    } catch (Exception e) {
                        ErrorPacket error = new ErrorPacket();
                        AbstractServicePacket.copyHead(apiPacket, error);
                        if (e instanceof CodedException) {
                            CodedException codeEx = (CodedException) e;
                            error.errorCode = codeEx.getErrorCode();
                        } else {
                            if (e instanceof JSONException || e instanceof SerializeException) {
                                error.errorCode = VenusExceptionCodeConstant.REQUEST_ILLEGAL;
                            } else {
                                error.errorCode = VenusExceptionCodeConstant.UNKNOW_EXCEPTION;
                            }
                        }
                        error.message = e.getMessage();
                        postMessageBack(conn, routerPacket, request, error);

                        if (e instanceof VenusExceptionLevel) {
                            if (((VenusExceptionLevel) e).getLevel() != null) {
                                LogHandler.logDependsOnLevel(((VenusExceptionLevel) e).getLevel(), logger, e.getMessage() + " client:{clientID=" + apiPacket.clientId
                                        + ",ip=" + conn.getHost() + ":" + conn.getPort() + ",sourceIP=" + finalSourceIp + ", apiName=" + apiPacket.apiName
                                        + "}", e);
                            }
                        } else {
                            if (logger.isDebugEnabled()) {
                                logger.debug(e.getMessage() + " [ip=" + conn.getHost() + ":" + conn.getPort() + ",sourceIP=" + finalSourceIp + ", apiName="
                                        + apiPacket.apiName + "]", e);
                            }
                        }
                        return;
                    }

                    final String apiName = request.apiName;
                    int index = apiName.lastIndexOf(".");
                    String serviceName = request.apiName.substring(0, index);
                    String methodName = request.apiName.substring(index + 1);
                    // RequestInfo info = getRequestInfo(conn, request);

                    final Endpoint endpoint = getServiceManager().getEndpoint(serviceName, methodName, request.parameterMap.keySet().toArray(new String[] {}));

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

                    *//**
                     * 判断超时
                     *//*
                    if (errorPacket == null) {
                        errorPacket = checkTimeout(endpoint, request, waitTime);
                    }

                    if (errorPacket != null) {
                        if (resultType == ResultType.NOTIFY) {
                            if (invocationListener != null) {
                                invocationListener.onException(new ServiceVersionNotAllowException(errorPacket.message));
                            } else {
                                postMessageBack(conn, routerPacket, request, errorPacket);
                            }
                        } else {
                            postMessageBack(conn, routerPacket, request, errorPacket);
                        }

                        if (logger.isDebugEnabled()) {
                            logger.debug("receive service request packet from " + conn.getHost() + ":" + conn.getPort());
                            logger.debug("sending response to ip=" + conn.getHost() + ":" + conn.getPort() + ",sourceIP=" + finalSourceIp + ": " + errorPacket
                                    + " ");
                        }
                        return;
                    }

                    final Map<String, Object> paramters = request.parameterMap;// convertService.convert(request.parameterMap,
                                                                               // endpoint.getParameterTypeDict());
                    final AbstractServiceRequestPacket requestFinal = request;
                    final ResultType resultTypeFinal = resultType;
                    final RemotingInvocationListener<Serializable> invocationListenerFinal = invocationListener;
                    final SerializeServiceRequestPacket requestPacket = request;
                    final VenusRouterPacket finalRouterPacket = routerPacket;
                    final byte[] traceID = request.traceId;
                    class ServiceRunnable extends MultiQueueRunnable {
                        @Override
                        public void doRun() {
                            long startRunTime = TimeUtil.currentTimeMillis();
                            Response result = null;
                            if (conn.isClosed() && resultTypeFinal == ResultType.RESPONSE) {
                                return;
                            }
                            try {
                                RequestHandler requestHandler = new RequestHandler();

                                RequestInfo requestInfo = requestHandler.getRequestInfo(packetSerializeType, conn, routerPacket);
                                RequestContext context = requestHandler.createContext(requestInfo, endpoint, request);
                                ThreadLocalMap.put(VenusTracerUtil.REQUEST_TRACE_ID, traceID);
                                ThreadLocalMap.put(ThreadLocalConstant.REQUEST_CONTEXT, context);
                                // invoke service endpoint
                                result = handleRequest(context, conn, endpoint);

                                if (result.getErrorCode() == 0) {
                                    if (resultTypeFinal == ResultType.RESPONSE) {
                                        Serializer serializer = SerializerFactory.getSerializer(packetSerializeType);
                                        ServiceResponsePacket response = new SerializeServiceResponsePacket(serializer, endpoint.getMethod()
                                                .getGenericReturnType());
                                        AbstractServicePacket.copyHead(requestFinal, response);
                                        response.result = result.getResult();
                                        postMessageBack(conn, finalRouterPacket, requestFinal, response);
                                    } else if (resultTypeFinal == ResultType.OK) {
                                        OKPacket ok = new OKPacket();
                                        AbstractServicePacket.copyHead(requestFinal, ok);
                                        postMessageBack(conn, finalRouterPacket, requestFinal, ok);
                                    } else if (resultTypeFinal == ResultType.NOTIFY) {
                                        if (invocationListenerFinal != null && !invocationListenerFinal.isResponsed()) {
                                            invocationListenerFinal.onException(new ServiceNotCallbackException("Server side not call back error"));
                                        }
                                    }
                                } else {
                                    if (resultTypeFinal == ResultType.RESPONSE || resultTypeFinal == ResultType.OK) {
                                        ErrorPacket error = new ErrorPacket();
                                        AbstractServicePacket.copyHead(requestFinal, error);
                                        error.errorCode = result.getErrorCode();
                                        error.message = result.getErrorMessage();
                                        Throwable throwable = result.getException();
                                        if (throwable != null) {
                                            Serializer serializer = SerializerFactory.getSerializer(packetSerializeType);
                                            Map<String, PropertyDescriptor> mpd = Utils.getBeanPropertyDescriptor(throwable.getClass());
                                            Map<String, Object> additionalData = new HashMap<String, Object>();

                                            for (Map.Entry<String, PropertyDescriptor> entry : mpd.entrySet()) {
                                                additionalData.put(entry.getKey(), entry.getValue().getReadMethod().invoke(throwable));
                                            }
                                            error.additionalData = serializer.encode(additionalData);
                                        }

                                        postMessageBack(conn, finalRouterPacket, requestFinal, error);
                                    } else if (resultTypeFinal == ResultType.NOTIFY) {
                                        if (invocationListenerFinal != null && !invocationListenerFinal.isResponsed()) {
                                            if (result.getException() == null) {
                                                invocationListenerFinal.onException(new DefaultVenusException(result.getErrorCode(), result.getErrorMessage()));
                                            } else {
                                                invocationListenerFinal.onException(result.getException());
                                            }
                                        }
                                    }
                                }

                                if (logger.isDebugEnabled()) {
                                    logger.debug("receive service request packet from " + conn.getHost() + ":" + conn.getPort());
                                    logger.debug("sending response to ip=" + conn.getHost() + ":" + conn.getPort() + ",sourceIP=" + finalSourceIp + ": "
                                            + result + " ");
                                }
                            } catch (Exception e) {
                                ErrorPacket error = new ErrorPacket();
                                AbstractServicePacket.copyHead(requestFinal, error);
                                Integer code = codeMap.get(e.getClass());
                                if(code != null){
                                    error.errorCode = code;
                                }else{
                                    if (e instanceof CodedException) {
                                        CodedException codeEx = (CodedException) e;
                                        error.errorCode = codeEx.getErrorCode();
                                        if (logger.isDebugEnabled()) {
                                            logger.debug("error when invoke", e);
                                        }
                                    } else {
                                        try {
                                            Method method = e.getClass().getMethod("getErrorCode", new Class[] {});
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
                                error.message = e.getMessage();
                                postMessageBack(conn, finalRouterPacket, requestFinal, error);

                                return;
                            }catch(OutOfMemoryError e){
                            	ErrorPacket error = new ErrorPacket();
                                AbstractServicePacket.copyHead(requestFinal, error);
                                error.errorCode = VenusExceptionCodeConstant.SERVICE_UNAVAILABLE_EXCEPTION;
                                error.message = e.getMessage();
                                postMessageBack(conn, finalRouterPacket, requestFinal, error);
                                VenusStatus.getInstance().setStatus(PacketConstant.VENUS_STATUS_OUT_OF_MEMORY);
                                logger.error("error when invoke", e);
                            	throw e;
                            }catch (Error e) {
                                ErrorPacket error = new ErrorPacket();
                                AbstractServicePacket.copyHead(requestFinal, error);
                                error.errorCode = VenusExceptionCodeConstant.SERVICE_UNAVAILABLE_EXCEPTION;
                                error.message = e.getMessage();
                                postMessageBack(conn, finalRouterPacket, requestFinal, error);
                                logger.error("error when invoke", e);
                                return;
                            } finally {
                                long endRunTime = TimeUtil.currentTimeMillis();

                                long queuedTime = startRunTime - received;
                                long executTime = endRunTime - startRunTime;
                                MonitorRuntime.getInstance().calculateAverage(endpoint.getService().getName(), endpoint.getName(), executTime,false);

                                StringBuffer buffer = new StringBuffer();
                                buffer.append("[").append(queuedTime).append(",").append(executTime).append("]ms api=").append(apiName).append(", ip=")
                                        .append(conn.getHost() + ":" + conn.getPort()).append(", sourceIP=").append(finalSourceIp).append(", clientID=")
                                        .append(requestPacket.clientId).append(", requestID=").append(requestPacket.clientRequestId);

                                PerformanceLogger pLevel = endpoint.getPerformanceLogger();
                                if (pLevel != null) {

                                    if (pLevel.isPrintParams()) {
                                        buffer.append(", params=\n");
                                        buffer.append(JSON.toJSONString(requestPacket.parameterMap));
                                    }
                                    if (pLevel.isPrintResult()) {
                                        if (result == null) {
                                            buffer.append(", result=<null>");
                                        } else {
                                            buffer.append(", result=\n");
                                            buffer.append(JSON.toJSONString(result));
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
                                    if (queuedTime >= 30 * 1000 || executTime >= 30 * 1000 || queuedTime + executTime >= 30 * 1000) {
                                        if (performanceLogger.isErrorEnabled()) {
                                            performanceLogger.error(buffer.toString());
                                        }
                                    } else if (queuedTime >= 10 * 1000 || executTime >= 10 * 1000 || queuedTime + executTime >= 10 * 1000) {
                                        if (performanceLogger.isWarnEnabled()) {
                                            performanceLogger.warn(buffer.toString());
                                        }
                                    } else if (queuedTime >= 5 * 1000 || executTime >= 5 * 1000 || queuedTime + executTime >= 5 * 1000) {
                                        if (performanceLogger.isInfoEnabled()) {
                                            performanceLogger.info(buffer.toString());
                                        }
                                    } else {
                                        if (performanceLogger.isDebugEnabled()) {
                                            performanceLogger.debug(buffer.toString());
                                        }
                                    }
                                }
                                ThreadLocalMap.remove(ThreadLocalConstant.REQUEST_CONTEXT);
                                ThreadLocalMap.remove(VenusTracerUtil.REQUEST_TRACE_ID);
                            }

                        }

                        @Override
                        public String getName() {
                            return apiName;
                        }
                    }

                    if (useThreadLocalExecutor) {
                        Executor executor = threadLocal.get();
                        if (executor == null) {
                            new ServiceRunnable().run();
                        } else {
                            executor.execute(new ServiceRunnable());
                        }
                    } else {
                        if (executor == null) {
                            new ServiceRunnable().run();
                        } else {
                            executor.execute(new ServiceRunnable());
                        }
                    }

                } catch (Exception e) {
                    ErrorPacket error = new ErrorPacket();
                    AbstractServicePacket.copyHead(request, error);
                    error.errorCode = VenusExceptionCodeConstant.SERVICE_UNAVAILABLE_EXCEPTION;
                    error.message = e.getMessage();
                    postMessageBack(conn, routerPacket, request, error);
                    logger.error("error when invoke", e);
                    return;
                } catch (Error e) {
                    ErrorPacket error = new ErrorPacket();
                    AbstractServicePacket.copyHead(request, error);
                    error.errorCode = VenusExceptionCodeConstant.SERVICE_UNAVAILABLE_EXCEPTION;
                    error.message = e.getMessage();
                    postMessageBack(conn, routerPacket, request, error);
                    logger.error("error when invoke", e);
                    return;
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


    private static ErrorPacket checkTimeout(Endpoint endpoint, AbstractServiceRequestPacket request, long waitTime) {
        if (waitTime > endpoint.getTimeWait()) {
            ErrorPacket error = new ErrorPacket();
            AbstractServicePacket.copyHead(request, error);
            error.errorCode = VenusExceptionCodeConstant.INVOCATION_ABORT_WAIT_TIMEOUT;
            error.message = TIMEOUT;
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

    protected RequestContext createContext(RequestInfo info, Connection conn, Endpoint endpoint, Map<String, Object> paramters) {
        RequestContext context = new RequestContext();
        context.setParameters(paramters);
        context.setEndPointer(endpoint);
        context.setRequestInfo(info);
        return context;
    }

    private Response handleRequest(RequestContext context, Connection conn, Endpoint endpoint) {

        Response response = new Response();

        DefaultEndpointInvocation invocation = new DefaultEndpointInvocation(context, endpoint);

        try {
            UtilTimerStack.push(ENDPOINT_INVOKED_TIME);
            response.setResult(invocation.invoke());
        } catch (Throwable e) {
            if (e instanceof ServiceInvokeException) {
                e = ((ServiceInvokeException) e).getTargetException();
            }
            if (e instanceof Exception) {
                response.setException((Exception) e);
            } else {
                response.setException(new DefaultVenusException(e.getMessage(), e));
            }

            Integer code = codeMap.get(e.getClass());

            if(code != null){
                response.setErrorCode(code);
                response.setErrorMessage(e.getMessage());
            }else{
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

                    if (e instanceof NullPointerException && e.getMessage() == null) {
                        response.setErrorMessage("Server Side error caused by NullPointerException");
                    } else {
                        response.setErrorMessage(e.getMessage());
                    }
                }
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

    *//**
     * extract request info from connection and packet
     *
     * @return
     *//*


    @Override
    public void init() throws InitialisationException {
        if (executor == null && executorEnabled && !useThreadLocalExecutor && maxExecutionThread > 0) {
            if (this.executorProtected) {
                MultiBlockingQueueExecutor multiBlockingQueueExecutor = null;
                final DefaultQueueConfigManager manager = new DefaultQueueConfigManager(this.maxExecutionThread);
                MultiBlockingQueue blockingQueue = new MultiBlockingQueue(manager);
                executor = multiBlockingQueueExecutor = new MultiBlockingQueueExecutor(maxExecutionThread, threadLiveTime, TimeUnit.MINUTES, blockingQueue,
                        new CallerRunsPolicy());
                manager.executor = multiBlockingQueueExecutor;
                manager.init();
            } else {
                executor = Executors.newFixedThreadPool(maxExecutionThread);
            }
        }
    }

    public void postMessageBack(Connection conn, VenusRouterPacket routerPacket, AbstractServicePacket source, AbstractServicePacket result) {
        if (routerPacket == null) {
            conn.write(result.toByteBuffer());
        } else {
            routerPacket.data = result.toByteArray();
            conn.write(routerPacket.toByteBuffer());
        }
    }*/
}
