package com.meidusa.venus.io.packet;

import java.io.UnsupportedEncodingException;

/**
 * Still not sure how to write this class
 * 
 * @author lichencheng
 * 
 */
public class PublicKeyAuthenPacket extends DummyAuthenPacket {

    /**
	 *
	 */
    private static final long serialVersionUID = 1L;

    public String privateKey;

    public PublicKeyAuthenPacket() {
        this.authType = PacketConstant.AUTHEN_TYPE_PKI;
    }

    @Override
    protected void writeBody(ServicePacketBuffer buffer) throws UnsupportedEncodingException {
        super.writeBody(buffer);
    }

    @Override
    protected void readBody(ServicePacketBuffer buffer) {
        super.readBody(buffer);
    }
}
