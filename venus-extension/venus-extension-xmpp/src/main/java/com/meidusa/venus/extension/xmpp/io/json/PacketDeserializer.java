package com.meidusa.venus.extension.xmpp.io.json;

import java.lang.reflect.Type;

import org.dom4j.Element;
import org.xmpp.packet.IQ;
import org.xmpp.packet.Message;
import org.xmpp.packet.Packet;
import org.xmpp.packet.Presence;

import com.meidusa.fastjson.parser.DefaultExtJSONParser;

public class PacketDeserializer extends ElementDeserializer {

    public <T> T deserialze(DefaultExtJSONParser parser, Type type, Object fieldName) {
        Element element = super.deserialze(parser, type, fieldName);

        if (element == null) {
            return null;
        }

        try {

            Packet packet = null;
            if ("iq".equals(element.getName())) {
                packet = new IQ(element);
            } else if ("presence".equals(element.getName())) {
                packet = new Presence(element);
            } else if ("message".equals(element.getName())) {
                packet = new Message(element);
            }

            return (T) packet;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

    }

}
