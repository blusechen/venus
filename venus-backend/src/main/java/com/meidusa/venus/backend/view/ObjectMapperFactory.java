/**
 * 
 */
package com.meidusa.venus.backend.view;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;
import org.codehaus.jackson.map.annotate.JsonSerialize;

/**
 * @author Sun Ning
 * 
 */
public class ObjectMapperFactory {

    public static ObjectMapper getNullableObjectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();

        // mapper.configure(SerializationConfig.Feature.INDENT_OUTPUT, true);
        // mapper.configure(SerializationConfig.Feature.FAIL_ON_EMPTY_BEANS, false);
        // mapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        // //mapper.configure(DeserializationConfig.Feature.USE_ANNOTATIONS, false);
        // mapper.configure(Feature.AUTO_CLOSE_SOURCE, false);
        // mapper.configure(JsonGenerator.Feature.AUTO_CLOSE_TARGET, false);

        SerializationConfig config = objectMapper.getSerializationConfig();
        config.setSerializationInclusion(JsonSerialize.Inclusion.NON_NULL);

        return objectMapper;
    }
}
