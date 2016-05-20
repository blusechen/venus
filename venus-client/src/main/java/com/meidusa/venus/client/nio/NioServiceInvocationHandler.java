package com.meidusa.venus.client.nio;

import com.meidusa.toolkit.net.*;
import com.meidusa.toolkit.util.StringUtil;
import com.meidusa.toolkit.util.TimeUtil;
import com.meidusa.venus.annotations.Endpoint;
import com.meidusa.venus.annotations.Service;
import com.meidusa.venus.client.VenusInvocationHandler;
import com.meidusa.venus.client.VenusNIOMessageHandler;
import com.meidusa.venus.client.nio.config.RemoteServer;
import com.meidusa.venus.client.nio.config.ServiceConfig;
import com.meidusa.venus.exception.DefaultVenusException;
import com.meidusa.venus.io.network.VenusBackendConnectionFactory;
import com.meidusa.venus.io.packet.PacketConstant;
import com.meidusa.venus.io.packet.serialize.SerializeServiceRequestPacket;
import com.meidusa.venus.io.serializer.Serializer;
import com.meidusa.venus.io.serializer.SerializerFactory;
import com.meidusa.venus.metainfo.EndpointParameter;
import com.meidusa.venus.notify.InvocationListener;
import com.meidusa.venus.util.VenusAnnotationUtils;
import com.meidusa.venus.util.VenusTracerUtil;
import org.springframework.util.Assert;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by huawei on 5/18/16.
 */
public class NioServiceInvocationHandler extends VenusInvocationHandler implements Runnable {

    private static ExecutorService es = Executors.newCachedThreadPool();

    private RegistryManager rm;
    private ServiceConfig config;

    private BackendConnectionPool pool;
    private List<String> currentRemoteAddress;

    private ConnectionConnector connector;
    private ConnectionManager manager;

    private VenusNIOMessageHandler handler = new VenusNIOMessageHandler();

    private static AtomicLong sequenceId = new AtomicLong(1);

    public NioServiceInvocationHandler(RegistryManager rm, ServiceConfig config, ConnectionConnector connector) {
        Assert.notNull(rm, "注册中心管理器不能为空");
        Assert.notNull(config, "服务配置不能为空");
        Assert.notNull(connector, "连接管理器不能为空");
        this.rm = rm;
        this.config = config;
        this.connector = connector;

    }

    public NioServiceInvocationHandler init() throws Exception {
        List<RemoteServer> servers;
        try{
            if (!config.isOverride()) {
                List<String> remoteAddresses = rm.getRemote(config.getServiceName(), config.getVersion());
                if (remoteAddresses == null || remoteAddresses.size() <= 0) {
                    throw new DefaultVenusException(0, "未知远程服务地址");
                }
                currentRemoteAddress = remoteAddresses;

                BackendConnectionPool nioPools[] = new BackendConnectionPool[remoteAddresses.size()];

                for(int i = 0;i < remoteAddresses.size(); i++) {
                    VenusBackendConnectionFactory nioFactory = new VenusBackendConnectionFactory();
                    nioFactory.setAuthenticator(config.getAuthenticator());
                    String temp[] = StringUtil.split(remoteAddresses.get(i), ":");
                    nioFactory.setHost(temp[0]);
                    if (temp.length > 1) {
                        nioFactory.setPort(Integer.parseInt(temp[1]));
                    }else {
                        nioFactory.setPort(16800);
                    }
                    nioFactory.setConnector(connector);
                    nioFactory.setMessageHandler(handler);

                    nioPools[i] = new PollingBackendConnectionPool("N-" + remoteAddresses.get(i) , nioFactory, 8);
                    nioPools[i].init();
                }

                if (remoteAddresses.size() == 1) {
                    pool = nioPools[0];
                }else {
                    MultipleLoadBalanceBackendConnectionPool mlbbcp = new MultipleLoadBalanceBackendConnectionPool("", 1, nioPools);
                    pool = mlbbcp;
                }

            }else {
                servers = config.getServers();
            }
        }catch (Exception e) {
            e.printStackTrace();
        }

        return this;
    }

    @Override
    public void run() {
        if (!config.isOverride()) {

        }
    }

    @Override
    protected Object invokeRemoteService(Service service, Endpoint endpoint, Method method, EndpointParameter[] params, Object[] args) throws Exception {

        byte[] traceID = VenusTracerUtil.getTracerID();

        if (traceID == null) {
            traceID = VenusTracerUtil.randomTracerID();
        }

        SerializeServiceRequestPacket serviceRequestPacket = null;

        Serializer serializer = SerializerFactory.getSerializer(config.getAuthenticator().getSerializeType());
        serviceRequestPacket = new SerializeServiceRequestPacket(serializer, null);
        serviceRequestPacket.clientId = PacketConstant.VENUS_CLIENT_ID;
        serviceRequestPacket.clientRequestId = sequenceId.getAndIncrement();
        serviceRequestPacket.traceId = traceID;
        serviceRequestPacket.apiName = VenusAnnotationUtils.getApiname(method, service, endpoint);
        serviceRequestPacket.serviceVersion = service.version();
        serviceRequestPacket.parameterMap = new HashMap<String, Object>();

        if (params != null) {
            for (int i = 0; i < params.length; i++) {
                if (args[i] instanceof InvocationListener) {

                } else {
                    serviceRequestPacket.parameterMap.put(params[i].getParamName(), args[i]);
                }

            }
        }

        long start = TimeUtil.currentTimeMillis();
        long borrowed = start;

        BackendConnection conn = null;

        NioPacketWaitTask task = new NioPacketWaitTask();


        if (method.getGenericReturnType() != Void.class) {
            task.setExpireTime(30);
            task.setReturnType(method.getGenericReturnType());
            NioInvocationContainer.getInstance().put(serviceRequestPacket.clientRequestId, task);
        }

        BackendConnectionPool currentPool = pool;
        try{

            conn = currentPool.borrowObject();
            conn.write(serviceRequestPacket.toByteBuffer());
        }catch (Exception e) {

        }finally {
            if(conn != null) {
                currentPool.returnObject(conn);
            }
        }

        if (method.getGenericReturnType() == Void.class) {
            return null;
        }

        Future future = es.submit(new NioCallable(task));

        long time = System.currentTimeMillis();
        try{
            Object result =  future.get(30*1000, TimeUnit.SECONDS);
            return result;
        }catch (Exception e) {

        }

        if (task.getResult() == null) {
            throw new Exception("server execute time is too looooooooooooooooooooooog");
        }

        return task.getResult();
    }
}

class NioCallable implements Callable {

    private NioPacketWaitTask task;

    public NioCallable(NioPacketWaitTask task) {
        this.task = task;
    }

    @Override
    public Object call() throws Exception {
        while(true) {
            if(task.isComplete()) {
                break;
            }

            if (task.isExpire()) {
                break;
            }
        }
        return task.getResult();
    }
}
