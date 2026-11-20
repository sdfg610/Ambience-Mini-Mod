package me.molybdenum.ambience_mini.deps.jid3lib.id3;

import me.molybdenum.ambience_mini.deps.jid3lib.InvalidTagException;
import me.molybdenum.ambience_mini.deps.jid3lib.accessors.RandomAccessor;

/**
 * &nbsp;&nbsp; The 'ISRC' frame should contain the International Standard Recording<br> &nbsp;&nbsp; Code [ISRC] (12
 * characters).<br>
 *
 * @author Eric Farng
 * @version $Revision: 1.4 $
 */
public class FrameBodyTSRC extends AbstractFrameBodyTextInformation {

    /**
     * Creates a new FrameBodyTSRC object.
     */
    public FrameBodyTSRC() {
        super();
    }

    /**
     * Creates a new FrameBodyTSRC object.
     */
    public FrameBodyTSRC(final FrameBodyTSRC body) {
        super(body);
    }

    /**
     * Creates a new FrameBodyTSRC object.
     */
    public FrameBodyTSRC(final byte textEncoding, final String text) {
        super(textEncoding, text);
    }

    /**
     * Creates a new FrameBodyTSRC object.
     */
    public FrameBodyTSRC(final RandomAccessor file) throws java.io.IOException, InvalidTagException {
        super(file);
    }

    public String getIdentifier() {
        return "TSRC";
    }
}