package me.molybdenum.ambience_mini.engine.shared.configuration;

import me.molybdenum.ambience_mini.engine.shared.configuration.messages.ExcError;
import me.molybdenum.ambience_mini.engine.shared.configuration.messages.Message;
import me.molybdenum.ambience_mini.engine.shared.configuration.abstract_syntax.config.Config;
import me.molybdenum.ambience_mini.engine.shared.configuration.music_provider.BaseMusicProvider;
import me.molybdenum.ambience_mini.engine.shared.configuration.semantic_analysis.SemanticAnalysis;
import me.molybdenum.ambience_mini.engine.shared.configuration.syntactic_analysis.Parser;
import me.molybdenum.ambience_mini.engine.client.core.providers.BaseGameStateProvider;
import org.jetbrains.annotations.Nullable;

import java.io.InputStream;
import java.util.ArrayList;

public class Loader {
    public static LoadResult loadFrom(
            InputStream configStream,
            BaseMusicProvider musicProvider,
            @Nullable BaseGameStateProvider gameStateProvider
    ) {
        ArrayList<Message> messages = new ArrayList<>();

        try {
            Config config = new Parser().Parse(configStream, messages);
            new SemanticAnalysis(musicProvider, gameStateProvider).validate(config, messages);

            if (messages.stream().noneMatch(Message::isError))
                return LoadResult.of(config, messages);
        }
        catch (Exception ex) {
            messages.add(new ExcError(ex));
        }

        return LoadResult.fail(messages);
    }


    // TODO: Different methods for load interpreter and load music manager (or similar).
}
