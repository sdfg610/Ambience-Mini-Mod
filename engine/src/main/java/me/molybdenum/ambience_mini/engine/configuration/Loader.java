package me.molybdenum.ambience_mini.engine.configuration;

import me.molybdenum.ambience_mini.engine.configuration.interpreter.Interpreter;
import me.molybdenum.ambience_mini.engine.configuration.music_provider.MusicProvider;
import me.molybdenum.ambience_mini.engine.configuration.semantic_analysis.SemError;
import me.molybdenum.ambience_mini.engine.configuration.semantic_analysis.TypeEnv;
import me.molybdenum.ambience_mini.engine.configuration.semantic_analysis.SemanticAnalysis;
import me.molybdenum.ambience_mini.engine.configuration.syntactic_analysis.Parser;
import me.molybdenum.ambience_mini.engine.configuration.syntactic_analysis.Scanner;
import me.molybdenum.ambience_mini.engine.core.providers.BaseGameStateProvider;
import org.slf4j.Logger;

import java.io.InputStream;
import java.util.List;
import java.util.Optional;

public class Loader {
    public static Optional<Interpreter> loadFrom(
            InputStream musicConfig,
            MusicProvider musicProvider,
            BaseGameStateProvider gameStateProvider,
            Logger logger
    ) {
        try {
            Parser parser = new Parser(new Scanner(musicConfig), logger);
            parser.Parse();

            if (!parser.hasErrors()) {
                List<SemError> semErr = new SemanticAnalysis(musicProvider, gameStateProvider)
                        .Conf(parser.mainNode, new TypeEnv())
                        .toList();

                if (semErr.isEmpty())
                    return Optional.of(new Interpreter(parser.mainNode, gameStateProvider));
                else
                    for (SemError error : semErr)
                        logger.error("Semantic error [line {}]: {}", error.line(), error.message());
            }
        }
        catch (Exception ex) {
            logger.error("An exception occurred during parsing of the AmbienceMini Config:\n", ex);
        }

        return Optional.empty();
    }
}
