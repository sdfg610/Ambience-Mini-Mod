package me.molybdenum.ambience_mini.deps.jid3lib.id3;

import me.molybdenum.ambience_mini.deps.jid3lib.InvalidTagException;
import me.molybdenum.ambience_mini.deps.jid3lib.accessors.RandomAccessor;

/**
 * <div class=h5>TORY</div>
 * <p/>
 * <div class=t>The 'Original release year' frame is intended for the year when the original recording, if for example
 * the music in the file should be a cover of a previously released song, was released. The field is formatted as in the
 * "TYER" frame.</div>
 *
 * @author Eric Farng
 * @version $Revision: 1.4 $
 */
public class FrameBodyTORY extends AbstractFrameBodyTextInformation {

    /**
     * Creates a new FrameBodyTORY object.
     */
    public FrameBodyTORY() {
        super();
    }

    /**
     * Creates a new FrameBodyTORY object.
     */
    public FrameBodyTORY(final FrameBodyTORY body) {
        super(body);
    }

    /**
     * Creates a new FrameBodyTORY object.
     */
    public FrameBodyTORY(final byte textEncoding, final String text) {
        super(textEncoding, text);
    }

    /**
     * Creates a new FrameBodyTORY object.
     */
    public FrameBodyTORY(final RandomAccessor file) throws java.io.IOException, InvalidTagException {
        super(file);
    }

    public String getIdentifier() {
        return "TORY";
    }
}