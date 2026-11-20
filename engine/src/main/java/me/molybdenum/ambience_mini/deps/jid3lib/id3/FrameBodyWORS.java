package me.molybdenum.ambience_mini.deps.jid3lib.id3;

import me.molybdenum.ambience_mini.deps.jid3lib.InvalidTagException;
import me.molybdenum.ambience_mini.deps.jid3lib.accessors.RandomAccessor;

/**
 * &nbsp;&nbsp; The 'Official Internet radio station homepage' contains a URL<br> &nbsp;&nbsp; pointing at the homepage
 * of the internet radio station.</p>
 *
 * @author Eric Farng
 * @version $Revision: 1.4 $
 */
public class FrameBodyWORS extends AbstractFrameBodyUrlLink {

    /**
     * Creates a new FrameBodyWORS object.
     */
    public FrameBodyWORS() {
        super();
    }

    /**
     * Creates a new FrameBodyWORS object.
     */
    public FrameBodyWORS(final String urlLink) {
        super(urlLink);
    }

    /**
     * Creates a new FrameBodyWORS object.
     */
    public FrameBodyWORS(final FrameBodyWORS body) {
        super(body);
    }

    /**
     * Creates a new FrameBodyWORS object.
     */
    public FrameBodyWORS(final RandomAccessor file) throws java.io.IOException, InvalidTagException {
        super(file);
    }

    public String getIdentifier() {
        return "WORS";
    }
}