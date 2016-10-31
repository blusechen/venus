package com.meidusa.venus.io.network;

import java.io.EOFException;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.nio.ByteBuffer;

import com.meidusa.toolkit.util.MemoryParser;
import com.meidusa.toolkit.util.TimeUtil;

public abstract class AbstractBIOConnection {
    protected long _lastEvent;
    private Socket socket;

    protected boolean closePosted = false;
    protected long lastMessageSent = TimeUtil.currentTimeMillis();
    private String remoteAddress;
    private static int DEFAULT_BUFFER_SIZE = 4096;
    
    protected static int MAX_BUFFER_SIZE = 4 * 1024 * 1024;
    static {
    	int max = MemoryParser.parser(MAX_BUFFER_SIZE);
    	if(max < MAX_BUFFER_SIZE){
    		max = MAX_BUFFER_SIZE;
    	}
    	MAX_BUFFER_SIZE = max;
    }
    
    private byte[] tmp = new byte[4096];

    protected ByteBuffer _buffer = ByteBuffer.allocate(DEFAULT_BUFFER_SIZE);

    public AbstractBIOConnection(Socket socket, long createStamp) {
        _lastEvent = createStamp;
        this.socket = socket;
        try {
            remoteAddress = this.socket.getRemoteSocketAddress().toString();
        } catch (Exception e) {
            remoteAddress = "socket not connected!";
        }
        ;
    }

    public String getRemoteAddress() {
        return remoteAddress;
    }

    public void setSoTimeout(int soTimeout) throws SocketException {
        this.socket.setSoTimeout(soTimeout);

    }

    public int getSoTimeout() throws SocketException {
        return socket.getSoTimeout();
    }

    public void write(byte[] bts) throws IOException {
        try {
            socket.getOutputStream().write(bts);
        } catch (IOException e) {
            this.socket.close();
            throw e;
        }
    }

    public int getHeaderSize() {
        return 4;
    }

    protected int getPacketLength(ByteBuffer buffer, int offset) {
        if (buffer.position() < offset + getHeaderSize()) {
            return -1;
        } else {
            int length = (buffer.get(offset) & 0xff) << 24;
            length |= (buffer.get(++offset) & 0xff) << 16;
            length |= (buffer.get(++offset) & 0xff) << 8;
            length |= (buffer.get(++offset) & 0xff) << 0;
            return length;
        }
    }

    public byte[] read() throws IOException {
        int _length = -1;
        // we may already have the next frame entirely in the buffer from
        // a previous read
        try {
            // read whatever data we can from the source
            do {
                int got = this.socket.getInputStream().read(tmp);
                if (got <= 0) {
                    throw new EOFException();
                } else {
                    expandCapacity(got);
                    this._buffer.put(tmp, 0, got);
                }

                if (_length == -1) {
                    // if we didn't already have our length, see if we now
                    // have enough data to obtain it
                    if (got >= this.getHeaderSize()) {
                        _length = getPacketLength(_buffer, 0);

                        // don't let things grow without bounds
                        if (_length < -1 || _length > MAX_BUFFER_SIZE) {
                            throw new IOException("over max packet limit,current=" + _length + " , limit=" + MAX_BUFFER_SIZE
                                    + ",improved limit via set System property (-Dtoolkit.packet.max= newlimit)");
                        }

                        if (_length >= 0 && _length < this.getHeaderSize()) {
                            throw new IOException("packet error,decode full packet size=" + _length + ", but packet head need size=" + getHeaderSize());
                        }
                    }
                }

            } while (_length < 0 || _buffer.position() < _length);

            _buffer.flip();
            byte[] bts = new byte[_length];
            _buffer.get(bts);
            _buffer.clear();
            return bts;
        } finally {
            /*
             * if(completed){ calculateAverage(_length); }
             */
        }

    }

    private void expandCapacity(int needSize) {
        if (_buffer.remaining() < needSize) {
            int newSize = _buffer.capacity() << 1;

            ByteBuffer newbuf = ByteBuffer.allocate(Math.max(newSize, needSize + _buffer.position()));
            newbuf.put((ByteBuffer) _buffer.flip());
            _buffer = newbuf;
        }
    }

    public void close() throws Exception {
        socket.close();
    }

    public boolean isClosed() {
        return socket.isClosed();
    }
}
