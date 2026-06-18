package me.molybdenum.ambience_ide;

import me.molybdenum.ambience_mini.engine.client.configuration.Loader;
import me.molybdenum.ambience_mini.engine.client.configuration.Music;
import me.molybdenum.ambience_mini.engine.client.configuration.abstract_syntax.type.*;
import me.molybdenum.ambience_mini.engine.client.configuration.messages.*;
import me.molybdenum.ambience_mini.engine.client.configuration.interpreter.Interpreter;
import me.molybdenum.ambience_mini.engine.client.configuration.interpreter.selection.PlaylistSelection;
import me.molybdenum.ambience_mini.engine.client.configuration.interpreter.values.BoolVal;
import me.molybdenum.ambience_mini.engine.client.configuration.interpreter.values.Value;
import me.molybdenum.ambience_mini.engine.client.configuration.music_provider.FakeMusicProvider;
import me.molybdenum.ambience_mini.engine.client.core.providers.Event;
import me.molybdenum.ambience_mini.engine.client.core.providers.Property;
import me.molybdenum.ambience_mini.engine.shared.BuildConfig;
import me.molybdenum.ambience_mini.engine.shared.utils.Pair;
import me.molybdenum.ambience_mini.engine.shared.utils.Utils;
import org.teavm.jso.JSExport;
import org.teavm.jso.dom.html.HTMLDocument;
import org.teavm.jso.dom.html.HTMLElement;
import org.teavm.jso.dom.html.HTMLInputElement;
import org.teavm.jso.dom.xml.Node;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


public class Main
{
    private static final GameStateProviderMock provider = new GameStateProviderMock();


    @JSExport
    public static String getAmbienceMiniVersion() {
        return BuildConfig.APP_VERSION.toString();
    }


    @JSExport
    public static void loadEventsAndProperties() {
        HTMLDocument doc = HTMLDocument.current();

        var elemEvents = doc.getElementById("events");
        for (var ev : provider.getEvents())
            elemEvents.appendChild(createEventRow(doc, ev));

        var elemProperties = doc.getElementById("properties");
        for (var pr : provider.getProperties())
            elemProperties.appendChild(createPropertyRow(doc, pr));
    }


    private static Node createEventRow(HTMLDocument doc, Event ev) {
        String eventId = '@' + ev.name;

        var row = doc.createElement("tr");

        row.setAttribute("onchange", "handleEventUpdate('" + eventId + "')");
        row.appendChild(makeNameColumn(doc, eventId));
        row.appendChild(makeRadioColumn(doc, "true", false, eventId));
        row.appendChild(makeRadioColumn(doc, "false", true, eventId));
        var last = makeRadioColumn(doc, "undefined", false, eventId);
        row.appendChild(last);
        last.setClassName("absorbing-column");

        return row;
    }

    private static Node createPropertyRow(HTMLDocument doc, Property pr) {
        String propertyId = '$' + pr.name;

        var row = doc.createElement("tr");

        row.appendChild(makeNameColumn(doc, propertyId));
        var last = makeStringColumn(doc, propertyId, pr.type instanceof ListT);
        row.appendChild(last);
        last.setClassName("absorbing-column");

        return row;
    }


    private static HTMLElement makeNameColumn(HTMLDocument doc, String text) {
        var nameElem = doc.createElement("td");
        nameElem.setClassName("name");
        nameElem.setInnerText(text);
        return nameElem;
    }


    private static HTMLElement makeRadioColumn(HTMLDocument doc, String text, boolean checked, String eventId) {
        var button = doc.createElement("input");
        button.setId(eventId + "-" + text);
        button.setAttribute("name", eventId);
        button.setAttribute("type", "radio");
        if (checked)
            button.setAttribute("checked", "");

        var label = doc.createElement("label");
        label.appendChild(button);
        label.appendChild(doc.createTextNode(text));

        var column = doc.createElement("td");
        column.appendChild(label);

        return column;
    }

    @JSExport
    public static void handleEventUpdate(String eventId) {
        HTMLDocument doc = HTMLDocument.current();

        if (((HTMLInputElement)doc.getElementById(eventId + "-true")).isChecked())
            provider.setEventValue(eventId.substring(1), BoolVal.TRUE);
        else if (((HTMLInputElement)doc.getElementById(eventId + "-false")).isChecked())
            provider.setEventValue(eventId.substring(1), BoolVal.FALSE);
        else if (((HTMLInputElement)doc.getElementById(eventId + "-undefined")).isChecked())
            provider.setEventValue(eventId.substring(1), BoolVal.UNDEFINED);
    }


    private static HTMLElement makeStringColumn(HTMLDocument doc, String propertyId, boolean multiline) {
        var value = provider.getPropertyValueString(propertyId.substring(1));
        var textBox = doc.createElement(multiline ? "textarea" : "input");
        if (!multiline) {
            textBox.setAttribute("type", "text");
            textBox.setAttribute("value", value);
        }
        else {
            textBox.setAttribute("rows", Integer.toString(countLines(value)));
            textBox.setInnerText(value);
        }
        textBox.getStyle().setProperty("width", "100%");
        textBox.getStyle().setProperty("min-width", "15em");
        textBox.setAttribute("onchange", "handleStringPropertyUpdate('" + propertyId + "', this.value)");

        var column = doc.createElement("td");
        column.appendChild(textBox);

        return column;
    }

    @JSExport
    public static void handleStringPropertyUpdate(String propertyId, String value) {
        provider.setPropertyValueString(propertyId.substring(1), value);
    }

    public static int countLines(String str) {
        if(str == null || str.isEmpty())
            return 0;

        int lines = 1;
        int pos = 0;
        while ((pos = str.indexOf("\n", pos) + 1) != 0)
            lines++;
        return lines;
    }


    @JSExport
    public static void compileAndRun(String musicConfig) {
        HTMLDocument doc = HTMLDocument.current();
        var output = doc.getElementById("output");
        output.setInnerHTML("");

        var parseErrors = new ArrayList<String>();
        provider.prepare(parseErrors);
        if (!parseErrors.isEmpty()) {
            String message = "Found invalid values for some properties! Treating as 'undefined'. The errors are:\n";
            message += parseErrors.stream().map(err -> "  - " + err).collect(Collectors.joining("\n"));
            output.appendChild(makeParagraph(doc, message));
        }

        InputStream stream = new ByteArrayInputStream(musicConfig.getBytes(StandardCharsets.UTF_8));
        Loader.loadFrom(stream, new FakeMusicProvider(), provider).match(
                Main::printResult,
                Main::printErrors
        );
    }

    private static void printResult(Interpreter interpreter, List<Message> warnings) {
        HTMLDocument doc = HTMLDocument.current();
        var output = doc.getElementById("output");
        output.appendChild(makeParagraph(doc, "Configuration is valid and ran to completion!"));

        printMessages(warnings, output, doc);

        ArrayList<Pair<String, Value<?>>> trace = new ArrayList<>();
        PlaylistSelection choice = interpreter.selectPlaylist(trace);
        if (choice == null)
            output.appendChild(makeParagraph(doc, "No playlist could be selected, which means the currently playing music (if any) will continue. If you want the music to stop, make sure that the empty playlist (play [ ];) is selected."));
        else
            output.appendChild(makeParagraph(doc, String.format("Selected new playlist at priority %d: [ %s ]", choice.priority(), String.join(", ", choice.playlist().stream().map(Music::path).toList()))));

        if (!trace.isEmpty())
            output.appendChild(makeParagraph(doc, String.format("Events and properties computed during selection:\n%s", Utils.getKeyValuePairString(trace))));
    }

    private static void printErrors(List<Message> errors) {
        HTMLDocument doc = HTMLDocument.current();
        var output = doc.getElementById("output");
        output.appendChild(makeParagraph(doc, "There were errors in the configuration!"));

        printMessages(errors, output, doc);
    }

    private static void printMessages(List<Message> errors, HTMLElement output, HTMLDocument doc) {
        for (var error : errors) {
            String text;

            if (error instanceof SynError err)
                text = String.format("Syntactic error [line %d, column %d]: %s", err.line(), err.column(), err.message());
            else if (error instanceof SemError err)
                text = String.format("Semantic error [line %d]: %s", err.line(), err.message());
            else if (error instanceof SemWarning wrn)
                text = String.format("Warning [line %d]: %s", wrn.line(), wrn.message());
            else if (error instanceof ExcError err) {
                StringWriter sw = new StringWriter();
                err.exception().printStackTrace(new PrintWriter(sw));
                text = String.format("An exception occurred while loading the music configuration: %s\n%s", err.exception().getMessage(), sw);
            }
            else
                throw new RuntimeException("Could not print load-error of type: " + error.getClass().getName());

            output.appendChild(makeParagraph(doc, text));
        }
    }

    private static HTMLElement makeParagraph(HTMLDocument doc, String text) {
        var p = doc.createElement("p");
        p.setClassName("outputElement");
        p.setInnerText(text);
        return p;
    }
}
