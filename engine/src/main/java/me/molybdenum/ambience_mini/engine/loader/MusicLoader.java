package me.molybdenum.ambience_mini.engine.loader;

import me.molybdenum.ambience_mini.engine.loader.compiler.Compiler;
import me.molybdenum.ambience_mini.engine.loader.semantic_analysis.Env;
import me.molybdenum.ambience_mini.engine.loader.semantic_analysis.SemanticAnalysis;
import me.molybdenum.ambience_mini.engine.loader.syntactic_analysis.Parser;
import me.molybdenum.ambience_mini.engine.loader.syntactic_analysis.Scanner;
import me.molybdenum.ambience_mini.engine.player.rule.Rule;
import me.molybdenum.ambience_mini.engine.state.BaseGameStateProvider;
import org.slf4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

public class MusicLoader {
    public static final String MUSIC_DIRECTORY = "music";
    public static final String MUSIC_CONFIG_FILE = "music_config.txt";

    public static Optional<Rule> loadFrom(String ambienceDirectory, Logger logger, BaseGameStateProvider gameStateProvider) {
        try {
            File configFile = Path.of(ambienceDirectory, MUSIC_CONFIG_FILE).toFile();
            String musicPath = Path.of(ambienceDirectory, MUSIC_DIRECTORY).toString();

            if (!configFile.exists())
                return Optional.empty();

            Parser parser = new Parser(new Scanner(new FileInputStream(configFile)), logger);
            parser.Parse();

            if (!parser.hasErrors()) {
                List<String> semErr = new SemanticAnalysis(musicPath, gameStateProvider)
                        .Conf(parser.mainNode, new Env())
                        .toList();

                if (semErr.isEmpty())
                    return Optional.ofNullable(new Compiler(musicPath, gameStateProvider).Conf(parser.mainNode));
                else
                    for (String error : semErr)
                        logger.error("Semantic error: {}", error);
            }
        }
        catch (Exception ex) {
            logger.error("An exception occurred during parsing of the AmbienceMini Config:\n {}", ex.toString());
        }

        return Optional.empty();
    }
}
