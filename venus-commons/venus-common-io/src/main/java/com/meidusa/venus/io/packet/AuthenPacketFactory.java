package com.meidusa.venus.io.packet;

import java.util.HashMap;
import java.util.Map;

/**
 * 
 * @author struct
 * 
 */
public class AuthenPacketFactory {
    private static AuthenPacketFactory factory = new AuthenPacketFactory();

    public static AuthenPacketFactory getInstance() {
        return factory;
    }

    private AuthenPacketFactory() {
    };

    private Map<Byte, Class<? extends AuthenPacket>> authenMap = new HashMap<Byte, Class<? extends AuthenPacket>>();
    {
        register(PacketConstant.AUTHEN_TYPE_DUMMY, DummyAuthenPacket.class);
        register(PacketConstant.AUTHEN_TYPE_PASSWORD, PasswordAuthenPacket.class);
        register(PacketConstant.AUTHEN_TYPE_PKI, PublicKeyAuthenPacket.class);
    }

    public synchronized void register(byte type, Class<? extends AuthenPacket> clazz) {
        authenMap.put(type, clazz);
    }

    public AuthenPacket createAuthenPacket(byte[] message) throws Exception {

        Class<? extends AuthenPacket> clazz = authenMap.get(AuthenPacket.getAuthenType(message));
        AuthenPacket packet;
        try {
            packet = clazz.newInstance();
            packet.init(message);
            return packet;
        } catch (Exception e) {
            throw e;
        }
    }
}
