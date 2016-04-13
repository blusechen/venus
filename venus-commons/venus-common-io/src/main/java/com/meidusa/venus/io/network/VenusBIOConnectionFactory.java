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

package com.meidusa.venus.io.network;

import java.io.IOException;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.Socket;

import org.apache.commons.pool.PoolableObjectFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.meidusa.toolkit.net.util.AuthenticationException;
import com.meidusa.toolkit.util.TimeUtil;
import com.meidusa.venus.io.authenticate.Authenticator;
import com.meidusa.venus.io.authenticate.DummyAuthenticator;
import com.meidusa.venus.io.packet.AbstractServicePacket;
import com.meidusa.venus.io.packet.AuthenPacket;
import com.meidusa.venus.io.packet.ErrorPacket;
import com.meidusa.venus.io.packet.HandshakePacket;
import com.meidusa.venus.io.packet.PacketConstant;
import com.meidusa.venus.io.packet.PingPacket;

/**
 * 
 * @author Struct
 * 
 */
public class VenusBIOConnectionFactory implements PoolableObjectFactory<VenusBIOConnection> {
    private static Logger authenticatorLogger = LoggerFactory.getLogger(Authenticator.class);
    private static Logger logger = LoggerFactory.getLogger(VenusBIOConnectionFactory.class);
    protected static PingPacket PING_PACKET = new PingPacket();
    private int sendBufferSize = 64;
    private int receiveBufferSize = 64;
    private boolean tcpNoDelay = true;
    private boolean keepAlive = true;
    private int coTimeout = 5 * 1000;
    private String host;
    private int port = 16800;
    private int soTimeout = 30 * 1000;
    private boolean needPing = false;
    private long maxLiveTime = 0;
    private Authenticator<HandshakePacket, AuthenPacket> authenticator = new DummyAuthenticator();

    public int getCoTimeout() {
        return coTimeout;
    }

    public void setCoTimeout(int coTimeout) {
        this.coTimeout = coTimeout;
    }

    public boolean isNeedPing() {
        return needPing;
    }

    public void setNeedPing(boolean needPing) {
        this.needPing = needPing;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String ipAddress) {
        this.host = ipAddress;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public int getSoTimeout() {
        return soTimeout;
    }

    public void setSoTimeout(int soTimeout) {
        this.soTimeout = soTimeout;
    }

    public int getSendBufferSize() {
        return sendBufferSize;
    }

    public void setSendBufferSize(int sendBufferSize) {
        this.sendBufferSize = sendBufferSize;
    }

    public int getReceiveBufferSize() {
        return receiveBufferSize;
    }

    public void setReceiveBufferSize(int receiveBufferSize) {
        this.receiveBufferSize = receiveBufferSize;
    }

    public boolean isTcpNoDelay() {
        return tcpNoDelay;
    }

    public void setTcpNoDelay(boolean tcpNoDelay) {
        this.tcpNoDelay = tcpNoDelay;
    }

    public boolean isKeepAlive() {
        return keepAlive;
    }

    public void setKeepAlive(boolean keepAlive) {
        this.keepAlive = keepAlive;
    }

    public void activateObject(VenusBIOConnection arg0) throws Exception {

    }

    public Authenticator<HandshakePacket, AuthenPacket> getAuthenticator() {
        return authenticator;
    }

    public void setAuthenticator(Authenticator<HandshakePacket, AuthenPacket> authenticator) {
        this.authenticator = authenticator;
    }

    public long getMaxLiveTime() {
        return maxLiveTime;
    }

    public void setMaxLiveTime(long maxLiveTime) {
        this.maxLiveTime = maxLiveTime;
    }

    public void destroyObject(VenusBIOConnection arg0) throws Exception {
        AbstractBIOConnection conn = (AbstractBIOConnection) arg0;
        conn.close();
        if (logger.isDebugEnabled()) {
            logger.debug("conn id=" + conn + " destory");
        }
    }

    public VenusBIOConnection makeObject() throws Exception {
        Socket socket = new Socket();
        InetSocketAddress address = null;
        if (host == null) {
            address = new InetSocketAddress(port);
        } else {
            address = new InetSocketAddress(host, port);
        }

        socket.setSendBufferSize(sendBufferSize * 1024);
        socket.setReceiveBufferSize(receiveBufferSize * 1024);
        socket.setTcpNoDelay(tcpNoDelay);
        socket.setKeepAlive(keepAlive);
        try {
            if (soTimeout > 0) {
                socket.setSoTimeout(soTimeout);
            }
            if (coTimeout > 0) {
                socket.connect(address, coTimeout);
            } else {
                socket.connect(address);
            }
        } catch (ConnectException e) {
            throw new ConnectException(e.getMessage() + " " + address.getHostName() + ":" + address.getPort());
        }

        VenusBIOConnection conn = new VenusBIOConnection(socket, TimeUtil.currentTimeMillis());
        byte[] bts = conn.read();
        HandshakePacket handshakePacket = new HandshakePacket();
        handshakePacket.init(bts);

        AuthenPacket authen = getAuthenticator().createAuthenPacket(handshakePacket);
        conn.write(authen.toByteArray());
        bts = conn.read();
        int type = AbstractServicePacket.getType(bts);
        if (type == PacketConstant.PACKET_TYPE_OK) {
            if (authenticatorLogger.isInfoEnabled()) {
                authenticatorLogger.info("authenticated by server=" + host + ":" + port + " success");
            }
        } else if (type == PacketConstant.PACKET_TYPE_ERROR) {
            ErrorPacket error = new ErrorPacket();
            error.init(bts);
            if (authenticatorLogger.isInfoEnabled()) {
                authenticatorLogger
                        .info("authenticated by server=" + host + ":" + port + " error={code=" + error.errorCode + ",message=" + error.message + "}");
            }
            throw new AuthenticationException(error.message, error.errorCode);
        }

        return conn;
    }

    public void passivateObject(VenusBIOConnection arg0) throws Exception {

    }

    public boolean validateObject(VenusBIOConnection arg0) {
        VenusBIOConnection conn = (VenusBIOConnection) arg0;
        if (conn.isClosed()) {
            return false;
        }
        if (needPing) {
            try {
                conn.write(PING_PACKET.toByteArray());
                conn.read();
            } catch (IOException e) {
                return false;
            }
        }
        if (maxLiveTime != 0) {
            long eliminateTime = conn.createTimestamp + maxLiveTime;
            if (eliminateTime > TimeUtil.currentTimeMillis()) {
                return false;
            }
        }
        return true;
    }

}
