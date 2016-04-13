package com.meidusa.venus.io;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.StreamCorruptedException;

public class VenusObjectOutputStream extends ObjectOutputStream {
    private VenusByteArrayOutputStream vis;

    public VenusObjectOutputStream() throws IOException {
        this(new VenusByteArrayOutputStream(512));
    }

    private VenusObjectOutputStream(OutputStream vis) throws IOException {
        super(vis);
        this.vis = (VenusByteArrayOutputStream) vis;
    }

    protected void writeStreamHeader() throws IOException, StreamCorruptedException {

    }

    public byte[] getBuffer() {
        return vis.toByteArray();
    }

    public void reset() throws IOException {
        super.reset();
        vis.reset();

    }

    /*
     * protected void writeClassDescriptor(ObjectStreamClass desc) throws IOException {
     * this.writeLong(Serialver.computeDefaultSUID(desc.forClass())); this.writeInt(0); this.flush(); int sizePosition =
     * vis.size(); this.writeUTF(desc.forClass().getName()); super.writeClassDescriptor(desc); this.flush(); int current
     * = vis.size(); Bits.putInt(vis.getContent(), sizePosition-4, current - sizePosition); }
     */
}
