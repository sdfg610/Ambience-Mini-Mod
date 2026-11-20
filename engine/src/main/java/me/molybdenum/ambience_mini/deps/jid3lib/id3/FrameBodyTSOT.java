package me.molybdenum.ambience_mini.deps.jid3lib.id3;

import me.molybdenum.ambience_mini.deps.jid3lib.InvalidTagException;
import me.molybdenum.ambience_mini.deps.jid3lib.accessors.RandomAccessor;

/**
 * &nbsp;&nbsp; The 'Title sort order' frame defines a string which should be used<br> &nbsp;&nbsp; instead of the title
 * (TIT2) for sorting purposes.<br>
 *
 * @author Eric Farng
 * @version $Revision: 1.4 $
 */
public class FrameBodyTSOT extends AbstractFrameBodyTextInformation {

    /**
     * Creates a new FrameBodyTSOT object.
     */
    public FrameBodyTSOT() {
        super();
    }

    /**
     * Creates a new FrameBodyTSOT object.
     */
    public FrameBodyTSOT(final FrameBodyTSOT body) {
        super(body);
    }

    /**
     * Creates a new FrameBodyTSOT object.
     */
    public FrameBodyTSOT(final byte textEncoding, final String text) {
        super(textEncoding, text);
    }

    /**
     * Creates a new FrameBodyTSOT object.
     */
    public FrameBodyTSOT(final RandomAccessor file) throws java.io.IOException, InvalidTagException {
        super(file);
    }

    public String getIdentifier() {
        return "TSOT";
    }
}