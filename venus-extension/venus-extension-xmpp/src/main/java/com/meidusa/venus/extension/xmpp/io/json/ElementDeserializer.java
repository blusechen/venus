package com.meidusa.venus.extension.xmpp.io.json;

import java.io.StringReader;
import java.lang.reflect.Type;

import org.dom4j.Document;

import com.meidusa.fastjson.parser.DefaultJSONParser;
import com.meidusa.fastjson.parser.JSONToken;
import com.meidusa.fastjson.parser.deserializer.ObjectDeserializer;
import com.meidusa.venus.extension.xmpp.XMPPPacketReader;

public class ElementDeserializer implements ObjectDeserializer {

    private static ThreadLocal<XMPPPacketReader> xmppReadThreadLocal = new ThreadLocal<XMPPPacketReader>() {
        protected XMPPPacketReader initialValue() {
            return new XMPPPacketReader();
        }
    };

    @SuppressWarnings("unchecked")
    public <T> T deserialze(DefaultJSONParser parser, Type type,Object fieldName) {
        String id = (String) parser.parse();

        if (id == null) {
            return null;
        }

        try {
            XMPPPacketReader reader = xmppReadThreadLocal.get();
            Document doc = reader.read(new StringReader(id));
            return (T) doc.getRootElement();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public int getFastMatchToken() {
        return JSONToken.LITERAL_STRING;
    }

}
