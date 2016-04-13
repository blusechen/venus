package com.meidusa.venus.io.packet;

public class DummyAuthenPacket extends AuthenPacket {
    private static final long serialVersionUID = 1L;

    public DummyAuthenPacket() {
        this.authType = AUTHEN_TYPE_DUMMY;
    }

    public static void main(String[] args) {
        new DummyAuthenPacket();
    }
}
