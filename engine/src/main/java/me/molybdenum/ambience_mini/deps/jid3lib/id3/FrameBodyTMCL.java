package me.molybdenum.ambience_mini.deps.jid3lib.id3;

import me.molybdenum.ambience_mini.deps.jid3lib.InvalidTagException;
import me.molybdenum.ambience_mini.deps.jid3lib.accessors.RandomAccessor;

/**
 * &nbsp;&nbsp; The 'Musician credits list' is intended as a mapping between<br> &nbsp;&nbsp; instruments and the
 * musician that played it. Every odd field is an<br> &nbsp;&nbsp; instrument and every even is an artist or a comma
 * delimited list of<br>
 * <p/>
 * &nbsp;&nbsp; artists.</p>
 *
 * @author Eric Farng
 * @version $Revision: 1.4 $
 */
public class FrameBodyTMCL extends AbstractFrameBodyTextInformation {

    /**
     * Creates a new FrameBodyTMCL object.
     */
    public FrameBodyTMCL() {
        super();
    }

    /**
     * Creates a new FrameBodyTMCL object.
     */
    public FrameBodyTMCL(final FrameBodyTMCL body) {
        super(body);
    }

    /**
     * Creates a new FrameBodyTMCL object.
     */
    public FrameBodyTMCL(final byte textEncoding, final String text) {
        super(textEncoding, text);
    }

    /**
     * Creates a new FrameBodyTMCL object.
     */
    public FrameBodyTMCL(final RandomAccessor file) throws java.io.IOException, InvalidTagException {
        super(file);
    }

    public String getIdentifier() {
        return "TMCL";
    }
}