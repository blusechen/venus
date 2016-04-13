package com.meidusa.venus.extension.xmpp.io.json;

import java.util.ArrayList;
import java.util.List;

import org.dom4j.Element;
import org.xmpp.packet.IQ;
import org.xmpp.packet.Message;
import org.xmpp.packet.Packet;
import org.xmpp.packet.Presence;

import com.meidusa.venus.io.packet.PacketConstant;
import com.meidusa.venus.io.serializer.Serializer;
import com.meidusa.venus.io.serializer.SerializerFactory;

public class XmppJsonSerializer {

    /**
     * @param args
     */
    public static void main(String[] args) {
        List<Packet> list = new ArrayList<Packet>();
        Packet message = new Message();
        message.setTo("1234@snda.com/android");
        message.setFrom("abcd@snda.com/iphone");
        list.add(message);
        testSerializer(message, PacketConstant.CONTENT_TYPE_BSON);

        testSerializer(message, PacketConstant.CONTENT_TYPE_JSON);

        Packet iq = new IQ();
        iq.setTo("1234@snda.com/android");
        iq.setFrom("abcd@snda.com/iphone");
        list.add(iq);
        testSerializer(iq, PacketConstant.CONTENT_TYPE_BSON);

        testSerializer(iq, PacketConstant.CONTENT_TYPE_JSON);

        Packet presence = new Presence();
        presence.setTo("1234@snda.com/android");
        presence.setFrom("abcd@snda.com/iphone");
        list.add(presence);
        testSerializer(presence, PacketConstant.CONTENT_TYPE_BSON);
        testSerializer(presence, PacketConstant.CONTENT_TYPE_JSON);

        testSerializerElement(presence.getElement(), PacketConstant.CONTENT_TYPE_BSON);
        testSerializerElement(presence.getElement(), PacketConstant.CONTENT_TYPE_JSON);
    }

    private static void testSerializer(Packet packet, byte type) {
        Serializer serializer = SerializerFactory.getSerializer(type);
        byte[] bts = serializer.encode(packet);

        Object obj = serializer.decode(bts, Packet.class);
        System.out.println(obj.getClass());
    }

    private static void testSerializerElement(Element element, byte type) {
        Serializer serializer = SerializerFactory.getSerializer(type);
        byte[] bts = serializer.encode(element);

        Object obj = serializer.decode(bts, Element.class);
        System.out.println(obj.getClass());
    }

    private static void testSerializer(List<Packet> list, byte type) {
        Serializer serializer = SerializerFactory.getSerializer(type);
        byte[] bts = serializer.encode(list);
    }

}
