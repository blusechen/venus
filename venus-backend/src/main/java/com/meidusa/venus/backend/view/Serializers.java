/**
 * 
 */
package com.meidusa.venus.backend.view;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Sun Ning
 * 
 */
public final class Serializers {

    private final static Map<String, Serializer> typedSerializers = new HashMap<String, Serializer>();

    static {
        typedSerializers.put(MediaTypes.APPLICATION_JSON, new JSONSerializer());
        typedSerializers.put(MediaTypes.APPLICATION_XML, new CastorXMLSerializer());
        // typedSerializers.put(MediaTypes.APPLICATION_XML, new XStreamXMLSerializer());
    }

    public static Serializer getSerializer(String type) {
        return typedSerializers.get(type);
    }

    public static Serializer getSerializer(int type) {
        if (type == MediaTypes.APPLICATION_XML_CODE) {
            return typedSerializers.get(MediaTypes.APPLICATION_XML);
        }

        if (type == MediaTypes.APPLICATION_JSON_CODE) {
            return typedSerializers.get(MediaTypes.APPLICATION_JSON);
        }

        return null;
    }
}
