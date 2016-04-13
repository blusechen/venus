package com.meidusa.venus.io.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class Input {
    private ByteArrayOutputStream baos = new ByteArrayOutputStream();

    public Input(InputStream in) {
        _in = in;
    }

    public boolean hasRemaining() {
        try {
            return _in.available() > 0;
        } catch (IOException e) {
            return false;
        }
    }

    public int readInt() throws IOException {
        return Bits.readInt(_in);
    }

    public long readLong() throws IOException {
        return Bits.readLong(_in);
    }

    public double readDouble() throws IOException {
        return Double.longBitsToDouble(readLong());
    }

    public byte[] readBinary() throws IOException {
        int size = Bits.readInt(_in);
        _in.read();
        byte[] bts = new byte[size];
        _in.read(bts);
        return bts;
    }

    public byte[] readBytes() throws IOException {
        int size = Bits.readInt(_in);
        byte[] bts = new byte[size];
        _in.read(bts);
        return bts;
    }

    public long skip(int length) throws IOException {
        return _in.skip(length);
    }

    public byte read() throws IOException {
        return (byte) (_in.read() & 0xFF);
    }

    public void fill(byte b[]) throws IOException {
        fill(b, b.length);
    }

    public void fill(byte b[], int len) throws IOException {
        int off = 0;
        while (len > 0) {
            int x = _in.read(b, off, len);
            off += x;
            len -= x;
        }
    }

    public String readCStr() throws IOException {
        baos.reset();

        while (true) {
            byte b = read();
            if (b == 0)
                break;
            baos.write(b);
        }

        try {
            return new String(baos.toByteArray(), "UTF-8");
        } catch (UnsupportedOperationException e) {
            throw new RuntimeException("impossible", e);
        }
    }

    public String readString() throws IOException {
        int lenght = readInt();

        byte[] content = new byte[lenght - 1];
        this.fill(content);
        String out = null;
        try {
            out = new String(content, "UTF-8");
        } catch (UnsupportedOperationException e) {
            throw new RuntimeException("impossible", e);
        }
        this.read();// skip 0x00
        return out;
    }

    final InputStream _in;
}
