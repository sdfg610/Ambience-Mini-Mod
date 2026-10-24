package me.molybdenum.ambience_mini.deps.jid3lib.id3;

import me.molybdenum.ambience_mini.deps.jid3lib.InvalidTagException;
import me.molybdenum.ambience_mini.deps.jid3lib.accessors.RandomAccessor;

/**
 * &nbsp;&nbsp; The 'Publishers official webpage' frame is a URL pointing at the<br> &nbsp;&nbsp; official webpage for
 * the publisher.<br>
 *
 * @author Eric Farng
 * @version $Revision: 1.4 $
 */
public class FrameBodyWPUB extends AbstractFrameBodyUrlLink {

    /**
     * Creates a new FrameBodyWPUB object.
     */
    public FrameBodyWPUB() {
        super();
    }

    /**
     * Creates a new FrameBodyWPUB object.
     */
    public FrameBodyWPUB(final String urlLink) {
        super(urlLink);
    }

    /**
     * Creates a new FrameBodyWPUB object.
     */
    public FrameBodyWPUB(final FrameBodyWPUB body) {
        super(body);
    }

    /**
     * Creates a new FrameBodyWPUB object.
     */
    public FrameBodyWPUB(final RandomAccessor file) throws java.io.IOException, InvalidTagException {
        super(file);
    }

    public String getIdentifier() {
        return "WPUB";
    }
}