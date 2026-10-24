package me.molybdenum.ambience_mini.deps.jid3lib.id3;

import me.molybdenum.ambience_mini.deps.jid3lib.InvalidTagException;
import me.molybdenum.ambience_mini.deps.jid3lib.accessors.RandomAccessor;

/**
 * <div class=h5>TDAT</div>
 * <p/>
 * <div class=t>The 'Date' frame is a numeric string in the DDMM format containing the date for the recording. This
 * field is always four characters long.</div>
 * <p/>
 * <p/>
 * <a name="TDLY"> </a>
 *
 * @author Eric Farng
 * @version $Revision: 1.4 $
 */
public class FrameBodyTDAT extends AbstractFrameBodyTextInformation {

    /**
     * Creates a new FrameBodyTDAT object.
     */
    public FrameBodyTDAT() {
        super();
    }

    /**
     * Creates a new FrameBodyTDAT object.
     */
    public FrameBodyTDAT(final FrameBodyTDAT body) {
        super(body);
    }

    /**
     * Creates a new FrameBodyTDAT object.
     */
    public FrameBodyTDAT(final byte textEncoding, final String text) {
        super(textEncoding, text);
    }

    /**
     * Creates a new FrameBodyTDAT object.
     */
    public FrameBodyTDAT(final RandomAccessor file) throws java.io.IOException, InvalidTagException {
        super(file);
    }

    public String getIdentifier() {
        return "TDAT";
    }
}