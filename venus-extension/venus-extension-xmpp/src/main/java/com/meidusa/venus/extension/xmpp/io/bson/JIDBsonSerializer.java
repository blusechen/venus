package com.meidusa.venus.extension.xmpp.io.bson;

import org.xmpp.packet.JID;

import com.meidusa.fastbson.parse.BSONScanner;
import com.meidusa.fastbson.parse.BSONWriter;
import com.meidusa.fastbson.serializer.ObjectSerializer;

public class JIDBsonSerializer implements ObjectSerializer {

    public Object deserialize(BSONScanner scanner, ObjectSerializer[] subSerializer, int i) {
        String id = scanner.readString();
        return new JID(id);
    }

    public void serialize(BSONWriter writer, Object value, ObjectSerializer[] subSerializer, int i) {
        writer.writeValue(value.toString());
    }

    public Class<?> getSerializedClass() {
        return JID.class;
    }

    public byte getBsonSuffix() {
        return 0x02;
    }

}
