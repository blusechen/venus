package com.meidusa.venus.extension.xmpp.io.bson;

import org.dom4j.Element;
import org.xmpp.forms.DataForm;

public class DataFormObjectSerializer extends ElementObjectSerializer<DataForm> {

    protected DataForm createObject(Element element) {
        if (element == null) {
            return null;
        }

        String nameSpace = null;
        if (element.getNamespace() != null) {
            nameSpace = element.getNamespace().getText();
        }

        if ("x".equals(element.getName()) && "jabber:x:data".equals(nameSpace)) {
            DataForm form = new DataForm(element);
            return form;
        }

        return null;
    }

    public Class<?> getSerializedClass() {
        return DataForm.class;
    }

}
