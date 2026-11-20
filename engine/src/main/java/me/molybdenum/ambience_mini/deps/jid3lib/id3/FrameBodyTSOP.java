package me.molybdenum.ambience_mini.deps.jid3lib.id3;

import me.molybdenum.ambience_mini.deps.jid3lib.InvalidTagException;
import me.molybdenum.ambience_mini.deps.jid3lib.accessors.RandomAccessor;

/**
 * &nbsp;&nbsp; The 'Performer sort order' frame defines a string which should be<br> &nbsp;&nbsp; used instead of the
 * performer (TPE2) for sorting purposes.</p>
 *
 * @author Eric Farng
 * @version $Revision: 1.4 $
 */
public class FrameBodyTSOP extends AbstractFrameBodyTextInformation {

    /**
     * Creates a new FrameBodyTSOP object.
     */
    public FrameBodyTSOP() {
        super();
    }

    /**
     * Creates a new FrameBodyTSOP object.
     */
    public FrameBodyTSOP(final FrameBodyTSOP body) {
        super(body);
    }

    /**
     * Creates a new FrameBodyTSOP object.
     */
    public FrameBodyTSOP(final byte textEncoding, final String text) {
        super(textEncoding, text);
    }

    /**
     * Creates a new FrameBodyTSOP object.
     */
    public FrameBodyTSOP(final RandomAccessor file) throws java.io.IOException, InvalidTagException {
        super(file);
    }

    public String getIdentifier() {
        return "TSOP";
    }
}