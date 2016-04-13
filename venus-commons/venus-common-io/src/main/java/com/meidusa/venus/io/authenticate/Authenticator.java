package com.meidusa.venus.io.authenticate;

import com.meidusa.toolkit.net.packet.AbstractPacket;

/**
 * 
 * @author Administrator
 * 
 * @param <I> incoming Packet from Server
 * @param <O> authenticationPacket will be send packet to Server
 */
@SuppressWarnings("rawtypes")
public interface Authenticator<I extends AbstractPacket, O extends AbstractPacket> {

    public O createAuthenPacket(I inPacket);

    public byte getSerializeType();

    public void setSerializeType(byte serializeType);
}
