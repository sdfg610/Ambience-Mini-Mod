package me.molybdenum.ambience_mini.deps.jid3lib.id3;

import me.molybdenum.ambience_mini.deps.jid3lib.InvalidTagException;
import me.molybdenum.ambience_mini.deps.jid3lib.accessors.RandomAccessor;

/**
 * &nbsp;&nbsp; The 'Content group description' frame is used if the sound belongs to<br> &nbsp;&nbsp; a larger category
 * of sounds/music. For example, classical music is<br> &nbsp;&nbsp; often sorted in different musical sections (e.g.
 * &quot;Piano Concerto&quot;,<br>
 * <p/>
 * &nbsp;&nbsp; &quot;Weather - Hurricane&quot;).</p>
 *
 * @author Eric Farng
 * @version $Revision: 1.4 $
 */
public class FrameBodyTIT1 extends AbstractFrameBodyTextInformation {

    /**
     * Creates a new FrameBodyTIT1 object.
     */
    public FrameBodyTIT1() {
        super();
    }

    /**
     * Creates a new FrameBodyTIT1 object.
     */
    public FrameBodyTIT1(final FrameBodyTIT1 body) {
        super(body);
    }

    /**
     * Creates a new FrameBodyTIT1 object.
     */
    public FrameBodyTIT1(final byte textEncoding, final String text) {
        super(textEncoding, text);
    }

    /**
     * Creates a new FrameBodyTIT1 object.
     */
    public FrameBodyTIT1(final RandomAccessor file) throws java.io.IOException, InvalidTagException {
        super(file);
    }

    public String getIdentifier() {
        return "TIT1";
    }
}