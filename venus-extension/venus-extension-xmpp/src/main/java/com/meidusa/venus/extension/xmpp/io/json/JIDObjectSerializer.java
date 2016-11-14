package com.meidusa.venus.extension.xmpp.io.json;

import java.io.IOException;
import java.lang.reflect.Type;

import org.xmpp.packet.JID;

import com.meidusa.fastjson.serializer.JSONSerializer;
import com.meidusa.fastjson.serializer.ObjectSerializer;

public class JIDObjectSerializer implements ObjectSerializer {

    public void write(JSONSerializer serializer, //
            Object object, //
            Object fieldName, //
            Type fieldType, //
            int features) throws IOException {
        if (object == null) {
            serializer.writeNull();
            return;
        }

        JID jid = (JID) object;
        serializer.write(jid.toString());
    }

}
