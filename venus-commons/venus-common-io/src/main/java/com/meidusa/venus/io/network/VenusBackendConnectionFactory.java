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

import java.nio.channels.SocketChannel;

import com.meidusa.toolkit.net.BackendConnection;
import com.meidusa.toolkit.net.MessageHandler;
import com.meidusa.toolkit.net.ValidatorMessageHandler;
import com.meidusa.toolkit.net.factory.AuthingableBackendConnectionFactory;
import com.meidusa.venus.io.authenticate.Authenticator;
import com.meidusa.venus.io.authenticate.DummyAuthenticator;
import com.meidusa.venus.io.packet.AuthenPacket;
import com.meidusa.venus.io.packet.HandshakePacket;

/**
 * 
 * @author struct
 * 
 */
@SuppressWarnings("rawtypes")
public class VenusBackendConnectionFactory extends AuthingableBackendConnectionFactory {
    private Authenticator<HandshakePacket, AuthenPacket> authenticator = new DummyAuthenticator();

    private MessageHandler messageHandler;

    public MessageHandler getMessageHandler() {
        return messageHandler;
    }

    public void setMessageHandler(MessageHandler messageHandler) {
        this.messageHandler = messageHandler;
    }

    public Authenticator<HandshakePacket, AuthenPacket> getAuthenticator() {
        return authenticator;
    }

    public void setAuthenticator(Authenticator authenticator) {
        this.authenticator = authenticator;
    }

    @Override
    protected BackendConnection create(SocketChannel channel) {
        VenusBackendConnection c = new VenusBackendConnection(channel);
        c.setAuthenticator(authenticator);
        c.setResponseMessageHandler(messageHandler);
        return c;
    }

	@Override
	public ValidatorMessageHandler createValidatorMessageHandler() {
		return null;
	}

}
