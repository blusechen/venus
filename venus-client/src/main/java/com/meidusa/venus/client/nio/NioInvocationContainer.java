package com.meidusa.venus.client.nio;

import com.meidusa.toolkit.common.runtime.GlobalScheduler;
import com.meidusa.venus.io.packet.serialize.SerializeServiceRequestPacket;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Created by huawei on 5/20/16.
 */
public class NioInvocationContainer {

    private static GlobalScheduler scheduler = GlobalScheduler.getInstance();
    private static NioInvocationContainer instance = new NioInvocationContainer();

    static {
        scheduler.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                try{
                    synchronized (container) {
                        Iterator<Long> iterator = container.keySet().iterator();
                        while(iterator.hasNext()) {
                            Long clientRequestId = iterator.next();
                            NioPacketWaitTask task = container.get(clientRequestId);
                            if (task.isExpire()) {
                                iterator.remove();
                            }
                        }
                    }
                }catch (Exception e) {

                }
            }
        }, 0, 10, TimeUnit.MINUTES);
    }

    private NioInvocationContainer(){

    }

    public static NioInvocationContainer getInstance(){
        return instance;
    }

    private static Map<Long, NioPacketWaitTask> container= new HashMap<Long, NioPacketWaitTask>();

    public void put(Long id, NioPacketWaitTask packet) {
        container.put(id, packet);
    }

    public NioPacketWaitTask get(Long id) {
        return container.remove(id);
    }

}
