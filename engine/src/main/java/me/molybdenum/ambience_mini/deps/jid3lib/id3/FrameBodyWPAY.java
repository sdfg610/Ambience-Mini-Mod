package me.molybdenum.ambience_mini.deps.jid3lib.id3;

import me.molybdenum.ambience_mini.deps.jid3lib.InvalidTagException;
import me.molybdenum.ambience_mini.deps.jid3lib.accessors.RandomAccessor;

/**
 * &nbsp;&nbsp; The 'Payment' frame is a URL pointing at a webpage that will handle<br>
 * <p/>
 * &nbsp;&nbsp; the process of paying for this file.</p>
 *
 * @author Eric Farng
 * @version $Revision: 1.4 $
 */
public class FrameBodyWPAY extends AbstractFrameBodyUrlLink {

    /**
     * Creates a new FrameBodyWPAY object.
     */
    public FrameBodyWPAY() {
        super();
    }

    /**
     * Creates a new FrameBodyWPAY object.
     */
    public FrameBodyWPAY(final String urlLink) {
        super(urlLink);
    }

    /**
     * Creates a new FrameBodyWPAY object.
     */
    public FrameBodyWPAY(final FrameBodyWPAY body) {
        super(body);
    }

    /**
     * Creates a new FrameBodyWPAY object.
     */
    public FrameBodyWPAY(final RandomAccessor file) throws java.io.IOException, InvalidTagException {
        super(file);
    }

    public String getIdentifier() {
        return "WPAY";
    }
}