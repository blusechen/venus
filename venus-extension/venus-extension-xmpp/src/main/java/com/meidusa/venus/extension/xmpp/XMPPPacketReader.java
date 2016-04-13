/*
 * Copyright 2001-2004 (C) MetaStuff, Ltd. All Rights Reserved.
 *
 * This software is open source.
 * See the bottom of this file for the licence.
 *
 * $Id: XMPPPacketReader.java 3190 2005-12-12 15:00:46Z gato $
 */

package com.meidusa.venus.extension.xmpp;

import java.io.IOException;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentFactory;
import org.dom4j.Element;
import org.dom4j.QName;
import org.dom4j.io.XPP3Reader;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class XMPPPacketReader extends XPP3Reader {

    public XMPPPacketReader() {
    }

    public XMPPPacketReader(DocumentFactory factory) {
        super(factory);
    }

    // Implementation methods
    // -------------------------------------------------------------------------
    public Document parseDocument() throws DocumentException, IOException, XmlPullParserException {
        DocumentFactory df = getDocumentFactory();
        Document document = df.createDocument();
        Element parent = null;
        XmlPullParser pp = getXPPParser();
        int count = 0;
        while (true) {
            int type = -1;
            type = pp.nextToken();
            switch (type) {
                case XmlPullParser.PROCESSING_INSTRUCTION: {
                    String text = pp.getText();
                    int loc = text.indexOf(" ");
                    if (loc >= 0) {
                        document.addProcessingInstruction(text.substring(0, loc), text.substring(loc + 1));
                    } else {
                        document.addProcessingInstruction(text, "");
                    }
                    break;
                }
                case XmlPullParser.COMMENT: {
                    if (parent != null) {
                        parent.addComment(pp.getText());
                    } else {
                        document.addComment(pp.getText());
                    }
                    break;
                }
                case XmlPullParser.CDSECT: {
                    String text = pp.getText();
                    if (parent != null) {
                        parent.addCDATA(text);
                    } else {
                        if (text.trim().length() > 0) {
                            throw new DocumentException("Cannot have text content outside of the root document");
                        }
                    }
                    break;

                }
                case XmlPullParser.ENTITY_REF: {
                    String text = pp.getText();
                    if (parent != null) {
                        parent.addText(text);
                    } else {
                        if (text.trim().length() > 0) {
                            throw new DocumentException("Cannot have an entityref outside of the root document");
                        }
                    }
                    break;
                }
                case XmlPullParser.END_DOCUMENT: {
                    return document;
                }
                case XmlPullParser.START_TAG: {
                    QName qname = (pp.getPrefix() == null) ? df.createQName(pp.getName(), pp.getNamespace()) : df.createQName(pp.getName(), pp.getPrefix(),
                            pp.getNamespace());
                    Element newElement = null;
                    // Do not include the namespace if this is the start tag of a new packet
                    // This avoids including "jabber:client", "jabber:server" or
                    // "jabber:component:accept"
                    if ("jabber:client".equals(qname.getNamespaceURI()) || "jabber:server".equals(qname.getNamespaceURI())
                            || "jabber:connectionmanager".equals(qname.getNamespaceURI()) || "jabber:component:accept".equals(qname.getNamespaceURI())
                            || "http://jabber.org/protocol/httpbind".equals(qname.getNamespaceURI())) {
                        newElement = df.createElement(pp.getName());
                    } else {
                        newElement = df.createElement(qname);
                    }
                    int nsStart = pp.getNamespaceCount(pp.getDepth() - 1);
                    int nsEnd = pp.getNamespaceCount(pp.getDepth());
                    for (int i = nsStart; i < nsEnd; i++) {
                        if (pp.getNamespacePrefix(i) != null) {
                            newElement.addNamespace(pp.getNamespacePrefix(i), pp.getNamespaceUri(i));
                        }
                    }
                    for (int i = 0; i < pp.getAttributeCount(); i++) {
                        QName qa = (pp.getAttributePrefix(i) == null) ? df.createQName(pp.getAttributeName(i)) : df.createQName(pp.getAttributeName(i),
                                pp.getAttributePrefix(i), pp.getAttributeNamespace(i));
                        newElement.addAttribute(qa, pp.getAttributeValue(i));
                    }
                    if (parent != null) {
                        parent.add(newElement);
                    } else {
                        document.add(newElement);
                    }
                    parent = newElement;
                    count++;
                    break;
                }
                case XmlPullParser.END_TAG: {
                    if (parent != null) {
                        parent = parent.getParent();
                    }
                    count--;
                    if (count < 1) {
                        // Update the last time a Document was received
                        return document;
                    }
                    break;
                }
                case XmlPullParser.TEXT: {
                    String text = pp.getText();
                    if (parent != null) {
                        parent.addText(text);
                    } else {
                        if (text.trim().length() > 0) {
                            throw new DocumentException("Cannot have text content outside of the root document");
                        }
                    }
                    break;
                }
                default: {
                    ;
                }
            }
        }
    }

}
