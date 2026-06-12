package me.molybdenum.ambience_mini.engine.client.configuration;

import me.molybdenum.ambience_mini.engine.client.configuration.messages.ExcError;
import me.molybdenum.ambience_mini.engine.client.configuration.messages.Message;
import me.molybdenum.ambience_mini.engine.client.configuration.abstract_syntax.config.Config;
import me.molybdenum.ambience_mini.engine.client.configuration.interpreter.Interpreter;
import me.molybdenum.ambience_mini.engine.client.configuration.music_provider.MusicProvider;
import me.molybdenum.ambience_mini.engine.client.configuration.semantic_analysis.SemanticAnalysis;
import me.molybdenum.ambience_mini.engine.client.configuration.syntactic_analysis.Parser;
import me.molybdenum.ambience_mini.engine.client.core.providers.BaseGameStateProvider;

import java.io.InputStream;
import java.util.ArrayList;

public class Loader {
    public static LoadResult loadFrom(
            InputStream configStream,
            MusicProvider musicProvider,
            BaseGameStateProvider gameStateProvider
    ) {
        ArrayList<Message> messages = new ArrayList<>();

        try {
            Config config = new Parser().Parse(configStream, messages);
            new SemanticAnalysis(musicProvider, gameStateProvider).validate(config, messages);

            if (messages.stream().noneMatch(Message::isError))
                return LoadResult.of(new Interpreter(config, gameStateProvider), messages);
        }
        catch (Exception ex) {
            messages.add(new ExcError(ex));
        }

        return LoadResult.fail(messages);
    }
}
