package com.meidusa.venus.backend.network;

import java.nio.channels.SocketChannel;

import com.meidusa.toolkit.common.util.Tuple;
import com.meidusa.toolkit.net.config.ExceptionCodeConstant;
import com.meidusa.toolkit.util.TimeUtil;
import com.meidusa.venus.io.network.VenusFrontendConnection;

public class VenusBackendFrontendConnection extends VenusFrontendConnection {

    public VenusBackendFrontendConnection(SocketChannel channel) {
        super(channel);
    }

    @Override
    public void handle(final byte[] data) {
        final Tuple<Long, byte[]> tuple = new Tuple<Long, byte[]>(TimeUtil.currentTimeMillis(), data);
        // 异步处理前端数据
        if (processor.getExecutor() != null) {
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    try {
                        handler.handle(VenusBackendFrontendConnection.this, tuple);
                    } catch (Throwable t) {
                        handleError(ExceptionCodeConstant.ERR_HANDLE_DATA, t);
                    }
                }
            };
            processor.getExecutor().execute(runnable);
        } else {
            try {
                handler.handle(VenusBackendFrontendConnection.this, tuple);
            } catch (Throwable t) {
                handleError(ExceptionCodeConstant.ERR_HANDLE_DATA, t);
            }
        }
    }
}
