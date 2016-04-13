package com.meidusa.venus.backend;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.meidusa.venus.io.packet.PacketConstant;

public class ShutdownListener extends Thread {
    private static final Logger logger = LoggerFactory.getLogger(ShutdownListener.class);

    public void run() {
        logger.warn("venus status changed to VENUS_STATUS_SHUTDOWN...");
        VenusStatus.getInstance().setStatus(PacketConstant.VENUS_STATUS_SHUTDOWN);

        try {
            Thread.sleep(10 * 1000L);
        } catch (InterruptedException e) {
        }
        logger.warn("ShutdownListener invoked completed!");
    }
    
}
