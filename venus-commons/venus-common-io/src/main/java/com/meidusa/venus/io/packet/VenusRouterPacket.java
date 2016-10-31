package com.meidusa.venus.io.packet;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;

import com.meidusa.toolkit.net.packet.AbstractPacketBuffer;
import com.meidusa.toolkit.net.packet.GenericIOPacketBuffer;
import com.meidusa.toolkit.net.util.InetAddressUtil;

public class VenusRouterPacket extends AbstractVenusPacket {
    /**
     * connection Sequence id in router packet
     */
    public static final int CONNECTION_SEQUENCE_ID = VENUS_HEADER_LENGTH + 4;

    /**
     * connection Sequence id in router packet
     */
    public static final int FRONTEND_REQUEST_ID = CONNECTION_SEQUENCE_ID + 8;

    /**
     * connection Sequence id in router packet
     */
    public static final int BACKEND_REQUEST_ID = FRONTEND_REQUEST_ID + 8;

    public static final int VENUS_ROUTER_PACKET_DATA_POSITION = BACKEND_REQUEST_ID + 8 + 1;

    private static final long serialVersionUID = 1L;

    /**
     * 该数据包的原始数据内容，该字段不参与数据传输，只是借助该对象进行数据传递
     */
    public transient byte[] original;
    
    /**
     * 服务请求进入bus的时间点,仅仅用于bus中
     */
    public transient long startTime;
    
    public transient String api;
    
    public transient String traceId;

    /**
     * 客户端的原始IP
     * 
     * @see InetAddressUtil#pack(byte[])
     */
    public int srcIP;
    

    /**
     * 客户端的在HSB中的链接ID标识
     */
    public long frontendConnectionID;

    /**
     * Frontend connection RequestID in bus
     */
    public long frontendRequestID;

    /**
     * Backend connection RequestID in bus
     */
    public long backendRequestID;

    /**
	 * 在客户端经过bus的所有数据包，该参数则代表最初的请求方所采用的序列化方式。
	 * 如果服务端接收到VenusRouterPacket类型的数据包，序列化方式则以该字段为准。
	 * 其他数据包则以 connection中的serializeType为准
     */
    public byte serializeType = -1;

    /**
     * 路由数据包中，该字段代表客户端请求的数据包内容（一次请求的整体逻辑数据包）
     */
    public byte[] data;

    public VenusRouterPacket() {
        this.type = PACKET_TYPE_ROUTER;
    }

    @Override
    protected void readBody(ServicePacketBuffer buffer) {
        srcIP = buffer.readInt();
        frontendConnectionID = buffer.readLong();
        frontendRequestID = buffer.readLong();
        backendRequestID = buffer.readLong();
        serializeType = buffer.readByte();
        
       /* 
        * old version
        * data = buffer.readRemaining();
        * buffer.setPosition(buffer.getPosition() + data.length);
        */
        //new version
        int position = buffer.getPosition();
        int length = buffer.readInt();
        buffer.setPosition(position);
    	data = new byte[length];
    	buffer.readBytes(data);
    }

    @Override
    protected void writeBody(ServicePacketBuffer buffer) throws UnsupportedEncodingException {
        buffer.writeInt(srcIP);
        buffer.writeLong(frontendConnectionID);
        buffer.writeLong(frontendRequestID);
        buffer.writeLong(backendRequestID);
        buffer.writeByte(serializeType);
        buffer.writeBytes(data);

    }

    public static ByteBuffer toByteBuffer(VenusRouterPacket packet) {

        byte[] result = toByteArray(packet);
        return AbstractPacketBuffer.toByteBuffer(result, 0, result.length);
    }

    public static byte[] toByteArray(VenusRouterPacket packet) {
        int position = 4;
        byte[] result = new byte[39 + packet.data.length];

        // header
        position = GenericIOPacketBuffer.writeShort(result, position, PROTOCOL_VERSION);
        position = GenericIOPacketBuffer.writeInt(result, position, packet.type);

        // router
        position = GenericIOPacketBuffer.writeInt(result, position, packet.srcIP);
        position = GenericIOPacketBuffer.writeLong(result, position, packet.frontendConnectionID);
        position = GenericIOPacketBuffer.writeLong(result, position, packet.frontendRequestID);
        position = GenericIOPacketBuffer.writeLong(result, position, packet.backendRequestID);
        result[position++] = packet.serializeType;

        System.arraycopy(packet.data, 0, result, position, packet.data.length);

        // write packet length
        GenericIOPacketBuffer.writeInt(result, 0, result.length);
        return result;
    }

    @Override
    protected int calculatePacketSize() {
        return 42;
    }

    @Override
    protected Class<ServicePacketBuffer> getPacketBufferClass() {
        return ServicePacketBuffer.class;
    }

    public static long getConnectionSequenceID(byte[] buf) {
        return GenericIOPacketBuffer.readLong(buf, CONNECTION_SEQUENCE_ID);
    }

    public static long getSourceRequestID(byte[] buf) {
        return GenericIOPacketBuffer.readLong(buf, FRONTEND_REQUEST_ID);
    }

    public static long getRemoteRequestID(byte[] buf) {
        return GenericIOPacketBuffer.readLong(buf, BACKEND_REQUEST_ID);
    }

    public static byte[] getData(byte[] message) {
        byte[] bts = new byte[message.length - VENUS_ROUTER_PACKET_DATA_POSITION];
        System.arraycopy(message, VENUS_ROUTER_PACKET_DATA_POSITION, bts, 0, bts.length);
        return bts;
    }

}
