package com.meidusa.venus.io.decoder;

import java.io.UnsupportedEncodingException;

import com.meidusa.venus.io.packet.AbstractServiceRequestPacket;
import com.meidusa.venus.io.packet.PacketConstant;
import com.meidusa.venus.io.packet.ServicePacketBuffer;
import com.meidusa.venus.io.utils.GZipUtil;
import com.meidusa.venus.util.UUID;
import com.meidusa.venus.util.VenusTracerUtil;

public class SimpleServiceRequestPacket extends AbstractServiceRequestPacket {
    private static final long serialVersionUID = 1L;

    public byte[] traceId;
    public byte[] rootId;
    public byte[] parentId;
    public byte[] messageId;

    public byte[] paramterBytes;

    public SimpleServiceRequestPacket() {
        super();
    }

    protected void readBody(ServicePacketBuffer buffer) {
        super.readBody(buffer);
        readParams(buffer);

        // 兼容3.0.1之前的版本,3.0.2与之后的版本将支持traceID
        if (buffer.hasRemaining()) {
            traceId = new byte[16];
            buffer.readBytes(traceId, 0, 16);
        } else {
            traceId = PacketConstant.EMPTY_TRACE_ID;
        }

        if (buffer.hasRemaining()) {
            rootId = buffer.readLengthCodedBytes();
        }

        if (buffer.hasRemaining()) {
            parentId = buffer.readLengthCodedBytes();
        }
        if (buffer.hasRemaining()) {
            messageId = buffer.readLengthCodedBytes();
        }
    }

    protected void writeBody(ServicePacketBuffer buffer) throws UnsupportedEncodingException {
        super.writeBody(buffer);
        wirteParams(buffer);

        // 兼容3.0.1之前的版本,3.0.2与之后的版本将支持traceID
        if (traceId == null) {
            traceId = EMPTY_TRACE_ID;
        }
        buffer.writeBytes(traceId);
        buffer.writeLengthCodedBytes(rootId);
        buffer.writeLengthCodedBytes(parentId);
        buffer.writeLengthCodedBytes(messageId);
    }

    protected void wirteParams(ServicePacketBuffer buffer) {
        if (paramterBytes != null) {
            byte[] bts = paramterBytes;
            if (bts != null) {
                if (PacketConstant.AUTO_COMPRESS_SIZE > 0 && bts.length > PacketConstant.AUTO_COMPRESS_SIZE) {
                    buffer.writeLengthCodedBytes(GZipUtil.compress(bts));
                    this.flags = (byte) (this.flags | CAPABILITY_GZIP);
                } else {
                    buffer.writeLengthCodedBytes(bts);
                }
            } else {
                buffer.writeInt(0);
            }
        }else{
            buffer.writeInt(0);
        }
    }

    protected void readParams(ServicePacketBuffer buffer) {
        if (buffer.hasRemaining()) {
            byte[] bts = buffer.readLengthCodedBytes();
            byte f = (byte) (this.flags & CAPABILITY_GZIP);
            if (bts != null & bts.length > 0) {
                if (f == CAPABILITY_GZIP) {
                    bts = GZipUtil.decompress(bts);
                }
                
                paramterBytes = bts;
            }

        }
        
        if(parameterMap == null){
            parameterMap = EMP_MAP;
        }
    }

    protected void readMessageId(ServicePacketBuffer buffer) {

    }
    
    public static void main(String[] args) {
    	SimpleServiceRequestPacket request = new SimpleServiceRequestPacket();
    	request.paramterBytes = new byte[32 * 1024 * 1024];
    	request.apiName = "hello.getName";
    	request.traceId = VenusTracerUtil.randomTracerID();
    	
    	byte[] bts = request.toByteArray();
    	
    	ServicePacketBuffer buffer = new ServicePacketBuffer(bts);
    	SimpleServiceRequestPacket response = new SimpleServiceRequestPacket();
    	
    	response.init(buffer);
    	
    	System.out.println(response.apiName+","+new UUID(response.traceId));
	}
}
