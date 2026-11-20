package me.molybdenum.ambience_mini.deps.jid3lib.id3;

import me.molybdenum.ambience_mini.deps.jid3lib.InvalidTagException;
import me.molybdenum.ambience_mini.deps.jid3lib.accessors.RandomAccessor;

/**
 * &nbsp;&nbsp; The 'Band/Orchestra/Accompaniment' frame is used for additional<br>
 * <p/>
 * &nbsp;&nbsp; information about the performers in the recording.</p>
 *
 * @author Eric Farng
 * @version $Revision: 1.4 $
 */
public class FrameBodyTPE2 extends AbstractFrameBodyTextInformation {

    /**
     * Creates a new FrameBodyTPE2 object.
     */
    public FrameBodyTPE2() {
        super();
    }

    /**
     * Creates a new FrameBodyTPE2 object.
     */
    public FrameBodyTPE2(final FrameBodyTPE2 body) {
        super(body);
    }

    /**
     * Creates a new FrameBodyTPE2 object.
     */
    public FrameBodyTPE2(final byte textEncoding, final String text) {
        super(textEncoding, text);
    }

    /**
     * Creates a new FrameBodyTPE2 object.
     */
    public FrameBodyTPE2(final RandomAccessor file) throws java.io.IOException, InvalidTagException {
        super(file);
    }

    public String getIdentifier() {
        return "TPE2";
    }
}