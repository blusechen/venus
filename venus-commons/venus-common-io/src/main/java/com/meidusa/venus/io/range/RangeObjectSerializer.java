package com.meidusa.venus.io.range;

import java.io.IOException;
import java.lang.reflect.Type;

import com.meidusa.fastjson.serializer.JSONSerializer;
import com.meidusa.fastjson.serializer.ObjectSerializer;
import com.meidusa.venus.util.Range;

public class RangeObjectSerializer implements ObjectSerializer {

	public void write(JSONSerializer serializer, Object object,
			Object fieldName, Type fieldType, int features) throws IOException {
		if (object == null) {
			serializer.writeNull();
			return;
		}

		Range range = (Range) object;

		serializer.write(range.toString());
	}

}
