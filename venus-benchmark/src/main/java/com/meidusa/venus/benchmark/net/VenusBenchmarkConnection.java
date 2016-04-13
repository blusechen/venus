package com.meidusa.venus.benchmark.net;

import java.nio.channels.SocketChannel;

import com.meidusa.venus.io.network.VenusBackendConnection;

public class VenusBenchmarkConnection extends VenusBackendConnection {

    public VenusBenchmarkConnection(SocketChannel channel) {
        super(channel);
    }

    public boolean checkIdle(long now) {
        return false;
    }

    public boolean needPing(long now) {
        return false;
    }
}
