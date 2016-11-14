package com.meidusa.venus.extension.xmpp.io.json;

import java.lang.reflect.Type;

import org.xmpp.packet.JID;

import com.meidusa.fastjson.parser.DefaultExtJSONParser;
import com.meidusa.fastjson.parser.DefaultJSONParser;
import com.meidusa.fastjson.parser.JSONToken;
import com.meidusa.fastjson.parser.deserializer.ObjectDeserializer;

public class JIDObjectDeserializer implements ObjectDeserializer {

    public <T> T deserialze(DefaultJSONParser parser, Type type, Object fieldName){
        String id = (String) parser.parse();

        if (id == null) {
            return null;
        } else {
            return (T) new JID(id, true);
        }
    }

    public int getFastMatchToken() {
        return JSONToken.LITERAL_STRING;
    }

}
