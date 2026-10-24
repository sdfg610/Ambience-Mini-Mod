package me.molybdenum.ambience_mini.deps.jid3lib.id3;

import me.molybdenum.ambience_mini.deps.jid3lib.InvalidTagException;
import me.molybdenum.ambience_mini.deps.jid3lib.accessors.RandomAccessor;

/**
 * &nbsp;&nbsp; The 'Album/Movie/Show title' frame is intended for the title of the<br> &nbsp;&nbsp; recording (or
 * source of sound) from which the audio in the file is<br>
 * <p/>
 * &nbsp;&nbsp; taken.</p>
 *
 * @author Eric Farng
 * @version $Revision: 1.4 $
 */
public class FrameBodyTALB extends AbstractFrameBodyTextInformation {

    /**
     * Creates a new FrameBodyTALB object.
     */
    public FrameBodyTALB() {
        super();
    }

    /**
     * Creates a new FrameBodyTALB object.
     */
    public FrameBodyTALB(final FrameBodyTALB body) {
        super(body);
    }

    /**
     * Creates a new FrameBodyTALB object.
     */
    public FrameBodyTALB(final byte textEncoding, final String text) {
        super(textEncoding, text);
    }

    /**
     * Creates a new FrameBodyTALB object.
     */
    public FrameBodyTALB(final RandomAccessor file) throws java.io.IOException, InvalidTagException {
        super(file);
    }

    public String getIdentifier() {
        return "TALB";
    }
}