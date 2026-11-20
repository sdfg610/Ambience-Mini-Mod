package me.molybdenum.ambience_mini.deps.jid3lib.id3;

import me.molybdenum.ambience_mini.deps.jid3lib.TagException;
import me.molybdenum.ambience_mini.deps.jid3lib.TagNotFoundException;
import me.molybdenum.ambience_mini.deps.jid3lib.accessors.RandomAccessor;

import java.io.IOException;

/**
 * Superclass for all ID3v1 tags.
 *
 * @author $author$
 * @version $Revision: 1.3 $
 */
public abstract class AbstractID3v1 extends AbstractID3 {

    /**
     * Creates a new AbstractID3v1 object.
     */
    protected AbstractID3v1() {
        super();
    }

    /**
     * Creates a new AbstractID3v1 object.
     */
    protected AbstractID3v1(final AbstractID3v1 copyObject) {
        super(copyObject);
    }

    public void append(final RandomAccessor file) throws IOException, TagException {
        AbstractID3v1 oldTag;
        try {
            oldTag = new ID3v1_1(file);
            oldTag.append(this);
            oldTag.write(file);
        } catch (TagNotFoundException ex) {
            try {
                oldTag = new ID3v1(file);
                oldTag.append(this);
                oldTag.write(file);
            } catch (TagNotFoundException ex2) {
                write(file);
            }
        }
    }

    public void overwrite(final RandomAccessor file) throws IOException, TagException {
        AbstractID3v1 oldTag;
        try {
            oldTag = new ID3v1_1(file);
            oldTag.overwrite(this);
            oldTag.write(file);
        } catch (TagNotFoundException ex) {
            try {
                oldTag = new ID3v1(file);
                oldTag.overwrite(this);
                oldTag.write(file);
            } catch (TagNotFoundException ex2) {
                write(file);
            }
        }
    }
}