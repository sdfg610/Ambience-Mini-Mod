package me.molybdenum.ambience_mini.engine.configuration;

import me.molybdenum.ambience_mini.engine.configuration.abstract_syntax.config.Config;
import me.molybdenum.ambience_mini.engine.configuration.errors.ExcError;
import me.molybdenum.ambience_mini.engine.configuration.errors.LoadError;
import me.molybdenum.ambience_mini.engine.configuration.interpreter.Interpreter;
import me.molybdenum.ambience_mini.engine.configuration.music_provider.MusicProvider;
import me.molybdenum.ambience_mini.engine.configuration.semantic_analysis.SemanticAnalysis;
import me.molybdenum.ambience_mini.engine.configuration.syntactic_analysis.Parser;
import me.molybdenum.ambience_mini.engine.core.providers.BaseGameStateProvider;

import java.io.InputStream;
import java.util.ArrayList;

public class Loader {
    public static LoadResult loadFrom(
            InputStream configStream,
            MusicProvider musicProvider,
            BaseGameStateProvider gameStateProvider
    ) {
        ArrayList<LoadError> errors = new ArrayList<>();

        try {
            Config config = new Parser().Parse(configStream, errors);
            var sem = new SemanticAnalysis(musicProvider, gameStateProvider);
            sem.validate(config, errors);

            if (errors.isEmpty())
                return LoadResult.of(new Interpreter(config, gameStateProvider));
        }
        catch (Exception ex) {
            errors.add(new ExcError(ex));
        }

        return LoadResult.fail(errors);
    }
}
