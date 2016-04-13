package com.meidusa.venus.io.range;

import com.meidusa.fastbson.parse.BSONScanner;
import com.meidusa.fastbson.parse.BSONWriter;
import com.meidusa.fastbson.serializer.ObjectSerializer;
import com.meidusa.venus.util.Range;
import com.meidusa.venus.util.RangeUtil;

public class RangeBsonSerializer implements ObjectSerializer {

    public Object deserialize(BSONScanner scanner, ObjectSerializer[] subSerializer, int i) {
        String version = scanner.readString();
        return RangeUtil.getVersionRange(version);
    }

    public void serialize(BSONWriter writer, Object value, ObjectSerializer[] subSerializer, int i) {
        writer.writeValue(value.toString());
    }

    public Class<?> getSerializedClass() {
        return Range.class;
    }

    public byte getBsonSuffix() {
        return 0x02;
    }

}
