package me.molybdenum.ambience_mini.deps.jid3lib.id3;

import me.molybdenum.ambience_mini.deps.jid3lib.InvalidTagException;
import me.molybdenum.ambience_mini.deps.jid3lib.accessors.RandomAccessor;

/**
 * &nbsp;&nbsp; The 'Publisher' frame simply contains the name of the label or<br> &nbsp;&nbsp; publisher.</p>
 *
 * @author Eric Farng
 * @version $Revision: 1.4 $
 */
public class FrameBodyTPUB extends AbstractFrameBodyTextInformation {

    /**
     * Creates a new FrameBodyTPUB object.
     */
    public FrameBodyTPUB() {
        super();
    }

    /**
     * Creates a new FrameBodyTPUB object.
     */
    public FrameBodyTPUB(final FrameBodyTPUB body) {
        super(body);
    }

    /**
     * Creates a new FrameBodyTPUB object.
     */
    public FrameBodyTPUB(final byte textEncoding, final String text) {
        super(textEncoding, text);
    }

    /**
     * Creates a new FrameBodyTPUB object.
     */
    public FrameBodyTPUB(final RandomAccessor file) throws java.io.IOException, InvalidTagException {
        super(file);
    }

    public String getIdentifier() {
        return "TPUB";
    }
}