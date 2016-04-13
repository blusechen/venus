package com.meidusa.venus.io.authenticate;

import com.meidusa.venus.io.packet.HandshakePacket;
import com.meidusa.venus.io.packet.PasswordAuthenPacket;

public class UserPasswordAuthenticator extends DummyAuthenticator<PasswordAuthenPacket> {

    private String password = "venus";

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    protected void initPacket(PasswordAuthenPacket packet, HandshakePacket inPacket) {
        super.initPacket(packet, inPacket);
        packet.challenge = inPacket.challenge;
        packet.password = this.password;

    }

    protected PasswordAuthenPacket newAuthPacket() {
        PasswordAuthenPacket packet = new PasswordAuthenPacket();
        return packet;
    }

}
