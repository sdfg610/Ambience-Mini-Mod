package javazoom.jlayer_am_custom.decoder;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;

public class PushbackWrapperInputStream extends InputStream
{
    private final InputStream stream;

    protected byte[] push_buf;
    protected int push_pos;

    protected byte[] saved_push_buf;
    protected int saved_push_pos;


    public PushbackWrapperInputStream(@NotNull InputStream stream, int pushbackSize) {
        this.stream = stream;

        this.push_buf = new byte[pushbackSize];
        this.push_pos = pushbackSize;

        this.saved_push_buf = new byte[pushbackSize];
        this.saved_push_pos = pushbackSize;
    }


    @Override
    public int read() throws IOException {
        return stream.read();
    }

    @Override
    public int read(byte @NotNull [] b) throws IOException {
        return read(b, 0, b.length);
    }

    @Override
    public int read(byte @NotNull [] b, int off, int len) throws IOException {
        if (off < 0 || len < 0 || len > b.length - off) {
            throw new IndexOutOfBoundsException();
        } else if (len == 0) {
            return 0;
        }

        int avail = push_buf.length - push_pos;
        if (avail > 0) {
            if (len < avail) {
                avail = len;
            }
            System.arraycopy(push_buf, push_pos, b, off, avail);
            push_pos += avail;
            off += avail;
            len -= avail;
        }
        if (len > 0) {
            len = stream.read(b, off, len);
            if (len == -1) {
                return avail == 0 ? -1 : avail;
            }
            return avail + len;
        }
        return avail;
    }

    public void unread(byte[] b, int off, int len) throws IOException {
        if (len > push_pos) {
            throw new IOException("Push back buffer is full");
        }
        push_pos -= len;
        System.arraycopy(b, off, push_buf, push_pos, len);
    }


    @Override
    public boolean markSupported() {
        return stream.markSupported();
    }

    @Override
    public synchronized void mark(int readlimit) {
        stream.mark(readlimit);

        System.arraycopy(push_buf, 0, saved_push_buf, 0, push_buf.length);
        saved_push_pos = push_pos;
    }

    @Override
    public synchronized void reset() throws IOException {
        stream.reset();

        System.arraycopy(saved_push_buf, 0, push_buf, 0, push_buf.length);
        push_pos = saved_push_pos;
    }


    @Override
    public void close() throws IOException {
        stream.close();
    }
}
