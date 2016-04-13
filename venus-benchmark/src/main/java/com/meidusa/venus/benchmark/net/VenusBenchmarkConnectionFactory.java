package com.meidusa.venus.benchmark.net;

import java.nio.channels.SocketChannel;

import com.meidusa.toolkit.net.BackendConnection;
import com.meidusa.venus.io.network.VenusBackendConnectionFactory;

public class VenusBenchmarkConnectionFactory extends VenusBackendConnectionFactory {

    protected BackendConnection create(SocketChannel channel) {
        VenusBenchmarkConnection c = new VenusBenchmarkConnection(channel);
        c.setResponseMessageHandler(getMessageHandler());
        c.setAuthenticator(this.getAuthenticator());
        return c;
    }
}
