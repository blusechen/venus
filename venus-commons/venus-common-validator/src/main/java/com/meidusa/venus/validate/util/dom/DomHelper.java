package com.meidusa.venus.validate.util.dom;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

import com.meidusa.venus.validate.exception.ValidationRuntimeException;
import com.meidusa.venus.validate.util.ClassLoaderUtil;

/**
 * Helper class for XML reading using sax.
 * 
 * @author lichencheng.daisy
 * 
 */
public class DomHelper {
    private static String VENUS_VALIDATOR_DEFINITION_DTD = "venus-validator-definition-1.0.0.dtd";
    private static String VENUS_VALIDATOR_DTD = "venus-validator-1.0.0.dtd";

    private static Logger logger = LoggerFactory.getLogger(DomHelper.class);

    public static List<Element> findChilds(Element parent, String name) {
        List<Element> retElements = new ArrayList<Element>();
        if (parent != null) {
            NodeList rootList = parent.getChildNodes();
            for (int i = 0; i < rootList.getLength(); i++) {
                Node nd = rootList.item(i);
                if (nd instanceof Element) {
                    Element e = (Element) nd;
                    if (name.equals(e.getNodeName())) {
                        retElements.add(e);
                    }
                }
            }
        }

        return retElements;
    }

    public static Document parse(InputStream is, Map<String, String> dtdMappings) {
        SAXParserFactory factory = SAXParserFactory.newInstance();
        factory.setNamespaceAware(true);
        factory.setValidating((dtdMappings != null));

        SAXParser parser = null;
        try {
            parser = factory.newSAXParser();
        } catch (Exception e) {
            throw new ValidationRuntimeException("Unable to create SAXParser", e);

        }
        DOMBuilder builder = new DOMBuilder();
        try {
            parser.parse(is, new StartHandler(builder, dtdMappings));

        } catch (Exception e) {
            throw new ValidationRuntimeException("Cannot parse input", e);
        }

        return builder.getDocument();
    }

    static public class DOMBuilder implements ContentHandler {

        /** The default transformer factory shared by all instances */
        protected static SAXTransformerFactory FACTORY;

        /** The transformer factory */
        protected SAXTransformerFactory factory;

        /** The result */
        protected DOMResult result;

        /** The parentNode */
        protected Node parentNode;

        protected ContentHandler nextHandler;

        static {
            FACTORY = (SAXTransformerFactory) TransformerFactory.newInstance();

        }

        /**
         * Construct a new instance of this DOMBuilder.
         */
        public DOMBuilder() {
            this((Node) null);
        }

        /**
         * Construct a new instance of this DOMBuilder.
         */
        public DOMBuilder(SAXTransformerFactory factory) {
            this(factory, null);
        }

        /**
         * Constructs a new instance that appends nodes to the given parent node.
         */
        public DOMBuilder(Node parentNode) {
            this(null, parentNode);
        }

        /**
         * Construct a new instance of this DOMBuilder.
         */
        public DOMBuilder(SAXTransformerFactory factory, Node parentNode) {
            this.factory = factory == null ? FACTORY : factory;
            this.parentNode = parentNode;
            setup();
        }

        /**
         * Setup this instance transformer and result objects.
         */
        private void setup() {
            try {
                TransformerHandler handler = this.factory.newTransformerHandler();
                nextHandler = handler;
                if (this.parentNode != null) {
                    this.result = new DOMResult(this.parentNode);
                } else {
                    this.result = new DOMResult();
                }
                handler.setResult(this.result);
            } catch (javax.xml.transform.TransformerException local) {
                throw new ValidationRuntimeException("Fatal-Error: Unable to get transformer handler", local);
            }
        }

        /**
         * Return the newly built Document.
         */
        public Document getDocument() {
            if (this.result == null || this.result.getNode() == null) {
                return null;
            } else if (this.result.getNode().getNodeType() == Node.DOCUMENT_NODE) {
                return (Document) this.result.getNode();
            } else {
                return this.result.getNode().getOwnerDocument();
            }
        }

        public void setDocumentLocator(Locator locator) {
            nextHandler.setDocumentLocator(locator);
        }

        public void startDocument() throws SAXException {
            nextHandler.startDocument();
        }

        public void endDocument() throws SAXException {
            nextHandler.endDocument();
        }

        public void startElement(String uri, String loc, String raw, Attributes attrs) throws SAXException {
            nextHandler.startElement(uri, loc, raw, attrs);
        }

        public void endElement(String arg0, String arg1, String arg2) throws SAXException {
            nextHandler.endElement(arg0, arg1, arg2);
        }

        public void startPrefixMapping(String arg0, String arg1) throws SAXException {
            nextHandler.startPrefixMapping(arg0, arg1);
        }

        public void endPrefixMapping(String arg0) throws SAXException {
            nextHandler.endPrefixMapping(arg0);
        }

        public void characters(char[] arg0, int arg1, int arg2) throws SAXException {
            nextHandler.characters(arg0, arg1, arg2);
        }

        public void ignorableWhitespace(char[] arg0, int arg1, int arg2) throws SAXException {
            nextHandler.ignorableWhitespace(arg0, arg1, arg2);
        }

        public void processingInstruction(String arg0, String arg1) throws SAXException {
            nextHandler.processingInstruction(arg0, arg1);
        }

        public void skippedEntity(String arg0) throws SAXException {
            nextHandler.skippedEntity(arg0);
        }
    }

    public static class StartHandler extends DefaultHandler {

        private ContentHandler nextHandler;

        /**
         * Create a filter that is chained to another handler.
         * 
         * @param next the next handler in the chain.
         */
        public StartHandler(ContentHandler next, Map<String, String> dtdMappings) {
            nextHandler = next;
        }

        @Override
        public void setDocumentLocator(Locator locator) {
            nextHandler.setDocumentLocator(locator);
        }

        @Override
        public void startDocument() throws SAXException {
            nextHandler.startDocument();
        }

        @Override
        public void endDocument() throws SAXException {
            nextHandler.endDocument();
        }

        @Override
        public void startElement(String uri, String loc, String raw, Attributes attrs) throws SAXException {
            nextHandler.startElement(uri, loc, raw, attrs);
        }

        @Override
        public void endElement(String arg0, String arg1, String arg2) throws SAXException {
            nextHandler.endElement(arg0, arg1, arg2);
        }

        @Override
        public void startPrefixMapping(String arg0, String arg1) throws SAXException {
            nextHandler.startPrefixMapping(arg0, arg1);
        }

        @Override
        public void endPrefixMapping(String arg0) throws SAXException {
            nextHandler.endPrefixMapping(arg0);
        }

        @Override
        public void characters(char[] arg0, int arg1, int arg2) throws SAXException {
            nextHandler.characters(arg0, arg1, arg2);
        }

        @Override
        public void ignorableWhitespace(char[] arg0, int arg1, int arg2) throws SAXException {
            nextHandler.ignorableWhitespace(arg0, arg1, arg2);
        }

        @Override
        public void processingInstruction(String arg0, String arg1) throws SAXException {
            nextHandler.processingInstruction(arg0, arg1);
        }

        @Override
        public void skippedEntity(String arg0) throws SAXException {
            nextHandler.skippedEntity(arg0);
        }

        @Override
        public InputSource resolveEntity(String publicId, String systemId) {
            if (systemId.endsWith(VENUS_VALIDATOR_DTD)) {
                return new InputSource(ClassLoaderUtil.getResourceAsStream(VENUS_VALIDATOR_DTD, this.getClass()));
            } else if (systemId.endsWith(VENUS_VALIDATOR_DEFINITION_DTD)) {
                return new InputSource(ClassLoaderUtil.getResourceAsStream(VENUS_VALIDATOR_DTD, this.getClass()));
            } else {
                try {
                    return new InputSource(new FileInputStream(systemId));
                } catch (FileNotFoundException e) {
                    logger.error("can't find dtd file");
                    return null;
                }
            }
        }

        @Override
        public void warning(SAXParseException exception) {
        }

        @Override
        public void error(SAXParseException exception) throws SAXException {
            logger.error(
                    exception.getMessage() + " at (" + exception.getPublicId() + ":" + exception.getLineNumber() + ":" + exception.getColumnNumber() + ")",
                    exception);
            throw exception;
        }

        @Override
        public void fatalError(SAXParseException exception) throws SAXException {
            logger.error(
                    exception.getMessage() + " at (" + exception.getPublicId() + ":" + exception.getLineNumber() + ":" + exception.getColumnNumber() + ")",
                    exception);
            throw exception;
        }
    }

}
