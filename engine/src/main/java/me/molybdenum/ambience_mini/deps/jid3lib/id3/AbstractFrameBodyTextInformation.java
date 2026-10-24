package me.molybdenum.ambience_mini.deps.jid3lib.id3;

import me.molybdenum.ambience_mini.deps.jid3lib.InvalidTagException;
import me.molybdenum.ambience_mini.deps.jid3lib.accessors.RandomAccessor;
import me.molybdenum.ambience_mini.deps.jid3lib.object.ObjectNumberHashMap;
import me.molybdenum.ambience_mini.deps.jid3lib.object.ObjectStringSizeTerminated;

import java.io.IOException;

/**
 * All frames starting with "T" are the same structurally and subclass from here
 *
 * @author Eric Farng
 * @version $Revision: 1.4 $
 */
public abstract class AbstractFrameBodyTextInformation extends AbstractID3v2FrameBody {

    /**
     * Creates a new FrameBodyTextInformation object.
     */
    protected AbstractFrameBodyTextInformation() {
        super();
    }

    /**
     * Creates a new AbstractFrameBodyTextInformation object.
     */
    protected AbstractFrameBodyTextInformation(final AbstractFrameBodyTextInformation body) {
        super(body);
    }

    /**
     * Creates a new FrameBodyTextInformation object.
     */
    protected AbstractFrameBodyTextInformation(final byte textEncoding, final String text) {
        super();
        setObject(ObjectNumberHashMap.TEXT_ENCODING, new Byte(textEncoding));
        setObject("Text", text);
    }

    /**
     * Creates a new FrameBodyTextInformation object.
     */
    protected AbstractFrameBodyTextInformation(final RandomAccessor file) throws IOException, InvalidTagException {
        super();
        read(file);
    }

    public String getBriefDescription() {
        return getText();
    }

    public void setText(final String text) {
        setObject("Text", text);
    }

    public String getText() {
        return (String) getObject("Text");
    }

    public void setTextEncoding(final byte textEncoding) {
        setObject(ObjectNumberHashMap.TEXT_ENCODING, new Byte(textEncoding));
    }

    public byte getTextEncoding() {
        return (byte) (long) (Long) getObject(ObjectNumberHashMap.TEXT_ENCODING);
    }

    protected void setupObjectList() {
        appendToObjectList(new ObjectNumberHashMap(ObjectNumberHashMap.TEXT_ENCODING, 1));
        appendToObjectList(new ObjectStringSizeTerminated("Text"));
    }
}