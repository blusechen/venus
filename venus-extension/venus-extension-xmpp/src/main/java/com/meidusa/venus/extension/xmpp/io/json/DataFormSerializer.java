package com.meidusa.venus.extension.xmpp.io.json;

import org.dom4j.Element;
import org.xmpp.forms.DataForm;

public class DataFormSerializer extends ElementSerializer<DataForm> {

    protected Element getElement(DataForm object) {
        return (Element) object.getElement();
    }
}
