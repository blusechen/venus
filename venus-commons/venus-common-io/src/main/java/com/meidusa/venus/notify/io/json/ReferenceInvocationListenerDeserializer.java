package com.meidusa.venus.notify.io.json;

import java.lang.reflect.Type;

import com.meidusa.fastjson.parser.DefaultJSONParser;
import com.meidusa.fastjson.parser.JSONLexer;
import com.meidusa.fastjson.parser.JSONToken;
import com.meidusa.fastjson.parser.deserializer.ObjectDeserializer;
import com.meidusa.venus.notify.ReferenceInvocationListener;

public class ReferenceInvocationListenerDeserializer implements ObjectDeserializer {

    public final static ReferenceInvocationListenerDeserializer instance = new ReferenceInvocationListenerDeserializer();

    @SuppressWarnings("unchecked")
    public <T> T deserialze(DefaultJSONParser parser, Type type,Object fieldName) {
        final JSONLexer lexer = parser.getLexer();
        if (lexer.token() == JSONToken.NULL) {
            lexer.nextToken(JSONToken.COMMA);
            return null;
        }

        ReferenceInvocationListener object = new ReferenceInvocationListener();
        parser.parseObject(object);
        return (T) object;
    }

    public int getFastMatchToken() {
        return JSONToken.LBRACE;
    }
}
