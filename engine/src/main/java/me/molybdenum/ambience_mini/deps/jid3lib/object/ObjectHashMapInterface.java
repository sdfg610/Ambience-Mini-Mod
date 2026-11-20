package me.molybdenum.ambience_mini.deps.jid3lib.object;

import java.util.HashMap;
import java.util.Iterator;

public interface ObjectHashMapInterface {

    public HashMap getIdToString();

    public HashMap getStringToId();

    public Iterator iterator();
}