package me.molybdenum.ambience_ide;

import me.molybdenum.ambience_mini.engine.configuration.Loader;
import me.molybdenum.ambience_mini.engine.configuration.Music;
import me.molybdenum.ambience_mini.engine.configuration.abstract_syntax.type.FloatT;
import me.molybdenum.ambience_mini.engine.configuration.abstract_syntax.type.IntT;
import me.molybdenum.ambience_mini.engine.configuration.abstract_syntax.type.ListT;
import me.molybdenum.ambience_mini.engine.configuration.abstract_syntax.type.StringT;
import me.molybdenum.ambience_mini.engine.configuration.errors.ExcError;
import me.molybdenum.ambience_mini.engine.configuration.errors.LoadError;
import me.molybdenum.ambience_mini.engine.configuration.errors.SemError;
import me.molybdenum.ambience_mini.engine.configuration.errors.SynError;
import me.molybdenum.ambience_mini.engine.configuration.interpreter.Interpreter;
import me.molybdenum.ambience_mini.engine.configuration.interpreter.PlaylistChoice;
import me.molybdenum.ambience_mini.engine.configuration.interpreter.values.Value;
import me.molybdenum.ambience_mini.engine.configuration.music_provider.FakeMusicProvider;
import me.molybdenum.ambience_mini.engine.core.providers.Event;
import me.molybdenum.ambience_mini.engine.core.providers.GameStateProviderV1Mock;
import me.molybdenum.ambience_mini.engine.core.providers.Property;
import me.molybdenum.ambience_mini.engine.utils.Pair;
import me.molybdenum.ambience_mini.engine.utils.Utils;
import org.teavm.jso.JSExport;
import org.teavm.jso.dom.html.HTMLDocument;
import org.teavm.jso.dom.html.HTMLElement;
import org.teavm.jso.dom.xml.Node;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class Test
{
    private static final GameStateProviderV1Mock provider = new GameStateProviderV1Mock();


    @JSExport
    public static String getAmbienceMiniVersion() {
        return BuildConfig.APP_VERSION;
    }


    @JSExport
    public static void loadEventsAndProperties() {
        HTMLDocument doc = HTMLDocument.current();

        var elemEvents = doc.getElementById("events");
        for (var ev : provider.getEvents())
            elemEvents.appendChild(createEventControl(doc, ev));

        var elemProperties = doc.getElementById("properties");
        for (var pr : provider.getProperties())
            elemProperties.appendChild(createPropertyControl(doc, pr));
    }


    private static Node createEventControl(HTMLDocument doc, Event ev) {
        String name = '@' + ev.name;

        var radioHolder = doc.createElement("div");
        radioHolder.getStyle().setProperty("display", "inline-block");

        radioHolder.appendChild(makeRadioButton(doc, true, name));
        radioHolder.appendChild(makeRadioButton(doc, false, name));
        radioHolder.setAttribute("onchange", "handleEventUpdate('" + ev.name + "', document.getElementById('"+ name +"-true').checked)");

        var eventControl = makeControlElem(doc);
        eventControl.appendChild(makeNameElem(doc, name, 8));
        eventControl.appendChild(radioHolder);

        return eventControl;
    }

    private static Node createPropertyControl(HTMLDocument doc, Property pr) {
        String name = '$' + pr.name;

        var eventControl = makeControlElem(doc);
        eventControl.appendChild(makeNameElem(doc, name, 9.5f));

        if (pr.type instanceof StringT)
            eventControl.appendChild(makeStringControl(doc, pr));
        else if (pr.type instanceof IntT)
            eventControl.appendChild(makeIntegerControl(doc, pr));
        else if (pr.type instanceof FloatT)
            eventControl.appendChild(makeFloatControl(doc, pr));
        else if (pr.type instanceof ListT listT && listT.elementType() instanceof StringT)
            eventControl.appendChild(makeStringListControl(doc, pr));

        return eventControl;
    }


    private static HTMLElement makeControlElem(HTMLDocument doc) {
        var control = doc.createElement("div");
        control.getStyle().setProperty("padding-bottom", ".5em");
        control.getStyle().setProperty("display", "flex");
        return control;
    }

    private static HTMLElement makeNameElem(HTMLDocument doc, String name, float width) {
        var nameElem = doc.createElement("div");
        nameElem.getStyle().setProperty("width", width+"em");
        nameElem.setClassName("name");
        nameElem.setInnerText(name);
        return nameElem;
    }

    private static HTMLElement makeRadioButton(HTMLDocument doc, boolean value, String name) {
        var button = doc.createElement("input");
        button.setId(name + "-" + value);
        button.setAttribute("type", "radio");
        button.setAttribute("name", name);
        if (!value)
            button.setAttribute("checked", "");

        var label = doc.createElement("label");
        label.getStyle().setProperty("padding-right", "1em");
        label.appendChild(button);
        label.appendChild(doc.createTextNode(Boolean.toString(value)));

        return label;
    }

    private static HTMLElement makeStringControl(HTMLDocument doc, Property pr) {
        var textBox = doc.createElement("input");
        textBox.setAttribute("type", "text");
        textBox.setAttribute("value", provider.getPropertyValue(pr.name));
        textBox.getStyle().setProperty("flex", "1");
        textBox.setAttribute("onchange", "handleStringPropertyUpdate('" + pr.name + "', this.value)");
        return textBox;
    }

    private static HTMLElement makeIntegerControl(HTMLDocument doc, Property pr) {
        var textBox = doc.createElement("input");
        textBox.setAttribute("type", "number");
        textBox.setAttribute("value", Integer.toString(provider.<Integer>getPropertyValue(pr.name)));
        textBox.getStyle().setProperty("flex", "1");
        textBox.setAttribute("onchange", "handleIntegerPropertyUpdate('" + pr.name + "', this.value)");
        return textBox;
    }

    private static HTMLElement makeFloatControl(HTMLDocument doc, Property pr) {
        var textBox = doc.createElement("input");
        textBox.setAttribute("type", "number");
        textBox.setAttribute("value", Float.toString(provider.<Float>getPropertyValue(pr.name)));
        textBox.getStyle().setProperty("flex", "1");
        textBox.setAttribute("onchange", "handleFloatPropertyUpdate('" + pr.name + "', this.value)");
        return textBox;
    }

    private static HTMLElement makeStringListControl(HTMLDocument doc, Property pr) {
        var textBox = doc.createElement("input");
        textBox.setAttribute("type", "text");
        textBox.setAttribute("value", String.join(", ", provider.<List<String>>getPropertyValue(pr.name)));
        textBox.getStyle().setProperty("flex", "1");
        textBox.setAttribute("onchange", "handleStringListPropertyUpdate('" + pr.name + "', this.value)");
        return textBox;
    }


    @JSExport
    public static void handleEventUpdate(String event, boolean value) {
        provider.eventValues.put(event, value);
    }

    @JSExport
    public static void handleStringPropertyUpdate(String event, String value) {
        provider.propertyValues.put(event, value);
    }

    @JSExport
    public static void handleIntegerPropertyUpdate(String event, String value) {
        try {
            provider.propertyValues.put(event, Integer.parseInt(value));
        } catch (NumberFormatException ignored) {}
    }

    @JSExport
    public static void handleFloatPropertyUpdate(String event, String value) {
        try {
            provider.propertyValues.put(event, Float.parseFloat(value));
        } catch (NumberFormatException ignored) {}
    }

    @JSExport
    public static void handleStringListPropertyUpdate(String event, String value) {
        List<String> parts = Arrays.stream(value.split(",")).map(String::trim).filter(part -> !part.isEmpty()).toList();
        provider.propertyValues.put(event, parts);
    }



    @JSExport
    public static void compileAndRun(String musicConfig) {
        HTMLDocument doc = HTMLDocument.current();
        var output = doc.getElementById("output");
        output.setInnerHTML("");

        InputStream stream = new ByteArrayInputStream(musicConfig.getBytes(StandardCharsets.UTF_8));
        Loader.loadFrom(stream, new FakeMusicProvider(), provider).match(
                Test::printResult,
                Test::printErrors
        );
    }

    private static void printResult(Interpreter interpreter) {
        HTMLDocument doc = HTMLDocument.current();
        var output = doc.getElementById("output");
        output.appendChild(makeParagraph(doc, "Success!"));

        ArrayList<Pair<String, Value>> trace = new ArrayList<>();
        PlaylistChoice choice = interpreter.selectPlaylist(trace);
        if (choice == null)
            output.appendChild(makeParagraph(doc, "No playlist could be selected, which means the currently playing music will continue. If you want the music to stop, make sure that the empty playlist (play [ ];) is selected."));
        else if (choice.isInterrupt())
            output.appendChild(makeParagraph(doc, String.format("Selected new interrupt playlist: [ %s ]", String.join(", ", choice.playlist().stream().map(Music::musicPath).toList()))));
        else
            output.appendChild(makeParagraph(doc, String.format("Selected new playlist: [ %s ]", String.join(", ", choice.playlist().stream().map(Music::musicPath).toList()))));

        if (!trace.isEmpty())
            output.appendChild(makeParagraph(doc, String.format("Values computed during selection:\n%s", Utils.getKeyValuePairString(trace))));
    }

    private static void printErrors(List<LoadError> errors) {
        HTMLDocument doc = HTMLDocument.current();
        var output = doc.getElementById("output");
        output.appendChild(makeParagraph(doc, "Failure!"));

        for (var error : errors) {
            String text;

            if (error instanceof SynError err)
                text = String.format("Syntactic error [line %d, column %d]: %s", err.line(), err.column(), err.message());
            else if (error instanceof SemError err)
                text = String.format("Semantic error [line %d]: %s", err.line(), err.message());
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
