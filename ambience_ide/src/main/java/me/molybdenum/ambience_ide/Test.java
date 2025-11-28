package me.molybdenum.ambience_ide;

import me.molybdenum.ambience_mini.engine.Common;
import org.teavm.jso.dom.html.HTMLDocument;

public class Test
{
    public static void main(String[] args) {
        var document = HTMLDocument.current();
        var div = document.createElement("div");
        div.appendChild(document.createTextNode("TeaVM generated element + " + Common.MOD_ID + " + " + String.join(", ", args)));
        document.getBody().appendChild(div);
    }
}
