package com.meidusa.venus.client.authenticate;

import com.meidusa.toolkit.net.packet.AbstractPacket;

/**
 * 2.x 与 3.x 兼容性考虑.保留该类
 * 
 * @author structchen
 * @deprecated
 */
public interface Authenticator extends com.meidusa.venus.io.authenticate.Authenticator<AbstractPacket, AbstractPacket> {

}
