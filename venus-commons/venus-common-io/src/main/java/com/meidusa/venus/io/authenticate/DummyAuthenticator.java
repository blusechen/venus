package com.meidusa.venus.io.authenticate;

import com.meidusa.venus.VenusMetaInfo;
import com.meidusa.venus.io.packet.AuthenPacket;
import com.meidusa.venus.io.packet.DummyAuthenPacket;
import com.meidusa.venus.io.packet.HandshakePacket;
import com.meidusa.venus.io.packet.PacketConstant;

public class DummyAuthenticator<O extends AuthenPacket> implements Authenticator<HandshakePacket, O> {
    private int clientId = PacketConstant.VENUS_CLIENT_ID;
    private String username = "venus";
    private byte serializeType = PacketConstant.CONTENT_TYPE_JSON;

    public int getClientId() {
        return clientId;
    }

    public void setClientId(int clientId) {
        this.clientId = clientId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public byte getSerializeType() {
        return serializeType;
    }

    public void setSerializeType(byte serializeType) {
        this.serializeType = serializeType;
    }

    protected void initPacket(O packet, HandshakePacket inPacket) {
        packet.clientId = this.clientId;
        packet.version = VenusMetaInfo.VENUS_VERSION;
        packet.username = this.username;
        packet.shakeSerializeType = this.serializeType;
    }

    protected O newAuthPacket() {
        DummyAuthenPacket packet = new DummyAuthenPacket();
        return (O) packet;
    }

    @Override
    public O createAuthenPacket(HandshakePacket inPacket) {
        O packet = newAuthPacket();
        initPacket(packet, inPacket);
        return packet;
    }

}
