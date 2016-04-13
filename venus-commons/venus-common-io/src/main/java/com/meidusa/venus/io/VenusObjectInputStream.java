package com.meidusa.venus.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectStreamClass;
import java.io.StreamCorruptedException;
import java.util.HashMap;
import java.util.Map;

public class VenusObjectInputStream extends ObjectInputStream {
    private VenusByteArrayInputStream vis;
    Map<Long, ObjectStreamClass> map = new HashMap();

    public VenusObjectInputStream() throws IOException {
        this(new VenusByteArrayInputStream());
    }

    private VenusObjectInputStream(InputStream vis) throws IOException {
        super(vis);
        this.vis = (VenusByteArrayInputStream) vis;
    }

    protected void readStreamHeader() throws IOException, StreamCorruptedException {

    }

    public void setBuffer(byte[] buf) {
        vis.setBuffer(buf);
    }

    public void reset() throws IOException {
        vis.reset();
        this.close();
    }

    /*
     * protected ObjectStreamClass readClassDescriptor() throws IOException, ClassNotFoundException { long suid =
     * this.readLong(); int length = this.readInt(); int count = this.available(); String name = this.readUTF(); int
     * nextCount = this.available(); long id = suid; ObjectStreamClass clzz = map.get(id); if(clzz == null){ clzz =
     * super.readClassDescriptor(); map.put(id, clzz); }else{ this.skipBytes(length - (count - nextCount)); } return
     * clzz; }
     */
}
