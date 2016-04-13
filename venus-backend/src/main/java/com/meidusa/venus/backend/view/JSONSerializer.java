/**
 * 
 */
package com.meidusa.venus.backend.view;

import org.codehaus.jackson.map.ObjectMapper;

/**
 * 
 * @author Sun Ning
 * @since 2010-3-9
 */
public class JSONSerializer implements Serializer {

    private ObjectMapper objectMapper;

    public JSONSerializer() {
        objectMapper = ObjectMapperFactory.getNullableObjectMapper();
        // objectMapper = new ObjectMapper();
    }

    /*
     * (non-Javadoc)
     * @see com.meidusa.relation.servicegate.serializer.Serializer#serialize(java.lang.Object)
     */
    @Override
    public String serialize(Object o) throws Exception {
        return objectMapper.writeValueAsString(o);
    }

}
