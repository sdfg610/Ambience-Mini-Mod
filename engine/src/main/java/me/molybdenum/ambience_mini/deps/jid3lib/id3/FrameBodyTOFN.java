package me.molybdenum.ambience_mini.deps.jid3lib.id3;

import me.molybdenum.ambience_mini.deps.jid3lib.InvalidTagException;
import me.molybdenum.ambience_mini.deps.jid3lib.accessors.RandomAccessor;

/**
 * &nbsp;&nbsp; The 'Original filename' frame contains the preferred filename for the<br> &nbsp;&nbsp; file, since some
 * media doesn't allow the desired length of the<br>
 * <p/>
 * &nbsp;&nbsp; filename. The filename is case sensitive and includes its suffix.</p>
 *
 * @author Eric Farng
 * @version $Revision: 1.4 $
 */
public class FrameBodyTOFN extends AbstractFrameBodyTextInformation {

    /**
     * Creates a new FrameBodyTOFN object.
     */
    public FrameBodyTOFN() {
        super();
    }

    /**
     * Creates a new FrameBodyTOFN object.
     */
    public FrameBodyTOFN(final FrameBodyTOFN body) {
        super(body);
    }

    /**
     * Creates a new FrameBodyTOFN object.
     */
    public FrameBodyTOFN(final byte textEncoding, final String text) {
        super(textEncoding, text);
    }

    /**
     * Creates a new FrameBodyTOFN object.
     */
    public FrameBodyTOFN(final RandomAccessor file) throws java.io.IOException, InvalidTagException {
        super(file);
    }

    public String getIdentifier() {
        return "TOFN";
    }
}