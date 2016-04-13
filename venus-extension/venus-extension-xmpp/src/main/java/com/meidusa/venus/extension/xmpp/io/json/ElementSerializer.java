package com.meidusa.venus.extension.xmpp.io.json;

import java.io.IOException;

import org.dom4j.Element;

import com.meidusa.fastjson.serializer.JSONSerializer;
import com.meidusa.fastjson.serializer.ObjectSerializer;

public class ElementSerializer<T> implements ObjectSerializer {

    public void write(JSONSerializer serializer, Object object) throws IOException {
        if (object == null) {
            serializer.writeNull();
            return;
        }

        Element element = getElement((T) object);
        serializer.write(element.asXML());
    }

    protected Element getElement(T object) {
        return (Element) object;
    }
}
