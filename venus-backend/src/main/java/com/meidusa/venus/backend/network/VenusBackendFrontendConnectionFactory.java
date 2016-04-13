package com.meidusa.venus.backend.network;

import java.nio.channels.SocketChannel;

import com.meidusa.toolkit.net.FrontendConnection;
import com.meidusa.venus.io.network.VenusFrontendConnectionFactory;

public class VenusBackendFrontendConnectionFactory extends VenusFrontendConnectionFactory {

    protected FrontendConnection getConnection(SocketChannel channel) {
        VenusBackendFrontendConnection conn = new VenusBackendFrontendConnection(channel);
        conn.setRequestHandler(getMessageHandler());
        conn.setAuthenticateProvider(getAuthenticateProvider());
        return conn;
    }
}
