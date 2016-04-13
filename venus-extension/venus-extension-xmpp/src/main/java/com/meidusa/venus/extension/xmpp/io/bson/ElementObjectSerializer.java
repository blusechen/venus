package com.meidusa.venus.extension.xmpp.io.bson;

import java.io.StringReader;

import org.dom4j.Document;
import org.dom4j.Element;

import com.meidusa.fastbson.parse.BSONScanner;
import com.meidusa.fastbson.parse.BSONWriter;
import com.meidusa.fastbson.serializer.ObjectSerializer;
import com.meidusa.venus.extension.xmpp.XMPPPacketReader;

public class ElementObjectSerializer<T> implements ObjectSerializer {

    private static ThreadLocal<XMPPPacketReader> xmppReadThreadLocal = new ThreadLocal<XMPPPacketReader>() {
        protected XMPPPacketReader initialValue() {
            return new XMPPPacketReader();
        }
    };

    public Object deserialize(BSONScanner scanner, ObjectSerializer[] subSerializer, int i) {
        Element element = getElement(scanner);
        if (element == null)
            return null;
        return createObject(element);
    }

    protected Object createObject(Element element) {
        return element;
    }

    protected Element getElement(BSONScanner scanner) {
        String id = scanner.readString();
        try {
            XMPPPacketReader reader = xmppReadThreadLocal.get();
            Document doc = reader.read(new StringReader(id));
            return doc.getRootElement();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public void serialize(BSONWriter writer, Object value, ObjectSerializer[] subSerializer, int i) {
        writer.writeValue(getElement(value).asXML());
    }

    protected Element getElement(Object value) {
        return (Element) value;
    }

    public Class<?> getSerializedClass() {
        return Element.class;
    }

    public byte getBsonSuffix() {
        return 0x02;
    }

}
