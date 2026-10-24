package me.molybdenum.ambience_mini.deps.jid3lib.id3;

import me.molybdenum.ambience_mini.deps.jid3lib.InvalidTagException;
import me.molybdenum.ambience_mini.deps.jid3lib.accessors.RandomAccessor;

/**
 * &nbsp;&nbsp; The 'Internet radio station name' frame contains the name of the<br> &nbsp;&nbsp; internet radio station
 * from which the audio is streamed.</p>
 *
 * @author Eric Farng
 * @version $Revision: 1.4 $
 */
public class FrameBodyTRSN extends AbstractFrameBodyTextInformation {

    /**
     * Creates a new FrameBodyTRSN object.
     */
    public FrameBodyTRSN() {
        super();
    }

    /**
     * Creates a new FrameBodyTRSN object.
     */
    public FrameBodyTRSN(final FrameBodyTRSN body) {
        super(body);
    }

    /**
     * Creates a new FrameBodyTRSN object.
     */
    public FrameBodyTRSN(final byte textEncoding, final String text) {
        super(textEncoding, text);
    }

    /**
     * Creates a new FrameBodyTRSN object.
     */
    public FrameBodyTRSN(final RandomAccessor file) throws java.io.IOException, InvalidTagException {
        super(file);
    }

    public String getIdentifier() {
        return "TRSN";
    }
}