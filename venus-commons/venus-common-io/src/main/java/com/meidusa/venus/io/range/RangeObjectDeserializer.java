package com.meidusa.venus.io.range;

import java.lang.reflect.Type;

import com.meidusa.fastjson.parser.DefaultJSONParser;
import com.meidusa.fastjson.parser.JSONToken;
import com.meidusa.fastjson.parser.deserializer.ObjectDeserializer;
import com.meidusa.venus.util.RangeUtil;

public class RangeObjectDeserializer implements ObjectDeserializer {

    @Override
    public <T> T deserialze(DefaultJSONParser parser, Type type,Object fieldName) {
        String id = (String) parser.parse();

        if (id == null) {
            return null;
        } else {
            return (T) RangeUtil.getVersionRange(id);
        }
    }

    public int getFastMatchToken() {
        return JSONToken.LBRACE;
    }

}
