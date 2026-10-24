package me.molybdenum.ambience_mini.deps.jid3lib.lyrics3;

import me.molybdenum.ambience_mini.deps.jid3lib.InvalidTagException;
import me.molybdenum.ambience_mini.deps.jid3lib.accessors.RandomAccessor;
import me.molybdenum.ambience_mini.deps.jid3lib.object.ObjectStringSizeTerminated;

import java.io.RandomAccessFile;

/**
 * Lyrics/Music Author name.
 *
 * @author Eric Farng
 * @version $Revision: 1.4 $
 */
public class FieldBodyAUT extends AbstractLyrics3v2FieldBody {

    /**
     * Creates a new FieldBodyAUT object.
     */
    public FieldBodyAUT() {
        super();
    }

    /**
     * Creates a new FieldBodyAUT object.
     */
    public FieldBodyAUT(final FieldBodyAUT body) {
        super(body);
    }

    /**
     * Creates a new FieldBodyAUT object.
     */
    public FieldBodyAUT(final String author) {
        setObject("Author", author);
    }

    /**
     * Creates a new FieldBodyAUT object.
     */
    public FieldBodyAUT(final RandomAccessor file) throws InvalidTagException, java.io.IOException {
        this.read(file);
    }

    public void setAuthor(final String author) {
        setObject("Author", author);
    }

    public String getAuthor() {
        return (String) getObject("Author");
    }

    public String getIdentifier() {
        return "AUT";
    }

    protected void setupObjectList() {
        appendToObjectList(new ObjectStringSizeTerminated("Author"));
    }
}