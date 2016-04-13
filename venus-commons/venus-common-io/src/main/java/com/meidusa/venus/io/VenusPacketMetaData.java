package com.meidusa.venus.io;

import com.meidusa.toolkit.net.io.AbstractPacketMetaData;
import com.meidusa.venus.io.packet.PacketConstant;

public class VenusPacketMetaData extends AbstractPacketMetaData implements PacketConstant {

    @Override
    public int getHeaderSize() {
        return PacketConstant.SERVICE_HEADER_SIZE;
    }
}
