package com.meidusa.venus.extension.xmpp.io.bson;

import org.dom4j.Element;
import org.xmpp.packet.IQ;
import org.xmpp.packet.Message;
import org.xmpp.packet.Packet;
import org.xmpp.packet.Presence;

public class PacketObjectSerializer extends ElementObjectSerializer<Packet> {

    protected Packet createObject(Element element) {
        Packet packet = null;
        if ("iq".equals(element.getName())) {
            packet = new IQ(element);
        } else if ("presence".equals(element.getName())) {
            packet = new Presence(element);
        } else if ("message".equals(element.getName())) {
            packet = new Message(element);
        }
        return packet;
    }

    public Class<?> getSerializedClass() {
        return Packet.class;
    }

    protected Element getElement(Object value) {
        return ((Packet) value).getElement();
    }
}
