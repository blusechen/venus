/*
 * Copyright 2008-2108 amoeba.meidusa.com 
 * 
 * 	This program is free software; you can redistribute it and/or modify it under the terms of 
 * the GNU AFFERO GENERAL PUBLIC LICENSE as published by the Free Software Foundation; either version 3 of the License, 
 * or (at your option) any later version. 
 * 
 * 	This program is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * See the GNU AFFERO GENERAL PUBLIC LICENSE for more details. 
 * 	You should have received a copy of the GNU AFFERO GENERAL PUBLIC LICENSE along with this program; 
 * if not, write to the Free Software Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package com.meidusa.venus.benchmark;

import java.nio.ByteBuffer;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

import com.meidusa.toolkit.net.AuthingableBackendConnection;
import com.meidusa.toolkit.net.BackendConnection;
import com.meidusa.toolkit.net.Connection;
import com.meidusa.toolkit.net.MessageHandler;
import com.meidusa.toolkit.net.packet.Packet;
import com.meidusa.toolkit.util.TimeUtil;

public abstract class AbstractBenchmarkClient<T extends Packet, V extends BenchmarkContext> implements MessageHandler<Connection, byte[]>, Delayed {
    private long lastSentTime = TimeUtil.currentTimeMillis();
    private boolean debug = false;
    private boolean showError;
    private int timeout = -1;
    private Properties properties;
    long min = System.nanoTime();
    long start = 0;
    long max = 0;
    long end = min;
    long next = min;
    long count = 0;
    private AbstractBenchmark benchmark;
    private BackendConnection connection;
    protected V context;
    private MessageHandler connOldMessageHandler = null;

    public AbstractBenchmark getBenchmark() {
        return benchmark;
    }

    public void setBenchmark(AbstractBenchmark benchmark) {
        this.benchmark = benchmark;
    }

    public BackendConnection getConnection() {
        return connection;
    }

    public void setConnection(BackendConnection connection) {
        this.connection = connection;
    }

    public boolean isDebug() {
        return debug;
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    public boolean isShowError() {
        return showError;
    }

    public void setShowError(boolean showError) {
        this.showError = showError;
    }

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public void putAllRequestProperties(Map source) {
        if (properties == null) {
            properties = new Properties();
        }
        properties.putAll(source);
    }

    public Properties getRequestProperties() {
        return properties;
    }

    public AbstractBenchmarkClient(BackendConnection connection, V context) {
        this.connection = connection;
        start = System.nanoTime();
        this.context = context;
        connOldMessageHandler = connection.getHandler();
        connection.setHandler(this);
    }

    public Map<String, Object> getNextRequestContextMap() {
        return this.benchmark.getNextRequestContextMap();
    }

    public abstract T createRequestPacket();

    public abstract T decodeRecievedPacket(byte[] message);

    public void startBenchmark() {
        postPacketToServer();
    }

    protected void afterMessageRecieved(byte[] message) {

    }

    protected void doReceiveMessage(byte[] message) {
        boolean completed = responseIsCompleted(message);
        if (debug) {
            try {
                T t = decodeRecievedPacket(message);
                System.out.println("<<-- " + t);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        afterMessageRecieved(message);

        if (completed) {
            end = System.nanoTime();
            long current = end - next;
            next = end;
            min = Math.min(min, current);
            max = Math.max(max, current);
            count++;
            afterResponseCompleted();
        }
    }

    public void afterTimeout() {
        postPacketToServer();
    }

    public void handle(Connection conn, byte[] message) {

        if (conn instanceof AuthingableBackendConnection) {
            if (!((AuthingableBackendConnection) conn).isAuthenticated()) {
                connOldMessageHandler.handle(conn, message);
            } else {
                doReceiveMessage(message);
            }
        } else {
            connOldMessageHandler.handle(conn, message);
        }
    }

    protected boolean responseIsCompleted(byte[] message) {
        return true;
    }

    protected void afterResponseCompleted() {
        context.getResponseLatcher().countDown();
        if (context.isRunning()) {
            if (context.getRequestLatcher().getCount() > 0) {
                context.getRequestLatcher().countDown();
                postPacketToServer();
            }
        }
    }

    public long getDelay(TimeUnit unit) {
        return unit.convert(this.getTimeout(), TimeUnit.SECONDS);
    }

    @Override
    public int compareTo(Delayed o) {
        if (o == this)
            return 0;
        AbstractBenchmarkClient<T, V> x = (AbstractBenchmarkClient<T, V>) o;
        long diff = this.lastSentTime - x.lastSentTime;
        if (diff < 0) {
            return -1;
        } else {
            return 1;
        }
    }

    protected boolean checkTimeOut() {
        if (this.getTimeout() > 0) {
            long time = TimeUnit.SECONDS.convert(TimeUtil.currentTimeMillis() - lastSentTime, TimeUnit.MILLISECONDS);
            if (time > this.getTimeout()) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }

    }

    protected void postPacketToServer() {
        T packet = createRequestPacket();
        ByteBuffer buffer = packet.toByteBuffer();
        if (debug) {
            System.out.println("--->> " + packet);
        }
        lastSentTime = TimeUtil.currentTimeMillis();
        connection.write(buffer);
    }

    public void init() {
    }

    /*
     * public boolean checkIdle(long now) { boolean isTimeOut = false; if(timeout>0){ if (connection.isClosed()) {
     * isTimeOut = true; } long idleMillis = now - connection._lastEvent; if (idleMillis < timeout) { isTimeOut = false;
     * }else{ isTimeOut = true; } }else{ isTimeOut = false; } if(isTimeOut){
     * logger.warn("socket id="+this.getSocketId()+" receive time out="+(now - _lastEvent)); } return isTimeOut; }
     */
}
