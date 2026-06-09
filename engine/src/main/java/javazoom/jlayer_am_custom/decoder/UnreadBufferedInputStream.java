package javazoom.jlayer_am_custom.decoder;

import org.jetbrains.annotations.NotNull;

import java.io.BufferedInputStream;
import java.io.InputStream;

public class UnreadBufferedInputStream extends BufferedInputStream {
    public UnreadBufferedInputStream(@NotNull InputStream in) {
        super(in);
    }

    public UnreadBufferedInputStream(@NotNull InputStream in, int size) {
        super(in, size);
    }


    public int unread(int n) {
        int unreadCount = Math.min(pos, n);
        pos -= unreadCount;
        return unreadCount;
    }
}
