package me.molybdenum.ambience_mini.deps.jid3lib.id3;

import me.molybdenum.ambience_mini.deps.jid3lib.InvalidTagException;
import me.molybdenum.ambience_mini.deps.jid3lib.accessors.RandomAccessor;
import me.molybdenum.ambience_mini.deps.jid3lib.object.ObjectNumberHashMap;
import me.molybdenum.ambience_mini.deps.jid3lib.object.ObjectStringDateTime;

/**
 * &nbsp;&nbsp; The 'Original release time' frame contains a timestamp describing<br>
 * <p/>
 * &nbsp;&nbsp; when the original recording of the audio was released. Timestamp<br> &nbsp;&nbsp; format is described in
 * the ID3v2 structure document [ID3v2-strct].</p>
 *
 * @author Eric Farng
 * @version $Revision: 1.4 $
 */
public class FrameBodyTDOR extends AbstractFrameBodyTextInformation {

    /**
     * Creates a new FrameBodyTDOR object.
     */
    public FrameBodyTDOR() {
        super();
    }

    /**
     * Creates a new FrameBodyTDOR object.
     */
    public FrameBodyTDOR(final FrameBodyTDOR body) {
        super(body);
    }

    /**
     * Creates a new FrameBodyTDOR object.
     */
    public FrameBodyTDOR(final byte textEncoding, final String text) {
        setObject(ObjectNumberHashMap.TEXT_ENCODING, new Byte(textEncoding));
        setObject("Date Time", text);
    }

    /**
     * Creates a new FrameBodyTDOR object.
     */
    public FrameBodyTDOR(final RandomAccessor file) throws java.io.IOException, InvalidTagException {
        super(file);
    }

    public String getIdentifier() {
        return "TDOR";
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