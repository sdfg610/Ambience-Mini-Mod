package me.molybdenum.ambience_mini.deps.jid3lib.id3;

import me.molybdenum.ambience_mini.deps.jid3lib.InvalidTagException;
import me.molybdenum.ambience_mini.deps.jid3lib.accessors.RandomAccessor;

/**
 * &nbsp;&nbsp; The 'Language' frame should contain the languages of the text or<br> &nbsp;&nbsp; lyrics spoken or sung
 * in the audio. The language is represented with<br> &nbsp;&nbsp; three characters according to ISO-639-2 [ISO-639-2].
 * If more than one<br> &nbsp;&nbsp; language is used in the text their language codes should follow<br>
 * <p/>
 * &nbsp;&nbsp; according to the amount of their usage, e.g. &quot;eng&quot; $00 &quot;sve&quot; $00.</p>
 *
 * @author Eric Farng
 * @version $Revision: 1.4 $
 */
public class FrameBodyTLAN extends AbstractFrameBodyTextInformation {

    /**
     * Creates a new FrameBodyTLAN object.
     */
    public FrameBodyTLAN() {
        super();
    }

    /**
     * Creates a new FrameBodyTLAN object.
     */
    public FrameBodyTLAN(final FrameBodyTLAN body) {
        super(body);
    }

    /**
     * Creates a new FrameBodyTLAN object.
     */
    public FrameBodyTLAN(final byte textEncoding, final String text) {
        super(textEncoding, text);
    }

    /**
     * Creates a new FrameBodyTLAN object.
     */
    public FrameBodyTLAN(final RandomAccessor file) throws java.io.IOException, InvalidTagException {
        super(file);
    }

    public String getIdentifier() {
        return "TLAN";
    }

//    protected void setupObjectList() {
//        appendToObjectList(new ObjectNumberHashMap(ObjectNumberHashMap.TEXT_ENCODING, 1));
//        appendToObjectList(new ObjectStringHashMap("Language", 3));
//    }
}