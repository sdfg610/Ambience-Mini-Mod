package me.molybdenum.ambience_mini.engine.client.core.music.music_selector;

import me.molybdenum.ambience_mini.engine.client.core.music.music_selector.selection.*;
import me.molybdenum.ambience_mini.engine.shared.configuration.LoadResult;
import me.molybdenum.ambience_mini.engine.shared.configuration.Loader;
import me.molybdenum.ambience_mini.engine.shared.configuration.abstract_syntax.command.*;
import me.molybdenum.ambience_mini.engine.shared.configuration.interpreter.BaseInterpreter;
import me.molybdenum.ambience_mini.engine.shared.configuration.abstract_syntax.*;
import me.molybdenum.ambience_mini.engine.client.core.providers.BaseGameStateProvider;
import me.molybdenum.ambience_mini.engine.shared.configuration.interpreter.VariableEnv;
import me.molybdenum.ambience_mini.engine.shared.configuration.interpreter.values.Value;
import me.molybdenum.ambience_mini.engine.shared.configuration.semantic_analysis.Setup;
import me.molybdenum.ambience_mini.engine.shared.core.music.music_provider.BaseMusicProvider;
import me.molybdenum.ambience_mini.engine.shared.utils.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.InputStream;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

public class MusicSelector extends BaseInterpreter
{
    private final Map<String, Command> presets;
    private final List<String> presetNames;
    private Pair<String, Command> selectedPreset;


    public MusicSelector(
            Config config,
            BaseGameStateProvider gameStateProvider
    ) {
        super(gameStateProvider);

        this.presets = config.presets().stream().collect(Collectors.toMap(pres -> pres.name().value(), Preset::body));
        this.presetNames = config.presets().stream().map(pres -> pres.name().value()).toList();

        var first = config.presets().get(0);
        this.selectedPreset = new Pair<>(first.name().value(), first.body());
    }


    public static LoadResult<MusicSelector> tryOf(
            InputStream configStream,
            BaseMusicProvider musicProvider,
            BaseGameStateProvider gameStateProvider
    ) {
        return Loader.loadAndValidateConfig(
                configStream,
                musicProvider,
                new Setup.Client(gameStateProvider)
        ).map(config -> new MusicSelector(config, gameStateProvider));
    }


    public List<String> getPresetNames() {
        return presetNames;
    }

    public String getSelectedPreset() {
        return selectedPreset.left();
    }

    public void setSelectedPreset(String name) {
        var cmd = presets.get(name);
        if (cmd == null)
            throw new RuntimeException("No preset with the name '" + name + "'");
        selectedPreset = new Pair<>(name, cmd);
    }


    public void prepare(@Nullable ArrayList<String> messages) {
        gameStateProvider.prepare(messages);
    }

    public @NotNull Selection selectPlaylist(@Nullable ArrayList<Pair<String, Value<?>>> trace) {
        BiConsumer<String, Value<?>> tracer =
                trace == null ? null : (name, val) -> trace.add(new Pair<>(name, val));

        try {
            if (tracer != null)
                gameStateProvider.registerOnFiredListener(tracer);
            var selection = evalSchedule(selectedPreset.right(), new VariableEnv());
            return selection == null ? new NoneSelection() : selection;
        } finally {
            if (tracer != null)
                gameStateProvider.unregisterOnFiredListener(tracer);
        }
    }


    // -----------------------------------------------------------------------------------------------------------------
    // Playlist selection
    private Selection evalSchedule(Command command, VariableEnv env) {
        if (command instanceof Skip)
            return null;
        else if (command instanceof Play play) {
            return evalExpr(play.playlist(), env)
                    .asMusicList()
                    .<Selection>map(musicList -> new PlaylistSelection(
                            musicList,
                            play.isInstant(),
                            play.getPriority(),
                            env.lookup(ModifierKinds.VOLUME).flatMap(Value::asFloat).orElse(null),
                            play.line()
                    ))
                    .orElse(play.ifdef() ? null : new UndefinedSelection(play.line()));
        }
        else if (command instanceof Vanilla vanilla) {
            return new VanillaSelection(vanilla.line());
        }
        else if (command instanceof Block block) {
            var newEnv = env.enterScope();
            return block.body().stream()
                    .map(sc -> evalSchedule(sc, newEnv))
                    .filter(Objects::nonNull)
                    .findFirst().orElse(null);
        }
        else if (command instanceof When when) {
            return evalExpr(when.condition(), env).mapBool(
                    b -> b ? evalSchedule(when.body(), env) : null
            );
        }
        else if (command instanceof Let let) {
            return evalSchedule(
                    let.body(),
                    env.enterScope().bind(let.ident().value(), evalExpr(let.value(), env))
            );
        }
        else if (command instanceof Modifier mod) {
            env.bind(mod.modifier(), evalExpr(mod.arg(), env));
            return null;
        }
        else if (command instanceof Interrupt) {
            throw new RuntimeException("Interrupts should not show up explicitly in the initialized schedule. Please report this error to the developer");
        }
        else
            throw new RuntimeException("Unhandled Schedule-type '" + command.getClass().getCanonicalName() + "' in evaluator. Please report this error to the developer");
    }
}
