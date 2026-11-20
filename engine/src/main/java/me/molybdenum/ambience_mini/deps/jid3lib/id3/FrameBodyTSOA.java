package me.molybdenum.ambience_mini.deps.jid3lib.id3;

import me.molybdenum.ambience_mini.deps.jid3lib.InvalidTagException;
import me.molybdenum.ambience_mini.deps.jid3lib.accessors.RandomAccessor;

/**
 * &nbsp;&nbsp; The 'Album sort order' frame defines a string which should be used<br> &nbsp;&nbsp; instead of the album
 * name (TALB) for sorting purposes. E.g. an album<br> &nbsp;&nbsp; named &quot;A Soundtrack&quot; might preferably be
 * sorted as &quot;Soundtrack&quot;.</p>
 *
 * @author Eric Farng
 * @version $Revision: 1.4 $
 */
public class FrameBodyTSOA extends AbstractFrameBodyTextInformation {

    /**
     * Creates a new FrameBodyTSOA object.
     */
    public FrameBodyTSOA() {
        super();
    }

    /**
     * Creates a new FrameBodyTSOA object.
     */
    public FrameBodyTSOA(final FrameBodyTSOA body) {
        super(body);
    }

    /**
     * Creates a new FrameBodyTSOA object.
     */
    public FrameBodyTSOA(final byte textEncoding, final String text) {
        super(textEncoding, text);
    }

    /**
     * Creates a new FrameBodyTSOA object.
     */
    public FrameBodyTSOA(final RandomAccessor file) throws java.io.IOException, InvalidTagException {
        super(file);
    }

    public String getIdentifier() {
        return "TSOA";
    }
}