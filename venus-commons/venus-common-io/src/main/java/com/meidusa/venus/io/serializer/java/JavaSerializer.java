package com.meidusa.venus.io.serializer.java;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Type;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.meidusa.venus.io.packet.ServicePacketBuffer;
import com.meidusa.venus.io.serializer.Serializer;

public class JavaSerializer implements Serializer {
    private static Logger logger = LoggerFactory.getLogger(JavaSerializer.class);

    private byte[] toByts(Object obj) {
        byte[] bytes = null;
        ByteArrayOutputStream aos = new ByteArrayOutputStream();
        ObjectOutputStream oo = null;
        try {
            oo = new ObjectOutputStream(aos);
            oo.writeObject(obj);
            bytes = aos.toByteArray();

        } catch (IOException e) {
            logger.error("tobyts error", e);
        } finally {
            if (oo != null) {
                try {
                    oo.close();
                } catch (IOException e) {
                }
            }
        }
        return bytes;

    }

    public Object readObject(ServicePacketBuffer buffer) {
        if (!buffer.hasRemaining()) {
            return null;
        }
        int i = buffer.readInt();
        if (i <= 0 || i > buffer.remaining()) {
            return null;
        } else {
            byte[] toByts = new byte[i];
            buffer.readBytes(toByts, 0, i);
            try {
                return toObject(toByts);
            } catch (Exception e) {
                logger.error("toObject error", e);
            }
            return null;
        }

    }

    public void writeObject(ServicePacketBuffer buffer, Object obj) {
        byte[] toByts;
        try {
            toByts = toByts(obj);
            buffer.writeInt(toByts.length);
            buffer.writeBytes(toByts);
        } catch (Exception e) {
            logger.error("writeObject error", e);
        }

    }

    public Object decode(ServicePacketBuffer buffer, Type type) {
        return readObject(buffer);
    }

    public void encode(ServicePacketBuffer buffer, Object obj) {
        writeObject(buffer, obj);
    }

    private static java.lang.Object toObject(byte[] bytes) throws Exception {
        ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
        ObjectInputStream ois = new ObjectInputStream(bis);
        try {
            Object obj = ois.readObject();
            return obj;
        } finally {
            try {
                ois.close();
            } catch (Exception e) {
            }
        }
    }

    /*
     * private static java.lang.Object toObject(byte[] bytes) throws Exception { VenusObjectInputStream os =
     * getObjectInputStream(); try{ os.setBuffer(bytes); Object obj = os.readObject(); return obj; }finally{ try{
     * os.reset(); }catch(Exception e){} } }
     */

    @SuppressWarnings("unchecked")
    public Map<String, Object> decode(ServicePacketBuffer buffer, Map<String, Type> typeMap) {
        if (buffer.hasRemaining()) {
            return (Map<String, Object>) readObject(buffer);
        }
        return null;
    }

    @Override
    public byte[] encode(Object obj) {
        return toByts(obj);
    }

    @Override
    public Object decode(byte[] bts, Type type) {
        try {
            return toObject(bts);
        } catch (Exception e) {
            logger.error("toObject error", e);
        }
        return null;
    }

    @Override
    public Map<String, Object> decode(byte[] bts, Map<String, Type> typeMap) {
        try {
            return (Map<String, Object>) toObject(bts);
        } catch (Exception e) {
            logger.error("toObject error", e);
        }
        return null;
    }
}
