/**
 * 
 */
package com.meidusa.venus.backend.view;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URL;

import org.exolab.castor.mapping.Mapping;
import org.exolab.castor.mapping.MappingException;
import org.exolab.castor.xml.Marshaller;
import org.exolab.castor.xml.XMLContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * 
 * @author Sun Ning
 * @since 2010-3-9
 */
public class CastorXMLSerializer extends AbstractXMLSerializer {

    private static Logger logger = LoggerFactory.getLogger(CastorXMLSerializer.class);
    public static final String configFile = "/castor-mapping.xml";
    private static XMLContext context;

    static {
        context = new XMLContext();

        Mapping mapping = new Mapping();

        // castor mapping required
        try {
            URL mappingUrl = CastorXMLSerializer.class.getResource(configFile);
            if (mappingUrl != null) {
                mapping.loadMapping(mappingUrl);
                context.addMapping(mapping);
            }
        } catch (MappingException e) {
            logger.debug("", e);
        } catch (IOException e) {
            logger.debug("", e);
        }

    }

    /*
     * (non-Javadoc)
     * @see com.meidusa.relation.servicegate.view.AbstractXMLSerializer#serialize(java .lang.Object)
     */
    @Override
    public String serialize(Object o) throws Exception {
        StringWriter writer = new StringWriter();

        // which is not thread safe
        Marshaller castorMarshaller = getCongiuredMashaller(writer);
        castorMarshaller.marshal(o);
        String data = writer.toString();
        writer.close();
        return data;
    }

    public static Marshaller getCongiuredMashaller(Writer writer) throws IOException {
        Marshaller castorMarshaller = context.createMarshaller();

        castorMarshaller.setWriter(writer);
        // castorMarshaller.setMarshalExtendedType(false);
        castorMarshaller.setSuppressNamespaces(true);
        castorMarshaller.setSuppressXSIType(true);
        // which is not generic
        // castorMarshaller.setNamespaceMapping("rel",
        // "http://www.meidusa.com/rel");

        return castorMarshaller;
    }
}
