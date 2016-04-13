package com.meidusa.venus.io.packet;

import java.io.UnsupportedEncodingException;

public abstract class ServiceNofityPacket extends AbstractServicePacket {
    private static final long serialVersionUID = 1L;
    public byte[] identityData;
    public int errorCode;
    public String errorMessage;

    public Object callbackObject;

    public byte[] additionalData;

    /**
     * 用于跟踪请求的标记,16个字节
     */
    public byte[] traceId;

    public String apiName;

    public ServiceNofityPacket() {
        this.type = PACKET_TYPE_NOTIFY_PUBLISH;
    }

    protected void readBody(ServicePacketBuffer buffer) {
        super.readBody(buffer);
        identityData = buffer.readLengthCodedBytes();
        errorCode = buffer.readInt();
        if (errorCode != 0) {
            errorMessage = buffer.readLengthCodedString(PACKET_CHARSET);
        } else {
            callbackObject = readCallBackObject(buffer);
        }

        if (buffer.hasRemaining()) {
            additionalData = buffer.readLengthCodedBytes();
        }

        if (buffer.hasRemaining()) {
            traceId = new byte[16];
            buffer.readBytes(traceId, 0, 16);
        }

        if (buffer.hasRemaining()) {
            apiName = buffer.readLengthCodedString(PACKET_CHARSET);
        }
    }

    protected abstract Object readCallBackObject(ServicePacketBuffer buffer);

    protected abstract void writeCallBackObject(ServicePacketBuffer buffer, Object callbackObject);

    @Override
    protected void writeBody(ServicePacketBuffer buffer) throws UnsupportedEncodingException {
        super.writeBody(buffer);
        buffer.writeLengthCodedBytes(identityData);
        buffer.writeInt(errorCode);
        if (errorCode != 0) {
            buffer.writeLengthCodedString(errorMessage, PACKET_CHARSET);
        } else {
            writeCallBackObject(buffer, callbackObject);
        }

        buffer.writeLengthCodedBytes(additionalData);

        if (traceId == null) {
            traceId = EMPTY_TRACE_ID;
        }

        buffer.writeBytes(traceId);

        buffer.writeLengthCodedString(apiName, PACKET_CHARSET);
    }

}
