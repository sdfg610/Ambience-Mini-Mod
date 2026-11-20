package me.molybdenum.ambience_mini.deps.jid3lib.id3;

import me.molybdenum.ambience_mini.deps.jid3lib.InvalidTagException;
import me.molybdenum.ambience_mini.deps.jid3lib.accessors.RandomAccessor;

/**
 * &nbsp;&nbsp; The 'Title/Songname/Content description' frame is the actual name of<br> &nbsp;&nbsp; the piece (e.g.
 * &quot;Adagio&quot;, &quot;Hurricane Donna&quot;).</p>
 *
 * @author Eric Farng
 * @version $Revision: 1.4 $
 */
public class FrameBodyTIT2 extends AbstractFrameBodyTextInformation {

    /**
     * Creates a new FrameBodyTIT2 object.
     */
    public FrameBodyTIT2() {
        super();
    }

    /**
     * Creates a new FrameBodyTIT2 object.
     */
    public FrameBodyTIT2(final FrameBodyTIT2 body) {
        super(body);
    }

    /**
     * Creates a new FrameBodyTIT2 object.
     */
    public FrameBodyTIT2(final byte textEncoding, final String text) {
        super(textEncoding, text);
    }

    /**
     * Creates a new FrameBodyTIT2 object.
     */
    public FrameBodyTIT2(final RandomAccessor file) throws java.io.IOException, InvalidTagException {
        super(file);
    }

    public String getIdentifier() {
        return "TIT2";
    }
}