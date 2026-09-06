package me.molybdenum.ambience_mini.engine.shared.configuration;

import me.molybdenum.ambience_mini.engine.shared.configuration.messages.ExcError;
import me.molybdenum.ambience_mini.engine.shared.configuration.messages.Message;
import me.molybdenum.ambience_mini.engine.shared.configuration.abstract_syntax.Config;
import me.molybdenum.ambience_mini.engine.shared.configuration.semantic_analysis.Setup;
import me.molybdenum.ambience_mini.engine.shared.music.music_provider.BaseMusicProvider;
import me.molybdenum.ambience_mini.engine.shared.configuration.semantic_analysis.SemanticAnalysis;
import me.molybdenum.ambience_mini.engine.shared.configuration.syntactic_analysis.Parser;

import java.io.InputStream;
import java.util.ArrayList;

public class Loader
{
    public static LoadResult<Config> loadAndValidateConfig(
            InputStream configStream,
            BaseMusicProvider musicProvider,
            Setup setup
    ) {
        ArrayList<Message> messages = new ArrayList<>();

        try {
            return SemanticAnalysis.validateAndOptimize(
                    new Parser().Parse(configStream, messages),
                    musicProvider, setup, messages
            );
        }
        catch (Exception ex) {
            messages.add(new ExcError(ex));
            return LoadResult.fail(messages);
        }
    }
}
