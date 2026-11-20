package me.molybdenum.ambience_mini.deps.jid3lib.id3;

import me.molybdenum.ambience_mini.deps.jid3lib.InvalidTagException;
import me.molybdenum.ambience_mini.deps.jid3lib.accessors.RandomAccessor;

/**
 * &nbsp;&nbsp; The 'Mood' frame is intended to reflect the mood of the audio with a<br> &nbsp;&nbsp; few keywords, e.g.
 * &quot;Romantic&quot; or &quot;Sad&quot;.<br>
 *
 * @author Eric Farng
 * @version $Revision: 1.4 $
 */
public class FrameBodyTMOO extends AbstractFrameBodyTextInformation {

    /**
     * Creates a new FrameBodyTMOO object.
     */
    public FrameBodyTMOO() {
        super();
    }

    /**
     * Creates a new FrameBodyTMOO object.
     */
    public FrameBodyTMOO(final FrameBodyTMOO body) {
        super(body);
    }

    /**
     * Creates a new FrameBodyTMOO object.
     */
    public FrameBodyTMOO(final byte textEncoding, final String text) {
        super(textEncoding, text);
    }

    /**
     * Creates a new FrameBodyTMOO object.
     */
    public FrameBodyTMOO(final RandomAccessor file) throws java.io.IOException, InvalidTagException {
        super(file);
    }

    public String getIdentifier() {
        return "TMOO";
    }
}