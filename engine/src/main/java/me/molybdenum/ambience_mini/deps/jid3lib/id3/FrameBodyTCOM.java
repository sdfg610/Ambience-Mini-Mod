package me.molybdenum.ambience_mini.deps.jid3lib.id3;

import me.molybdenum.ambience_mini.deps.jid3lib.InvalidTagException;
import me.molybdenum.ambience_mini.deps.jid3lib.accessors.RandomAccessor;

/**
 * &nbsp;&nbsp; The 'Composer' frame is intended for the name of the composer.</p>
 *
 * @author Eric Farng
 * @version $Revision: 1.4 $
 */
public class FrameBodyTCOM extends AbstractFrameBodyTextInformation {

    /**
     * Creates a new FrameBodyTCOM object.
     */
    public FrameBodyTCOM() {
        super();
    }

    /**
     * Creates a new FrameBodyTCOM object.
     */
    public FrameBodyTCOM(final FrameBodyTCOM body) {
        super(body);
    }

    /**
     * Creates a new FrameBodyTCOM object.
     */
    public FrameBodyTCOM(final byte textEncoding, final String text) {
        super(textEncoding, text);
    }

    /**
     * Creates a new FrameBodyTCOM object.
     */
    public FrameBodyTCOM(final RandomAccessor file) throws java.io.IOException, InvalidTagException {
        super(file);
    }

    public String getIdentifier() {
        return "TCOM";
    }
}