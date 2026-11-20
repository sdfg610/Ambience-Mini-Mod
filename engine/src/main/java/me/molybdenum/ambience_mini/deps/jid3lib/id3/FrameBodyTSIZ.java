package me.molybdenum.ambience_mini.deps.jid3lib.id3;

import me.molybdenum.ambience_mini.deps.jid3lib.InvalidTagException;
import me.molybdenum.ambience_mini.deps.jid3lib.accessors.RandomAccessor;

/**
 * <div class=h5>TSIZ</div>
 * <p/>
 * <div class=t>The 'Size' frame contains the size of the audiofile in bytes, excluding the ID3v2 tag, represented as a
 * numeric string.</div>
 *
 * @author Eric Farng
 * @version $Revision: 1.4 $
 */
public class FrameBodyTSIZ extends AbstractFrameBodyTextInformation {

    /**
     * Creates a new FrameBodyTSIZ object.
     */
    public FrameBodyTSIZ() {
        super();
    }

    /**
     * Creates a new FrameBodyTSIZ object.
     */
    public FrameBodyTSIZ(final FrameBodyTSIZ body) {
        super(body);
    }

    /**
     * Creates a new FrameBodyTSIZ object.
     */
    public FrameBodyTSIZ(final byte textEncoding, final String text) {
        super(textEncoding, text);
    }

    /**
     * Creates a new FrameBodyTSIZ object.
     */
    public FrameBodyTSIZ(final RandomAccessor file) throws java.io.IOException, InvalidTagException {
        super(file);
    }

    public String getIdentifier() {
        return "TSIZ";
    }
}