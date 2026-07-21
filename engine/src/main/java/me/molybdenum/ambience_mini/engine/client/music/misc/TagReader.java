package me.molybdenum.ambience_mini.engine.client.music.misc;

import me.molybdenum.ambience_mini.engine.shared.utils.Pair;
import org.jetbrains.annotations.Nullable;

public abstract class TagReader {
    @Nullable public abstract String getLoopStartStr();
    @Nullable public abstract String getLoopEndStr();
    @Nullable public abstract String getLoopLengthStr();

    @Nullable public abstract String getTitle();
    @Nullable public abstract String getAuthor();

    public Pair<Long, Long> getLoopStartAndEnd() {
        long loopStart, loopEnd;

        String loopStartStr = getLoopStartStr();
        if (loopStartStr == null)
            throw new RuntimeException("No 'loopstart' tag in metadata.");
        try {
            loopStart = Long.parseLong(loopStartStr);
            if (loopStart < 0)
                throw new RuntimeException("The 'loopstart' tag must be non-negative. Got: " + loopStart);
        }
        catch (Exception e) {
            throw new RuntimeException("Could not parse 'loopstart' tag with value '" + loopStartStr + "'");
        }

        String loopEndStr = getLoopEndStr();
        String loopLengthStr = getLoopLengthStr();
        if (loopEndStr != null) {
            try {
                loopEnd = Long.parseLong(loopEndStr);
                if (loopEnd <= loopStart)
                    throw new RuntimeException("The 'loopstart' tag must be less than the 'loopend' tag. Got: " + loopStart + " and " + loopEnd);
            }
            catch (Exception e) {
                throw new RuntimeException("Could not parse 'loopend' tag with value '" + loopEndStr + "'");
            }
        }
        else if (loopLengthStr != null) {
            try {
                long len = Long.parseLong(loopLengthStr);
                if (len <= 0)
                    throw new RuntimeException("The 'looplength' tag must be positive. Got: " + len);
                loopEnd = loopStart + len;
            }
            catch (Exception e) {
                throw new RuntimeException("Could not parse 'looplength' tag with value '" + loopLengthStr + "'");
            }
        }
        else
            throw new RuntimeException("No 'loopend' or 'looplength' tag in metadata.");

        return new Pair<>(loopStart, loopEnd);
    }
}
