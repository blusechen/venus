package com.meidusa.venus.benchmark.venus.packet;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.meidusa.fastjson.JSON;
import com.meidusa.fastjson.JSONObject;
import com.meidusa.fastjson.parser.DefaultExtJSONParser;
import com.meidusa.fastjson.parser.DefaultJSONParser;
import com.meidusa.venus.io.packet.AbstractServiceRequestPacket;
import com.meidusa.venus.io.packet.PacketConstant;
import com.meidusa.venus.io.packet.ServicePacketBuffer;
import com.meidusa.venus.io.serializer.bson.FastBsonSerializerWrapper;
import com.meidusa.venus.io.utils.GZipUtil;
import com.meidusa.venus.util.ParameterizedTypeImpl;

public class BsonVenusRequestPacket extends AbstractServiceRequestPacket {
    private static final long serialVersionUID = 1L;
    private static FastBsonSerializerWrapper serializer = new FastBsonSerializerWrapper();
    public String params;

    public BsonVenusRequestPacket() {
        type = PACKET_TYPE_SERVICE_REQUEST;
    }

    protected void readBody(ServicePacketBuffer buffer) {
        super.readBody(buffer);
        if (buffer.hasRemaining()) {
            byte f = (byte) (this.flags & CAPABILITY_GZIP);
            if (f == CAPABILITY_GZIP) {
                byte[] bts = buffer.readLengthCodedBytes();
                if (bts != null & bts.length > 0) {
                    bts = GZipUtil.decompress(bts);
                    Map map = (Map) serializer.decode(bts, Map.class);
                    params = new JSONObject(map).toJSONString();
                }
            } else {
                Map map = (Map) serializer.decode(buffer, Map.class);
                params = new JSONObject(map).toJSONString();
            }
        }
    }

    protected void writeBody(ServicePacketBuffer buffer) throws UnsupportedEncodingException {
        super.writeBody(buffer);
        if (!StringUtils.isEmpty(params)) {
            Map<String, Object> data = new HashMap<String, Object>();
            DefaultJSONParser parser = new DefaultExtJSONParser(params);
            parser.parseObject(data, Object.class);
            byte[] bts = serializer.encode(data);
            if (bts != null) {
                if (PacketConstant.AUTO_COMPRESS_SIZE > 0 && bts.length > PacketConstant.AUTO_COMPRESS_SIZE) {
                    buffer.writeLengthCodedBytes(GZipUtil.compress(bts));
                    this.flags = (byte) (this.flags | CAPABILITY_GZIP);
                } else {
                    buffer.writeLengthCodedBytes(bts);
                }
            } else {
                buffer.writeInt(0);
            }
        } else {
            buffer.writeInt(0);
        }
    }

    public static void main(String[] args) {
        FastBsonSerializerWrapper serializer = new FastBsonSerializerWrapper();
        ServicePacketBuffer buffer = new ServicePacketBuffer(64);
        JSONObject jsonObject = JSON.parseObject("{\"name\":\"aadsfasdf\",age:1.0}");
        Map<String, Object> data = jsonObject.getDataMap();
        serializer.encode(buffer, data);
        buffer.setPosition(0);

        Map map = (Map) serializer.decode(buffer, ParameterizedTypeImpl.make(Map.class, new Type[] { String.class, Object.class }, HashMap.class));
        System.out.println(new JSONObject(map).toJSONString());
    }
}
