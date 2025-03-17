package gsto.ambience_mini.music.loader;

import gsto.ambience_mini.AmbienceMini;
import gsto.ambience_mini.music.loader.compiler.Compiler;
import gsto.ambience_mini.music.loader.pretty_printer.PrettyPrinter;
import gsto.ambience_mini.music.loader.semantic_analysis.Env;
import gsto.ambience_mini.music.loader.semantic_analysis.SemanticAnalysis;
import gsto.ambience_mini.music.loader.syntactic_analysis.Parser;
import gsto.ambience_mini.music.loader.syntactic_analysis.Scanner;
import gsto.ambience_mini.music.player.rule.Rule;

import java.io.FileInputStream;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

public class MusicLoader {
    public static final String MUSIC_DIRECTORY = "music";
    public static final String MUSIC_CONFIG_FILE = "music_config.txt";

    public static Optional<Rule> loadFrom(String ambienceDirectory) {
        try {
            Path configFilePath = Path.of(ambienceDirectory, MUSIC_CONFIG_FILE);
            String musicPath = Path.of(ambienceDirectory, MUSIC_DIRECTORY).toString();

            Parser parser = new Parser(new Scanner(new FileInputStream(configFilePath.toFile())));
            parser.Parse();

            if (!parser.hasErrors()) {
                List<String> semErr = new SemanticAnalysis(musicPath)
                        .Conf(parser.mainNode, new Env())
                        .toList();

                if (semErr.isEmpty())
                    return Optional.ofNullable(new Compiler(musicPath).Conf(parser.mainNode));
                else
                    for (String error : semErr)
                        AmbienceMini.LOGGER.error("Semantic error: {}", error);
            }
        }
        catch (Exception ex) {
            AmbienceMini.LOGGER.error("An exception occurred during parsing of the AmbienceMini Config:\n {}", ex.toString());
        }

        return Optional.empty();
    }
}
