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

import java.io.EOFException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.meidusa.toolkit.net.AuthingableBackendConnection;
import com.meidusa.toolkit.net.Connection;
import com.meidusa.toolkit.net.MessageHandler;
import com.meidusa.venus.io.Status;
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
public class VenusBackendConnection extends AuthingableBackendConnection implements MessageHandler<Connection, byte[]> {

    private static Logger logger = LoggerFactory.getLogger(VenusBackendConnection.class);
    private Authenticator<HandshakePacket, AuthenPacket> authenticator = new DummyAuthenticator();
    private Status status = Status.WAITE_HANDSHAKE;
    private byte serializeType;

    public VenusBackendConnection(SocketChannel channel) {
        super(channel);
        this.setHandler(this);
    }

    public byte getSerializeType() {
        return serializeType;
    }

    public void ping(long now) {
        write(new PingPacket().toByteBuffer());
        if (logger.isDebugEnabled()) {
            logger.debug("send ping packet to " + this.getId());
        }
    }

    public void handle(Connection conn, byte[] message) {
        if (AbstractServicePacket.getType(message) == PacketConstant.PACKET_TYPE_ERROR) {
            setAuthenticated(false);
            ErrorPacket error = new ErrorPacket();
            error.init(message);
            logger.error("handShake with host=" + this.getHost()+":"+ this.getPort()  + ", errorCode=" + error.errorCode + " ,message=" + error.message + ",hashCode="
                    + this.hashCode());
            return;
        }

        if (status == Status.WAITE_HANDSHAKE) {

            if (logger.isDebugEnabled()) {
                logger.debug("1. handShake with " + this.getHost()+":"+ this.getPort()  + ",hashCode=" + this.hashCode());
            }

            HandshakePacket handpacket = new HandshakePacket();
            handpacket.init(message);

            AuthenPacket authen = getAuthenticator().createAuthenPacket(handpacket);
            this.serializeType = authen.shakeSerializeType;
            authen.capabilities = authen.capabilities | PacketConstant.CAPABILITY_LISTENER;
            status = Status.AUTHING;
            if (logger.isDebugEnabled()) {
                logger.debug("2. authing packet sent to server:" + this.getHost()+":"+ this.getPort()+",hashCode=" + this.hashCode());
            }
            
            this.write(authen.toByteBuffer());
        } else if (status == Status.AUTHING) {

            if (AbstractServicePacket.getType(message) == PacketConstant.PACKET_TYPE_OK) {
                if (logger.isDebugEnabled()) {
                    logger.debug("3. authing success from server:" + this.getHost()+":"+ this.getPort()  + ",hashCode=" + this.hashCode());
                }
                this.status = Status.COMPLETED;
                setAuthenticated(true);
                return;
            } else {
                if (logger.isDebugEnabled()) {
                    logger.debug("3. authing fail from server:" + this.getHost()+":"+ this.getPort()  + ",hashCode=" + this.hashCode());
                }
                this.status = Status.AUTH_ERROR;
                setAuthenticated(false);
                return;
            }
        }
    }

    public Authenticator<HandshakePacket, AuthenPacket> getAuthenticator() {
        return authenticator;
    }

    public void setAuthenticator(Authenticator<HandshakePacket, AuthenPacket> authenticator) {
        this.authenticator = authenticator;
    }

    public boolean checkValid() {
        return !this.isClosed() && this.isAuthenticated();
    }

    @Override
    public void handleError(int errCode, Throwable t) {
        if(t instanceof EOFException){
            if(logger.isDebugEnabled()){
                logger.debug("handle error=" + errCode + ", host=" + this.getHost() + ":" + this.port + ",hash=" + this.hashCode(), t);
            }
        }else{
            logger.error("handle error=" + errCode + ", host=" + this.getHost() + ":" + this.port + ",hash=" + this.hashCode(), t);
        }
        
        this.close();
    }

    protected int getPacketLength(ByteBuffer buffer, int offset) throws IOException {
        if (buffer.position() < offset + PacketConstant.PACKET_HEADER_SIZE) {
            return -1;
        } else {
            int length = (buffer.get(offset) & 0xff) << 24;
            length |= (buffer.get(++offset) & 0xff) << 16;
            length |= (buffer.get(++offset) & 0xff) << 8;
            length |= (buffer.get(++offset) & 0xff) << 0;
            
            if(length <= 0){
                throw new IOException("receive packet lenght error, lenght="+length +",host="+this.getHost());
            }
            return length;
        }
    }

}
