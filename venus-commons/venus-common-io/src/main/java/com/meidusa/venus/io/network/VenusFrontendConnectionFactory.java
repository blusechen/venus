package com.meidusa.venus.io.network;

import java.nio.channels.SocketChannel;

import com.meidusa.toolkit.net.AuthingableFrontendConnection;
import com.meidusa.toolkit.net.FrontendConnection;
import com.meidusa.toolkit.net.MessageHandler;
import com.meidusa.toolkit.net.authenticate.server.AuthenticateProvider;
import com.meidusa.toolkit.net.factory.FrontendConnectionFactory;

/**
 * 
 * @author struct
 * 
 */
public class VenusFrontendConnectionFactory extends FrontendConnectionFactory {
    private MessageHandler<? extends AuthingableFrontendConnection, ?> messageHandler;
    private AuthenticateProvider<AuthingableFrontendConnection, ?> authenticateProvider;

    public MessageHandler<? extends AuthingableFrontendConnection, ?> getMessageHandler() {
        return messageHandler;
    }

    public void setMessageHandler(MessageHandler<? extends AuthingableFrontendConnection, ?> messageHandler) {
        this.messageHandler = messageHandler;
    }

    public AuthenticateProvider<? extends AuthingableFrontendConnection, ?> getAuthenticateProvider() {
        return authenticateProvider;
    }

    public void setAuthenticateProvider(AuthenticateProvider<AuthingableFrontendConnection, ?> authenticateProvider) {
        this.authenticateProvider = authenticateProvider;
    }

    protected FrontendConnection getConnection(SocketChannel channel) {
        VenusFrontendConnection conn = new VenusFrontendConnection(channel);
        conn.setRequestHandler(messageHandler);
        conn.setAuthenticateProvider(authenticateProvider);
        return conn;
    }
}
