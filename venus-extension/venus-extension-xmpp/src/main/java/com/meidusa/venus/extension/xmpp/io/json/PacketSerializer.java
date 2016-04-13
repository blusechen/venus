package com.meidusa.venus.extension.xmpp.io.json;

import org.dom4j.Element;
import org.xmpp.packet.Packet;

public class PacketSerializer extends ElementSerializer<Packet> {

    protected Element getElement(Packet object) {
        return (Element) object.getElement();
    }
}
