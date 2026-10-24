package me.molybdenum.ambience_mini.deps.jid3lib.id3;

import me.molybdenum.ambience_mini.deps.jid3lib.InvalidTagException;
import me.molybdenum.ambience_mini.deps.jid3lib.accessors.RandomAccessor;

/**
 * &nbsp;&nbsp; The 'Lead artist/Lead performer/Soloist/Performing group' is<br> &nbsp;&nbsp; used for the main
 * artist.</p>
 *
 * @author Eric Farng
 * @version $Revision: 1.4 $
 */
public class FrameBodyTPE1 extends AbstractFrameBodyTextInformation {

    /**
     * Creates a new FrameBodyTPE1 object.
     */
    public FrameBodyTPE1() {
        super();
    }

    /**
     * Creates a new FrameBodyTPE1 object.
     */
    public FrameBodyTPE1(final FrameBodyTPE1 body) {
        super(body);
    }

    /**
     * Creates a new FrameBodyTPE1 object.
     */
    public FrameBodyTPE1(final byte textEncoding, final String text) {
        super(textEncoding, text);
    }

    /**
     * Creates a new FrameBodyTPE1 object.
     */
    public FrameBodyTPE1(final RandomAccessor file) throws java.io.IOException, InvalidTagException {
        super(file);
    }

    public String getIdentifier() {
        return "TPE1";
    }
}