package me.molybdenum.ambience_mini.deps.jid3lib.id3;

import me.molybdenum.ambience_mini.deps.jid3lib.InvalidTagException;
import me.molybdenum.ambience_mini.deps.jid3lib.accessors.RandomAccessor;

/**
 * <div class=h5>TYER</div>
 * <p/>
 * <div class=t>The 'Year' frame is a numeric string with a year of the recording. This frames is always four characters
 * long (until the year 10000).</div>
 *
 * @author Eric Farng
 * @version $Revision: 1.4 $
 */
public class FrameBodyTYER extends AbstractFrameBodyTextInformation {

    /**
     * Creates a new FrameBodyTYER object.
     */
    public FrameBodyTYER() {
        super();
    }

    /**
     * Creates a new FrameBodyTYER object.
     */
    public FrameBodyTYER(final FrameBodyTYER body) {
        super(body);
    }

    /**
     * Creates a new FrameBodyTYER object.
     */
    public FrameBodyTYER(final byte textEncoding, final String text) {
        super(textEncoding, text);
    }

    /**
     * Creates a new FrameBodyTYER object.
     */
    public FrameBodyTYER(final RandomAccessor file) throws java.io.IOException, InvalidTagException {
        super(file);
    }

    public String getIdentifier() {
        return "TYER";
    }
}