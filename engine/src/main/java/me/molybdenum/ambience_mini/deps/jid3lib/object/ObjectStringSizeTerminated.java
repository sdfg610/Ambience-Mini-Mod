package me.molybdenum.ambience_mini.deps.jid3lib.object;

import java.nio.charset.StandardCharsets;

/**
 * ID3v2 and Lyrics3v2 tags have individual fields <code>AbstractMP3Fragment</code>s Then each fragment is broken down
 * in to individual <code>AbstractMP3Object</code>s
 *
 * @author Eric Farng
 * @version $Revision: 1.5 $
 */
public class ObjectStringSizeTerminated extends AbstractMP3Object {

    /**
     * Creates a new ObjectStringSizeTerminated object.
     */
    public ObjectStringSizeTerminated(final String identifier) {
        this.identifier = identifier;
    }

    /**
     * Creates a new ObjectStringSizeTerminated object.
     */
    public ObjectStringSizeTerminated(final ObjectStringSizeTerminated object) {
        super(object);
    }

    public int getSize() {
        final String str = writeString();
        if (str != null) {
            return str.length();
        }
        return 0;
    }

    @Override
    public void readByteArray(byte[] arr, int offset) {
        if (offset <= 0)
            super.readByteArray(arr, offset);
        byte encoding = arr[offset-1];

        this.value = switch (encoding) {
            case 0 -> new String(arr, offset, arr.length-offset, StandardCharsets.ISO_8859_1);
            case 1 -> new String(arr, offset, arr.length-offset, StandardCharsets.UTF_16);
            case 2 -> new String(arr, offset, arr.length-offset, StandardCharsets.UTF_16BE);
            case 3 -> new String(arr, offset, arr.length-offset, StandardCharsets.UTF_8);
            default -> throw new IllegalStateException("Unexpected value: " + encoding);
        };
    }

    public void readString(final String str, final int offset) {
        if (str == null) {
            throw new NullPointerException("String is null");
        }
        if ((offset < 0) || (offset >= str.length())) {
            throw new IndexOutOfBoundsException("Offset to String is out of bounds: offset = " +
                                                offset +
                                                ", string.length()" +
                                                str.length());
        }
        this.value = str.substring(offset);
    }

    public String toString() {
        return writeString();
    }

    public String writeString() {
        return (String) this.value;
    }
}