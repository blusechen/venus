package com.meidusa.venus.extension.xmpp.io.json;

import java.lang.reflect.Type;

import org.dom4j.Element;
import org.xmpp.forms.DataForm;

import com.meidusa.fastjson.parser.DefaultExtJSONParser;

public class DataFormDeserializer extends ElementDeserializer {

    public <T> T deserialze(DefaultExtJSONParser parser, Type type) {
        Element element = super.deserialze(parser, type);

        if (element == null) {
            return null;
        }

        String nameSpace = null;
        if (element.getNamespace() != null) {
            nameSpace = element.getNamespace().getText();
        }

        if ("x".equals(element.getName()) && "jabber:x:data".equals(nameSpace)) {
            DataForm form = new DataForm(element);
            return (T) form;
        }

        return null;
    }

}
