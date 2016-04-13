package com.meidusa.venus.io;

import java.io.ByteArrayOutputStream;

public class VenusByteArrayOutputStream extends ByteArrayOutputStream {

    public VenusByteArrayOutputStream() {
    }

    public VenusByteArrayOutputStream(int size) {
        super(size);
    }

    public byte[] getContent() {
        return this.buf;
    }
}
