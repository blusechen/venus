/*
 * Copyright 2008-2108 amoeba.meidusa.com 
 * 
 * 	This program is free software; you can redistribute it and/or modify it under the terms of 
 * the GNU AFFERO GENERAL PUBLIC LICENSE as published by the Free Software Foundation; either version 3 of the License, 
 * or (at your option) any later version. 
 * 
 * 	This program is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * See the GNU AFFERO GENERAL PUBLIC LICENSE for more details. 
 * 	You should have received a copy of the GNU AFFERO GENERAL PUBLIC LICENSE along with this program; 
 * if not, write to the Free Software Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package com.meidusa.venus.benchmark.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.HashMap;
import java.util.InvalidPropertiesFormatException;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 */
public class ObjectMapLoader {

    // XML loading and saving methods for Properties

    // The required DTD URI for exported properties
    private static final String PROPS_DTD_URI = "http://amoeba.meidusa.com/objectMap.dtd";

    private static final String PROPS_DTD = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + "<!-- DTD for properties -->" + "<!ELEMENT bean ( property* ) >"
            + "<!ATTLIST bean class NMTOKEN #REQUIRED >" +

            "<!ELEMENT entry ( #PCDATA | bean )* >" + "<!ATTLIST entry key NMTOKEN #REQUIRED >" +

            "<!ELEMENT objectMap ( entry+ ) >" + "<!ATTLIST objectMap version NMTOKEN #REQUIRED >" +

            "<!ELEMENT property ( #PCDATA ) >" + "<!ATTLIST property name NMTOKEN #REQUIRED >";

    /**
     * Version number for the format of exported properties files.
     */

    public static void load(Map<String, Object> props, InputStream in) throws IOException, InvalidPropertiesFormatException {
        Document doc = null;
        try {
            doc = getLoadingDoc(in);
        } catch (SAXException saxe) {
            throw new InvalidPropertiesFormatException(saxe);
        }
        Element propertiesElement = (Element) doc.getChildNodes().item(1);
        importMap(props, propertiesElement);
    }

    static Document getLoadingDoc(InputStream in) throws SAXException, IOException {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setIgnoringElementContentWhitespace(true);
        dbf.setValidating(false);
        dbf.setCoalescing(true);
        dbf.setIgnoringComments(true);
        try {
            DocumentBuilder db = dbf.newDocumentBuilder();
            db.setEntityResolver(new Resolver());
            db.setErrorHandler(new EH());
            InputSource is = new InputSource(in);
            return db.parse(is);
        } catch (ParserConfigurationException x) {
            throw new Error(x);
        }
    }

    private static void importMap(Map<String, Object> props, Element propertiesElement) {
        NodeList entries = propertiesElement.getChildNodes();
        int numEntries = entries.getLength();
        int start = numEntries > 0 && entries.item(0).getNodeName().equals("comment") ? 1 : 0;
        for (int i = start; i < numEntries; i++) {
            Element entry = (Element) entries.item(i);
            if (entry.hasAttribute("key")) {
                entry.getFirstChild();
                Object val;
                try {
                    val = loadBean(entry);
                    props.put(entry.getAttribute("key"), val);
                } catch (Exception e) {
                    throw new Error(e.getMessage(), e);
                }

            }
        }
    }

    private static Object loadBean(Element keyElement) throws Exception {
        NodeList entries = keyElement.getChildNodes();
        for (int i = 0; i < entries.getLength(); i++) {
            Node node = entries.item(i);
            if (node instanceof Element) {
                Element entry = (Element) node;
                return DocumentUtil.loadBeanConfig(entry).createBeanObject(true, System.getProperties());
            }
        }
        String value = keyElement.getTextContent().trim();
        return Class.forName(value).newInstance();

    }

    private static class Resolver implements EntityResolver {
        public InputSource resolveEntity(String pid, String sid) throws SAXException {
            if (sid.equals(PROPS_DTD_URI)) {
                InputSource is;
                is = new InputSource(new StringReader(PROPS_DTD));
                is.setSystemId(PROPS_DTD_URI);
                return is;
            }
            throw new SAXException("Invalid system identifier: " + sid);
        }
    }

    private static class EH implements ErrorHandler {
        public void error(SAXParseException x) throws SAXException {
            throw x;
        }

        public void fatalError(SAXParseException x) throws SAXException {
            throw x;
        }

        public void warning(SAXParseException x) throws SAXException {
            throw x;
        }
    }

    public static void main(String[] args) throws Exception {
        HashMap<String, Object> map = new HashMap<String, Object>();
        load(map, new FileInputStream(new File("./1.xml")));
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            System.out.println("key=" + entry.getKey() + ",value=" + entry.getValue());
        }
    }

}
