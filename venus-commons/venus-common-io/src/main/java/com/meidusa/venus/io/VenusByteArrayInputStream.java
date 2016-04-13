package com.meidusa.venus.io;

import java.io.ByteArrayInputStream;

public class VenusByteArrayInputStream extends ByteArrayInputStream {
    private static byte[] NULL = new byte[] { 0 };

    public VenusByteArrayInputStream() {
        super(NULL);
    }

    public VenusByteArrayInputStream(byte buf[]) {
        super(buf);
    }

    public void setBuffer(byte buf[]) {
        setBuffer(buf, 0, buf.length);
    }

    public void setBuffer(byte buf[], int offset, int length) {
        this.buf = buf;
        this.pos = offset;
        this.count = Math.min(offset + length, buf.length);
        this.mark = offset;
    }

    /**
     * Creates <code>VenusByteArrayInputStream</code> that uses <code>buf</code> as its buffer array. The initial value
     * of <code>pos</code> is <code>offset</code> and the initial value of <code>count</code> is the minimum of
     * <code>offset+length</code> and <code>buf.length</code>. The buffer array is not copied. The buffer's mark is set
     * to the specified offset.
     * 
     * @param buf the input buffer.
     * @param offset the offset in the buffer of the first byte to read.
     * @param length the maximum number of bytes to read from the buffer.
     */
    public VenusByteArrayInputStream(byte buf[], int offset, int length) {
        super(buf, offset, length);
    }

}
