package com.meidusa.venus.io.packet;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

/**
 * 
 * @author Struct
 * 
 */
public class AbstractServiceRequestPacket extends AbstractServicePacket {
    public static final Map<String, Object> EMP_MAP = new HashMap<String,Object>();
    private static final long serialVersionUID = 1L;
    public long reserve;// reserve
    public String apiName;
    public int serviceVersion;
    public Map<String, Object> parameterMap;

    public AbstractServiceRequestPacket() {
        type = PACKET_TYPE_SERVICE_REQUEST;
    }

    protected void writeBody(ServicePacketBuffer buffer) throws UnsupportedEncodingException {
        super.writeBody(buffer);
        buffer.writeLong(reserve);
        buffer.writeLengthCodedString(apiName, PacketConstant.PACKET_CHARSET);
        buffer.writeInt(serviceVersion);
    }

    @Override
    protected void readBody(ServicePacketBuffer buffer) {
        super.readBody(buffer);
        reserve = buffer.readLong();
        apiName = buffer.readLengthCodedString(PacketConstant.PACKET_CHARSET);
        serviceVersion = buffer.readInt();
    }

    protected int calculatePacketSize() {
        return super.calculatePacketSize() + 64;
    }

}
