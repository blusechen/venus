package com.meidusa.venus.io.network;

import java.io.EOFException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.meidusa.toolkit.net.AuthingableFrontendConnection;
import com.meidusa.venus.io.packet.OKPacket;
import com.meidusa.venus.io.packet.PacketConstant;

/**
 * 
 * @author Struct
 * 
 */
public class VenusFrontendConnection extends AuthingableFrontendConnection {
    private static class SequenceGenerator {

        private static final long MAX_VALUE = Long.MAX_VALUE;

        private long id = 0L;
        private final Object lock = new Object();

        private long nextId() {
            synchronized (lock) {
                if (id >= MAX_VALUE) {
                    id = 0L;
                }
                return ++id;
            }
        }
    }

    private static Logger logger = LoggerFactory.getLogger(VenusFrontendConnection.class);
    private static final SequenceGenerator SEQUENCE_GENERATOR = new SequenceGenerator();
    public static OKPacket OK = new OKPacket();
    private byte serializeType = PacketConstant.CONTENT_TYPE_JSON;
    private int clientId;
    private long sequenceID;

    public VenusFrontendConnection(SocketChannel channel) {
        super(channel);
        this.sequenceID = SEQUENCE_GENERATOR.nextId();
    }

    public short getProtocol() {
        return getSerializeType();
    }

    public int getClientId() {
        return clientId;
    }

    public byte getSerializeType() {
        return serializeType;
    }

    public long getSequenceID() {
        return this.sequenceID;
    }

    /**
     * 正在处于验证的Connection Idle时间可以设置相应的少一点。
     */
    public boolean isIdleTimeout() {
        if (this.isAuthenticated) {
            return false;
        } else {
            return super.isIdleTimeout();
        }
    }

    @Override
    public void handleError(int errCode, Throwable t) {
        // 根据异常类型和信息，选择日志输出级别。
        if (t instanceof EOFException) {
            if (logger.isDebugEnabled()) {
                logger.debug(toString(), t);
            }
        } else if (isConnectionReset(t)) {
            if (logger.isInfoEnabled()) {
                logger.info(toString(), t);
            }
        } else {
            logger.warn(toString(), t);
        }

        close();
    }

    public void setClientId(int clientId) {
        this.clientId = clientId;
    }

    public void setSerializeType(byte serializeType) {
        this.serializeType = serializeType;
    }

    protected int getPacketLength(ByteBuffer buffer, int offset) {
        if (buffer.position() < offset + PacketConstant.PACKET_HEADER_SIZE) {
            return -1;
        } else {
            int length = (buffer.get(offset) & 0xff) << 24;
            length |= (buffer.get(++offset) & 0xff) << 16;
            length |= (buffer.get(++offset) & 0xff) << 8;
            length |= (buffer.get(++offset) & 0xff) << 0;
            return length;
        }
    }

}
