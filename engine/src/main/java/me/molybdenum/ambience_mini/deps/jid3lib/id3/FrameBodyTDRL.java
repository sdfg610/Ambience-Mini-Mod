package me.molybdenum.ambience_mini.deps.jid3lib.id3;

import me.molybdenum.ambience_mini.deps.jid3lib.InvalidTagException;
import me.molybdenum.ambience_mini.deps.jid3lib.accessors.RandomAccessor;
import me.molybdenum.ambience_mini.deps.jid3lib.object.ObjectNumberHashMap;
import me.molybdenum.ambience_mini.deps.jid3lib.object.ObjectStringDateTime;

/**
 * &nbsp;&nbsp; The 'Release time' frame contains a timestamp describing when the<br>
 * <p/>
 * &nbsp;&nbsp; audio was first released. Timestamp format is described in the ID3v2<br> &nbsp;&nbsp; structure document
 * [ID3v2-strct].</p>
 *
 * @author Eric Farng
 * @version $Revision: 1.4 $
 */
public class FrameBodyTDRL extends AbstractFrameBodyTextInformation {

    /**
     * Creates a new FrameBodyTDRL object.
     */
    public FrameBodyTDRL() {
        super();
    }

    /**
     * Creates a new FrameBodyTDRL object.
     */
    public FrameBodyTDRL(final AbstractFrameBodyTextInformation body) {
        super(body);
    }

    /**
     * Creates a new FrameBodyTDRL object.
     */
    public FrameBodyTDRL(final byte textEncoding, final String text) {
        setObject(ObjectNumberHashMap.TEXT_ENCODING, new Byte(textEncoding));
        setObject("Date Time", text);
    }

    /**
     * Creates a new FrameBodyTDRL object.
     */
    public FrameBodyTDRL(final RandomAccessor file) throws java.io.IOException, InvalidTagException {
        super(file);
    }

    public String getIdentifier() {
        return "TDRL";
    }

    public void setText(final String text) {
        setObject("Date Time", text);
    }

    public String getText() {
        return (String) getObject("Date Time");
    }

    public void setTextEncoding(final byte textEncoding) { setObject(ObjectNumberHashMap.TEXT_ENCODING, new Byte(textEncoding)); }
    public byte getTextEncoding() { return (byte) (long) (Long) getObject(ObjectNumberHashMap.TEXT_ENCODING); }

    protected void setupObjectList() {
        appendToObjectList(new ObjectNumberHashMap(ObjectNumberHashMap.TEXT_ENCODING, 1));
        appendToObjectList(new ObjectStringDateTime("Date Time"));
    }
}